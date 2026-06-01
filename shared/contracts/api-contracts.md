# SDA-Pro API Contracts

Base URL for the demo backend:

```text
http://localhost:8080
```

The project uses REST for synchronous service/dashboard operations and domain events for asynchronous SOC updates. External security integrations are mocked.

## Authentication / Identity

### POST /auth/login

Purpose: Authenticate a SOC analyst for the dashboard demo.

Request:

```json
{
  "email": "analyst@sda-pro.local",
  "password": "password"
}
```

Response:

```json
{
  "token": "mock-jwt-token",
  "email": "analyst@sda-pro.local",
  "role": "TIER_2"
}
```

### GET /identity/me

Purpose: Return the active analyst profile.

Response:

```json
{
  "name": "SOC Analyst",
  "email": "analyst@sda-pro.local",
  "role": "TIER_2"
}
```

## Alert Ingestion Service

### POST /ingest/webhook/{sourceType}

Purpose: Push-mode alert ingestion for Splunk, firewall, EDR, or generic source payloads.

Path parameters:

| Name | Type | Example |
| --- | --- | --- |
| sourceType | string | `splunk` |

Request:

```json
{
  "srcIp": "203.0.113.10",
  "destHost": "finance-db",
  "signature": "lateral-movement",
  "severity": "CRITICAL"
}
```

Response:

```json
{
  "alertId": "uuid",
  "incidentId": "uuid",
  "campaign": "Mock Campaign: lateral-movement containing 1 alert(s)",
  "enrichment": {
    "deduplicated": true,
    "geoIp": "US",
    "threatIntel": {
      "indicator": "203.0.113.10",
      "score": 92,
      "verdict": "MALICIOUS",
      "provider": "VirusTotalMock"
    },
    "assetCriticality": "HIGH",
    "classification": "IMMEDIATE_RESPONSE"
  }
}
```

Events published:

```text
AlertIngested
AlertEnriched
IncidentCreated
```

### POST /ingest/poll/{sourceId}

Purpose: Simulate pull-mode alert ingestion from a configured alert source.

Path parameters:

| Name | Type | Example |
| --- | --- | --- |
| sourceId | string | `firewall-demo` |

Response: Same shape as `POST /ingest/webhook/{sourceType}`.

### GET /alerts

Purpose: List normalized alerts.

Response:

```json
[
  {
    "id": "uuid",
    "source_id": "uuid or null",
    "external_alert_id": "mock-alert-id",
    "severity": "HIGH",
    "status": "INGESTED",
    "raw_payload_json": "{...}",
    "normalized_payload_json": "{...}",
    "deduplication_key": "sourceIp:asset:signature",
    "received_at": "timestamp",
    "created_at": "timestamp"
  }
]
```

### GET /alerts/{id}

Purpose: Read a single alert by ID.

Response: One alert object from `GET /alerts`.

## Enrichment & Correlation Service

### POST /enrichment/process

Purpose: Run alert normalization, Chain of Responsibility enrichment, Composite grouping, and incident creation.

Request:

```json
{
  "srcIp": "203.0.113.10",
  "destHost": "finance-db",
  "signature": "lateral-movement",
  "severity": "CRITICAL"
}
```

Response: Same shape as `POST /ingest/webhook/{sourceType}`.

Events published:

```text
AlertIngested
AlertEnriched
IncidentCreated
```

## Incident Management Service

### GET /incidents

Purpose: List incidents.

Response:

```json
[
  {
    "id": "uuid",
    "title": "Auto-created incident for finance-db",
    "description": "Correlated from enriched alert uuid",
    "severity": "CRITICAL",
    "current_state": "NEW",
    "affected_asset": "finance-db",
    "assigned_analyst_id": null,
    "created_at": "timestamp",
    "updated_at": "timestamp",
    "closed_at": null
  }
]
```

### POST /incidents

Purpose: Create an incident through the demo correlation pipeline.

Request: Same mock alert payload as `POST /enrichment/process`.

Response: Same shape as `POST /ingest/webhook/{sourceType}`.

### GET /incidents/{id}

Purpose: Read one incident by ID.

Response: One incident object from `GET /incidents`.

### PATCH /incidents/{id}/state

Purpose: Advance an incident through the State pattern lifecycle.

Request:

```json
{
  "action": "begin_triage",
  "reason": "analyst triage"
}
```

Supported demo actions:

| Current State | Action | Next State |
| --- | --- | --- |
| NEW | `begin_triage` | UNDER_TRIAGE |
| UNDER_TRIAGE | `contain` | CONTAINMENT |
| CONTAINMENT | `eradicate` | ERADICATION |
| ERADICATION | `recover` | RECOVERY |
| RECOVERY | `review` | POST_INCIDENT_REVIEW |
| POST_INCIDENT_REVIEW | `close` | CLOSED |

Response: Updated incident object.

Events published:

```text
IncidentStateChanged
```

## Response Orchestration Service

### POST /incidents/{id}/respond

Purpose: Run the response Facade. The facade selects a Strategy, creates actions with Factory Method, wraps actions with Decorators, executes through Proxy, persists outcomes, and publishes response events.

Response:

```json
{
  "incidentId": "uuid",
  "strategy": "AggressiveContainmentStrategy",
  "outcomes": [
    {
      "actionType": "BLOCK_IP",
      "success": true,
      "message": "Mock firewall blocked finance-db | audited"
    }
  ]
}
```

Events published:

```text
ResponseActionExecuted
IncidentStateChanged
```

### POST /incidents/{id}/actions

Purpose: Alias for executing a manual analyst response action through the same protected response facade.

Response: Same shape as `POST /incidents/{id}/respond`.

## Threat Intel Service

### POST /threat-intel/reputation

Purpose: Check indicator reputation using mock VirusTotal/MISP adapters behind a cache/rate-limit proxy.

Request:

```json
{
  "indicator": "203.0.113.10",
  "type": "IP"
}
```

Response:

```json
{
  "indicator": "203.0.113.10",
  "score": 92,
  "verdict": "MALICIOUS",
  "provider": "VirusTotalMock"
}
```

Events published:

```text
ThreatIntelUpdated
```

## Notification Service

### POST /notifications/dispatch

Purpose: Dispatch a mock Email, Slack, or PagerDuty notification and publish an event.

Request:

```json
{
  "channel": "SLACK",
  "recipient": "#soc-war-room",
  "message": "Critical incident created"
}
```

Response:

```json
{
  "status": "SENT",
  "mock": true
}
```

Events published:

```text
NotificationDispatched
```

## Audit Service

### GET /audit/events

Purpose: Read immutable audit records generated from domain events and mock notification dispatch.

Response:

```json
[
  {
    "id": "uuid",
    "event_type": "IncidentCreated",
    "actor_id": "system",
    "entity_type": "event",
    "entity_id": "IncidentCreated",
    "payload_json": "{...}",
    "created_at": "timestamp"
  }
]
```

## Dashboard / Realtime

### GET /dashboard/metrics

Purpose: Return dashboard summary metrics.

Response:

```json
{
  "alerts": 6,
  "incidents": 6,
  "openIncidents": 6,
  "mttd": "4m",
  "mttr": "18m"
}
```

### GET /dashboard/events

Purpose: Subscribe to realtime dashboard updates through Server-Sent Events.

Content type:

```text
text/event-stream
```

Example event:

```text
event: IncidentCreated
data: {"eventType":"IncidentCreated","occurredAt":"timestamp","payload":{"id":"uuid","state":"NEW"}}
```

