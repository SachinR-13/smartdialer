
package com.smartdialer.service;

import com.smartdialer.domain.*;
import com.smartdialer.provider.*;
import com.smartdialer.repository.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProviderEventService {

    private final DialerCallRepository calls;
    private final ProcessedProviderEventRepository events;
    private final AgentRepository agents;
    private final BorrowerRepository borrowers;

    private final CallStateMachine machine = new CallStateMachine();

    public ProviderEventService(
            DialerCallRepository calls,
            ProcessedProviderEventRepository events,
            AgentRepository agents,
            BorrowerRepository borrowers) {
        this.calls = calls;
        this.events = events;
        this.agents = agents;
        this.borrowers = borrowers;
    }

    @Transactional
    public String process(Long callId, ProviderEvent event) {

        // Ignore duplicate provider events.
        if (events.existsById(event.eventId())) {
            return "duplicate ignored";
        }

        DialerCall call = calls.findById(callId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Call not found: " + callId));

        CallStatus target = switch (event.type()) {
            case INITIATED -> CallStatus.INITIATED;
            case RINGING -> CallStatus.RINGING;
            case ANSWERED -> CallStatus.ANSWERED;
            case COMPLETED -> CallStatus.COMPLETED;
            case FAILED -> CallStatus.FAILED;
        };

        boolean terminal = call.getStatus() == CallStatus.COMPLETED
                || call.getStatus() == CallStatus.FAILED
                || call.getStatus() == CallStatus.CANCELLED;

        boolean allowed = machine.canTransition(call.getStatus(), target)
                || (target == CallStatus.COMPLETED && !terminal)
                || (target == CallStatus.FAILED && !terminal);

        if (allowed) {
            call.setStatus(target);

            Agent agent = call.getAgent();
            Borrower borrower = call.getBorrower();

            if (target == CallStatus.ANSWERED) {
                if (agent.getStatus() == AgentStatus.DIALING
                        || agent.getStatus() == AgentStatus.RESERVED) {
                    agent.setStatus(AgentStatus.CONNECTED);
                }
            }

            if (target == CallStatus.COMPLETED
                    || target == CallStatus.FAILED
                    || target == CallStatus.CANCELLED) {

                // Release the agent after the call ends.
                agent.setStatus(AgentStatus.AVAILABLE);

                if (target == CallStatus.COMPLETED) {
                    borrower.setStatus(BorrowerStatus.CONTACTED);
                } else {
                    borrower.setStatus(BorrowerStatus.QUEUED);
                }
            }
        }

        // Record the event after processing.
        events.save(new ProcessedProviderEvent(event.eventId()));

        return "processed";
    }
}
