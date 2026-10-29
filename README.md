# SmartDialer (Java 21)

A functional hiring-assignment prototype for progressive and predictive outbound dialing. It uses Spring Boot, Maven, Spring Data JPA and MySQL, with two mock telecom providers.

## Prerequisites
- JDK 21
- Maven 3.9+
- MySQL 8+

## Configure MySQL
Create a database and user (or use an existing local development account):

```sql
CREATE DATABASE smartdialer;
CREATE USER 'smartdialer'@'localhost' IDENTIFIED BY 'change-me';
GRANT ALL PRIVILEGES ON smartdialer.* TO 'smartdialer'@'localhost';
```

Set environment variables in PowerShell (for the current terminal):

```powershell
$env:DB_URL="jdbc:mysql://localhost:3306/smartdialer?createDatabaseIfNotExist=true"
$env:DB_USERNAME="smartdialer"
$env:DB_PASSWORD="change-me"
```

Do not commit real credentials. Hibernate `ddl-auto=update` is used for local prototype convenience; use versioned migrations before production.

## Run

```powershell
mvn clean test
mvn spring-boot:run
```

Application base URL: `http://localhost:8080/api`

## Quick demo (PowerShell)

Create two agents:
```powershell
Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/agents -ContentType 'application/json' -Body '{"name":"Agent 1","status":"AVAILABLE"}'
Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/agents -ContentType 'application/json' -Body '{"name":"Agent 2","status":"AVAILABLE"}'
```

Create borrowers:
```powershell
Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/borrowers -ContentType 'application/json' -Body '{"name":"Borrower 1","phoneNumber":"+910000000001"}'
Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/borrowers -ContentType 'application/json' -Body '{"name":"Borrower 2","phoneNumber":"+910000000002"}'
```

Progressive mode (starts at most one call):
```powershell
Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/dial -ContentType 'application/json' -Body '{"mode":"PROGRESSIVE","provider":"ProviderA"}'
```

Predictive mode:
```powershell
Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/dial -ContentType 'application/json' -Body '{"mode":"PREDICTIVE","answerRate":0.5,"provider":"ProviderA"}'
```

Inspect metrics:
```powershell
Invoke-RestMethod http://localhost:8080/api/metrics
Invoke-RestMethod http://localhost:8080/api/calls
```

Provider events can be simulated with `POST /api/calls/{id}/events` and a body such as:
```json
{"eventId":"event-001","providerCallId":"A-example","type":"RINGING","detail":"mock callback"}
```
Supported event types: `INITIATED`, `RINGING`, `ANSWERED`, `COMPLETED`, `FAILED`. Send the same `eventId` twice to verify duplicate suppression.

## API endpoints
- `POST/GET /api/agents`
- `PATCH /api/agents/{id}/status`
- `POST/GET /api/borrowers`
- `POST /api/dial`
- `GET /api/calls`
- `POST /api/calls/{id}/events`
- `GET /api/metrics`

## Implementation notes
- `PacingEngine` proposes volume; `SafetyController` applies hard limits independently.
- Agent and borrower rows are selected with pessimistic locks inside a transaction.
- `@Version` fields provide optimistic version checks.
- Event IDs are persisted to deduplicate callbacks.
- Provider A accepts quickly; Provider B simulates periodic provider failures.
- The project includes unit tests for safety decisions, pacing and call-state transitions.

## Assignment documents
See `docs/ARCHITECTURE.md`, `docs/STATE-MACHINES.md`, `docs/ARCHITECTURE-DECISIONS.md` and `docs/FINAL-ANSWER.md`.

## Known prototype limitations
This is a local functional prototype, not a production dialer. Provider initiation currently occurs during the allocation transaction; production should use a transactional outbox and asynchronous workers. Full worker lease recovery, real provider reconciliation, authentication/rate limiting, a richer scenario simulator and a dedicated load-test harness are follow-up work. Do not claim those are implemented.

### Simulation endpoint

`GET /api/simulation?agents=10&providerHealthy=true` runs a deterministic estimate for scenarios A-D (answer rates and average talk times). Set `providerHealthy=false` to demonstrate the Safety Controller rejecting new calls during provider degradation. It does not place calls or mutate campaign state.

### Basic load test

See `docs/LOAD-TEST.md` for a PowerShell smoke/load test and the allocation invariants to verify. Record actual measurements after running it locally.
