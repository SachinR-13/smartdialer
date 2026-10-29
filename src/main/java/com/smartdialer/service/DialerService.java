// package com.smartdialer.service;
// import com.smartdialer.domain.*; import com.smartdialer.provider.*; import com.smartdialer.repository.*; import org.springframework.data.domain.PageRequest; import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional; import java.util.*;
// @Service public class DialerService {
//  private final AgentRepository agents; private final BorrowerRepository borrowers; private final DialerCallRepository calls; private final List<TelecomProvider> providers; private final PacingEngine pacing; private final SafetyController safety;
//  public DialerService(AgentRepository agents,BorrowerRepository borrowers,DialerCallRepository calls,List<TelecomProvider> providers,PacingEngine pacing,SafetyController safety){this.agents=agents;this.borrowers=borrowers;this.calls=calls;this.providers=providers;this.pacing=pacing;this.safety=safety;}
//  public Agent createAgent(String name, AgentStatus status){return agents.save(new Agent(name,status));}
//  public Agent updateAgentStatus(Long id, AgentStatus status){Agent agent=agents.findById(id).orElseThrow(()->new IllegalArgumentException("Agent not found: "+id)); agent.setStatus(status); return agents.save(agent);}
//  public Borrower createBorrower(String name,String phone){return borrowers.save(new Borrower(name,phone));}
//  public List<Agent> listAgents(){return agents.findAll();} public List<Borrower> listBorrowers(){return borrowers.findAll();} public List<DialerCall> listCalls(){return calls.findTop100ByOrderByCreatedAtDesc();}
//  @Transactional public Map<String,Object> dial(DialMode mode,double answerRate,String providerName) {
//    TelecomProvider provider=providers.stream().filter(p->p.name().equalsIgnoreCase(providerName)).findFirst().orElseThrow(()->new IllegalArgumentException("Unknown provider: "+providerName));
//    long available=agents.countByStatus(AgentStatus.AVAILABLE);
//    long reserved=calls.countByStatusIn(List.of(CallStatus.RESERVED,CallStatus.INITIATED,CallStatus.RINGING));
//    long ringing=calls.countByStatusIn(List.of(CallStatus.RINGING,CallStatus.INITIATED));
//    int requested= mode==DialMode.PROGRESSIVE ? (int)Math.min(available,1_000_000) : pacing.requestedCalls((int)available,answerRate,(int)ringing,0.10);
//    SafetyDecision decision=safety.evaluate(requested,(int)available,(int)reserved,Math.max(1,(int)available),provider.healthy());
//    int approved=mode==DialMode.PROGRESSIVE?Math.min(decision.approved(),1):decision.approved();
//    List<Agent> agentBatch=agents.lockByStatus(AgentStatus.AVAILABLE,PageRequest.of(0,Math.max(approved,1)));
//    List<Borrower> borrowerBatch=borrowers.lockByStatus(BorrowerStatus.QUEUED,PageRequest.of(0,Math.max(approved,1)));
//    int count=Math.min(approved,Math.min(agentBatch.size(),borrowerBatch.size())); List<Map<String,Object>> started=new ArrayList<>();
//    for(int i=0;i<count;i++){
//      Agent agent=agentBatch.get(i); Borrower borrower=borrowerBatch.get(i); agent.setStatus(AgentStatus.RESERVED); borrower.setStatus(BorrowerStatus.RESERVED);
//      String key=UUID.randomUUID().toString(); DialerCall call=calls.save(new DialerCall(agent,borrower,key,provider.name()));
//      ProviderStartResult result;
//      try { result=provider.initiate(key,borrower.getPhoneNumber()); } catch(RuntimeException ex){result=new ProviderStartResult(null,false,"provider exception: "+ex.getMessage());}
//      if(result.accepted()) { call.setProviderCallId(result.providerCallId()); call.setStatus(CallStatus.INITIATED); agent.setStatus(AgentStatus.DIALING); started.add(Map.of("callId",call.getId(),"agentId",agent.getId(),"borrowerId",borrower.getId(),"providerCallId",result.providerCallId())); }
//      else { call.setStatus(CallStatus.FAILED); agent.setStatus(AgentStatus.AVAILABLE); borrower.setStatus(BorrowerStatus.QUEUED); }
//    }
//    Map<String,Object> response=new LinkedHashMap<>(); response.put("mode",mode);response.put("requestedCalls",requested);response.put("approvedCalls",approved);response.put("startedCalls",started.size());response.put("safetyDecision",decision.reason());response.put("fallbackToProgressive",decision.fallbackToProgressive());response.put("calls",started);return response;
//  }
//  public Map<String,Object> metrics(){long total=calls.count(); long connected=calls.countByStatusIn(List.of(CallStatus.CONNECTED,CallStatus.COMPLETED)); long active=calls.countByStatusIn(List.of(CallStatus.RESERVED,CallStatus.INITIATED,CallStatus.RINGING,CallStatus.ANSWERED,CallStatus.CONNECTED)); long available=agents.countByStatus(AgentStatus.AVAILABLE); long totalAgents=agents.count(); Map<String,Object> m=new LinkedHashMap<>();m.put("totalAgents",totalAgents);m.put("availableAgents",available);m.put("agentUtilizationPercent",totalAgents==0?0.0:Math.round((totalAgents-available)*1000.0/totalAgents)/10.0);m.put("totalCalls",total);m.put("activeCalls",active);m.put("connectedOrCompletedCalls",connected);return m;}
// }


package com.smartdialer.service;

