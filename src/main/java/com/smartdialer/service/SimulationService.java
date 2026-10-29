package com.smartdialer.service;

import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class SimulationService {
    private final PacingEngine pacing;
    private final SafetyController safety;
    public SimulationService(PacingEngine pacing, SafetyController safety) { this.pacing=pacing; this.safety=safety; }

    public Map<String,Object> run(int availableAgents, boolean providerHealthy) {
        List<Map<String,Object>> rows=new ArrayList<>();
        Object[][] scenarios={
            {"A",0.20,120.0}, {"B",0.50,90.0}, {"C",0.70,180.0}, {"D",0.10,240.0}
        };
        for(Object[] s:scenarios) {
            String name=(String)s[0]; double answerRate=(double)s[1]; double talk=(double)s[2];
            int requested=pacing.requestedCalls(availableAgents,answerRate,0,0.10);
            SafetyDecision decision=safety.evaluate(requested,availableAgents,0,availableAgents,providerHealthy);
            Map<String,Object> row=new LinkedHashMap<>();
            row.put("scenario",name); row.put("answerRate",answerRate); row.put("averageTalkSeconds",talk);
            row.put("availableAgents",availableAgents); row.put("requestedCalls",requested); row.put("approvedCalls",decision.approved());
            row.put("estimatedAnsweredCalls",(int)Math.floor(decision.approved()*answerRate));
            row.put("estimatedAgentBusySeconds",Math.round(decision.approved()*answerRate*talk));
            row.put("safetyDecision",decision.reason()); row.put("providerHealthy",providerHealthy); rows.add(row);
        }
        Map<String,Object> result=new LinkedHashMap<>(); result.put("note","Estimates only; this deterministic simulation does not place real calls or update campaign records."); result.put("scenarios",rows); return result;
    }
}
