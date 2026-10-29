# Basic load-test procedure

The assignment asks for a basic load test. Run the application against a local test database, seed a campaign-sized pool of agents and borrowers, then measure endpoint latency and verify the hard capacity invariants. This repository's core unit tests validate pacing and safety decisions; the following PowerShell loop is a lightweight manual endpoint smoke/load test, not a substitute for a dedicated performance benchmark.

```powershell
$sw = [System.Diagnostics.Stopwatch]::StartNew()
$errors = 0
1..100 | ForEach-Object {
  try { Invoke-RestMethod -Uri 'http://localhost:8080/api/metrics' -TimeoutSec 5 | Out-Null }
  catch { $errors++ }
}
$sw.Stop()
[pscustomobject]@{ Requests=100; Errors=$errors; ElapsedMs=$sw.ElapsedMilliseconds; AverageMs=[math]::Round($sw.Elapsed.TotalMilliseconds/100,2) }
```

For a meaningful allocation test, create multiple agents and borrowers and send concurrent `/api/dial` requests from a test client. Confirm that no agent is assigned to more than one active call and that approved call counts never exceed the Safety Controller's capacity. Record p50/p95 latency, error rate and database lock waits. This is intentionally described as a manual procedure; do not report benchmark results until you have run it.
