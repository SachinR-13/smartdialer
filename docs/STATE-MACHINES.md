# State machines

## Agent

`OFFLINE -> AVAILABLE -> RESERVED -> DIALING -> CONNECTED -> WRAP_UP -> AVAILABLE`

`AVAILABLE -> PAUSED -> AVAILABLE`; any active state may be moved to `OFFLINE` only after its call is safely reconciled. In this prototype the REST creation endpoint supports initial status; a production implementation should expose controlled status-transition endpoints.

## Call

`QUEUED -> RESERVED -> INITIATED -> RINGING -> ANSWERED -> CONNECTED -> COMPLETED`

Failure/cancellation may occur before terminal completion. Terminal states (`COMPLETED`, `FAILED`, `CANCELLED`) must not reopen. Duplicate event IDs are ignored. Out-of-order events that are not legal transitions are recorded without moving state backwards.
