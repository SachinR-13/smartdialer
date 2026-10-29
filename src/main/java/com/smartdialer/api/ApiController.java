
package com.smartdialer.api;

import com.smartdialer.domain.*;
import com.smartdialer.provider.*;
import com.smartdialer.service.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final DialerService dialer;
    private final ProviderEventService eventService;
    private final SimulationService simulation;

    public ApiController(
            DialerService dialer,
            ProviderEventService eventService,
            SimulationService simulation) {
        this.dialer = dialer;
        this.eventService = eventService;
        this.simulation = simulation;
    }

    public record CreateAgentRequest(
            @NotBlank String name,
            AgentStatus status) {
    }

    public record CreateBorrowerRequest(
            @NotBlank String name,
            @NotBlank String phoneNumber) {
    }

    public record AgentStatusRequest(
            @NotNull AgentStatus status) {
    }

    public record DialRequest(
            DialMode mode,
            @DecimalMin("0.01") @DecimalMax("1.0") Double answerRate,
            String provider) {
    }

    public record EventRequest(
            @NotBlank String eventId,
            @NotBlank String providerCallId,
            @NotNull ProviderEventType type,
            String detail) {
    }

    // DTO: avoids serializing lazy-loaded Hibernate entities directly.
    public record CallResponse(
            Long id,
            Long agentId,
            String agentName,
            Long borrowerId,
            String borrowerName,
            String borrowerPhoneNumber,
            CallStatus status,
            String idempotencyKey,
            String providerCallId,
            String providerName,
            java.time.Instant createdAt,
            java.time.Instant updatedAt,
            long version) {
    }

    @PostMapping("/agents")
    public ResponseEntity<Agent> createAgent(
            @Valid @RequestBody CreateAgentRequest r) {
        Agent agent = dialer.createAgent(
                r.name(),
                r.status() == null ? AgentStatus.AVAILABLE : r.status());

        return ResponseEntity.status(HttpStatus.CREATED).body(agent);
    }

    @GetMapping("/agents")
    public List<Agent> agents() {
        return dialer.listAgents();
    }

    @PatchMapping("/agents/{id}/status")
    public Agent updateAgentStatus(
            @PathVariable Long id,
            @Valid @RequestBody AgentStatusRequest r) {
        return dialer.updateAgentStatus(id, r.status());
    }

    @PostMapping("/borrowers")
    public ResponseEntity<Borrower> createBorrower(
            @Valid @RequestBody CreateBorrowerRequest r) {
        Borrower borrower = dialer.createBorrower(
                r.name(), r.phoneNumber());

        return ResponseEntity.status(HttpStatus.CREATED).body(borrower);
    }

    @GetMapping("/borrowers")
    public List<Borrower> borrowers() {
        return dialer.listBorrowers();
    }

    @PostMapping("/dial")
    public Map<String, Object> dial(@RequestBody DialRequest r) {
        return dialer.dial(
                r.mode() == null ? DialMode.PROGRESSIVE : r.mode(),
                r.answerRate() == null ? 0.5 : r.answerRate(),
                r.provider() == null ? "ProviderA" : r.provider());
    }

    @GetMapping("/calls")
    public List<CallResponse> calls() {
        return dialer.listCalls().stream()
                .map(call -> new CallResponse(
                        call.getId(),
                        call.getAgent().getId(),
                        call.getAgent().getName(),
                        call.getBorrower().getId(),
                        call.getBorrower().getName(),
                        call.getBorrower().getPhoneNumber(),
                        call.getStatus(),
                        call.getIdempotencyKey(),
                        call.getProviderCallId(),
                        call.getProviderName(),
                        call.getCreatedAt(),
                        call.getUpdatedAt(),
                        call.getVersion()))
                .toList();
    }

    @PostMapping("/calls/{id}/events")
    public Map<String, String> event(
            @PathVariable Long id,
            @Valid @RequestBody EventRequest r) {
        String result = eventService.process(
                id,
                new ProviderEvent(
                        r.eventId(),
                        r.providerCallId(),
                        r.type(),
                        r.detail()));

        return Map.of("result", result);
    }

    @GetMapping("/metrics")
    public Map<String, Object> metrics() {
        return dialer.metrics();
    }

    @GetMapping("/simulation")
    public Map<String, Object> simulation(
            @RequestParam(defaultValue = "10") int agents,
            @RequestParam(defaultValue = "true") boolean providerHealthy) {

        if (agents < 0 || agents > 100000) {
            throw new IllegalArgumentException(
                    "agents must be between 0 and 100000");
        }

        return simulation.run(agents, providerHealthy);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> badRequest(
            IllegalArgumentException e) {
        return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
    }
}