import com.smartdialer.domain.*;
import com.smartdialer.provider.*;
import com.smartdialer.repository.*;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class DialerService {

    private final AgentRepository agents;
    private final BorrowerRepository borrowers;
    private final DialerCallRepository calls;
    private final List<TelecomProvider> providers;
    private final PacingEngine pacing;
    private final SafetyController safety;

    public DialerService(
            AgentRepository agents,
            BorrowerRepository borrowers,
            DialerCallRepository calls,
            List<TelecomProvider> providers,
            PacingEngine pacing,
            SafetyController safety) {
        this.agents = agents;
        this.borrowers = borrowers;
        this.calls = calls;
        this.providers = providers;
        this.pacing = pacing;
        this.safety = safety;
    }

    public Agent createAgent(String name, AgentStatus status) {
        return agents.save(new Agent(name, status));
    }

    public Agent updateAgentStatus(Long id, AgentStatus status) {
        Agent agent = agents.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Agent not found: " + id));

        agent.setStatus(status);
        return agents.save(agent);
    }

    public Borrower createBorrower(String name, String phone) {
        return borrowers.save(new Borrower(name, phone));
    }

    public List<Agent> listAgents() {
        return agents.findAll();
    }

    public List<Borrower> listBorrowers() {
        return borrowers.findAll();
    }

    @Transactional(readOnly = true)
    public List<DialerCall> listCalls() {
        return calls.findAllWithAgentAndBorrower(PageRequest.of(0, 100));
    }

    @Transactional
    public Map<String, Object> dial(
            DialMode mode,
            double answerRate,
            String providerName) {

        TelecomProvider provider = providers.stream()
                .filter(p -> p.name().equalsIgnoreCase(providerName))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Unknown provider: " + providerName));

        long available = agents.countByStatus(AgentStatus.AVAILABLE);

        long reserved = calls.countByStatusIn(
                List.of(
                        CallStatus.RESERVED,
                        CallStatus.INITIATED,
                        CallStatus.RINGING
                )
        );

        long ringing = calls.countByStatusIn(
                List.of(
                        CallStatus.RINGING,
                        CallStatus.INITIATED
                )
        );

        int requested;

        if (mode == DialMode.PROGRESSIVE) {
            requested = (int) Math.min(available, 1_000_000);
        } else {
            requested = pacing.requestedCalls(
                    (int) available,
                    answerRate,
                    (int) ringing,
                    0.10
            );
        }

        SafetyDecision decision = safety.evaluate(
                requested,
                (int) available,
                (int) reserved,
                Math.max(1, (int) available),
                provider.healthy()
        );

        int approved = mode == DialMode.PROGRESSIVE
                ? Math.min(decision.approved(), 1)
                : decision.approved();

        List<Agent> agentBatch = agents.lockByStatus(
                AgentStatus.AVAILABLE,
                PageRequest.of(0, Math.max(approved, 1))
        );

        List<Borrower> borrowerBatch = borrowers.lockByStatus(
                BorrowerStatus.QUEUED,
                PageRequest.of(0, Math.max(approved, 1))
        );

        int count = Math.min(
                approved,
                Math.min(agentBatch.size(), borrowerBatch.size())
        );

        List<Map<String, Object>> started = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            Agent agent = agentBatch.get(i);
            Borrower borrower = borrowerBatch.get(i);

            agent.setStatus(AgentStatus.RESERVED);
            borrower.setStatus(BorrowerStatus.RESERVED);

            String key = UUID.randomUUID().toString();

            DialerCall call = calls.save(
                    new DialerCall(agent, borrower, key, provider.name())
            );

            ProviderStartResult result;

            try {
                result = provider.initiate(
                        key,
                        borrower.getPhoneNumber()
                );
            } catch (RuntimeException ex) {
                result = new ProviderStartResult(
                        null,
                        false,
                        "provider exception: " + ex.getMessage()
                );
            }

            if (result.accepted()) {
                call.setProviderCallId(result.providerCallId());
                call.setStatus(CallStatus.INITIATED);
                agent.setStatus(AgentStatus.DIALING);

                started.add(Map.of(
                        "callId", call.getId(),
                        "agentId", agent.getId(),
                        "borrowerId", borrower.getId(),
                        "providerCallId", result.providerCallId()
                ));
            } else {
                call.setStatus(CallStatus.FAILED);
                agent.setStatus(AgentStatus.AVAILABLE);
                borrower.setStatus(BorrowerStatus.QUEUED);
            }
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("mode", mode);
        response.put("requestedCalls", requested);
        response.put("approvedCalls", approved);
        response.put("startedCalls", started.size());
        response.put("safetyDecision", decision.reason());
        response.put("fallbackToProgressive", decision.fallbackToProgressive());
        response.put("calls", started);

        return response;
    }

    public Map<String, Object> metrics() {
        long total = calls.count();

        long connected = calls.countByStatusIn(
                List.of(CallStatus.CONNECTED, CallStatus.COMPLETED)
        );

        long active = calls.countByStatusIn(
                List.of(
                        CallStatus.RESERVED,
                        CallStatus.INITIATED,
                        CallStatus.RINGING,
                        CallStatus.ANSWERED,
                        CallStatus.CONNECTED
                )
        );

        long available = agents.countByStatus(AgentStatus.AVAILABLE);
        long totalAgents = agents.count();

        Map<String, Object> metrics = new LinkedHashMap<>();

        metrics.put("totalAgents", totalAgents);
        metrics.put("availableAgents", available);
        metrics.put(
                "agentUtilizationPercent",
                totalAgents == 0
                        ? 0.0
                        : Math.round(
                                (totalAgents - available) * 1000.0 / totalAgents
                        ) / 10.0
        );
        metrics.put("totalCalls", total);
        metrics.put("activeCalls", active);
        metrics.put("connectedOrCompletedCalls", connected);

        return metrics;
    }
}
