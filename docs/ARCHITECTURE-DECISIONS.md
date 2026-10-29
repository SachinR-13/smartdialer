# Architecture decision record

## Decision
Use a Java 21 / Spring Boot modular monolith with Spring Data JPA and MySQL. Use a provider interface and deterministic mock providers. Keep pacing, safety, allocation and event processing as separate components.

## Why
A modular monolith is easier to run and inspect within a short assignment. MySQL transactions and pessimistic locks provide a clear first correctness boundary without requiring Redis or Kafka. The database is authoritative; entity versions support optimistic conflict detection as an additional guard.

## Trade-offs and known gaps
- Holding a database transaction while calling a provider is not ideal; a production design should commit a reservation and transactional outbox record, then let a worker call the provider.
- Mock provider behavior is deterministic and does not emulate a real telecom network.
- Worker leases/reconciliation are described but not implemented as a distributed worker subsystem in this prototype.
- Provider events are accepted through a REST endpoint; production systems should authenticate/sign callbacks and correlate provider call IDs.
- The predictive formula is a transparent heuristic, not an ML model. Its parameters should be calibrated from observed campaign metrics.

## Scaling discussion
At larger volumes, row-lock contention on shared campaign/agent queues and synchronous provider calls become early bottlenecks. Next steps: partition campaigns/queues, use short transactions plus an outbox, run bounded workers, add provider circuit breakers, measure DB lock waits and queue age, then consider a message broker only when durable asynchronous throughput is needed.
