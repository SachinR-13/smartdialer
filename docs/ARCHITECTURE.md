# SmartDialer architecture

```mermaid
flowchart TD
  Campaign[Campaign / REST API] --> Engine[Progressive Dialer / Predictive Pacing]
  Engine --> Safety[Safety Controller: hard limits]
  Safety --> Allocator[Transactional Call Allocator]
  Allocator --> Provider[Provider Interface]
  Provider --> A[Provider A: fast/reliable]
  Provider --> B[Provider B: intermittent failures]
  Provider --> Events[Provider Event Handler]
  Events --> DB[(MySQL: agents, borrowers, calls, event IDs)]
  Allocator --> DB
```

The pacing engine only proposes a count. The Safety Controller independently applies hard capacity and provider-health checks. Allocation uses pessimistic row locks and entity versions; database state is the source of truth. Provider event IDs are persisted to make repeated deliveries idempotent. Production-grade delivery would additionally use a transactional outbox, provider reconciliation and lease-based worker recovery.
