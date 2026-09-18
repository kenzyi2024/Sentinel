# API reference

Base URL: `http://localhost:8080/api`. Interactive docs (Swagger UI): `/swagger-ui.html`.
All payloads are JSON. Errors use a uniform shape (see [Errors](#errors)).

## Scenarios

- `GET /api/scenarios` → `ScenarioInfo[]`
- `GET /api/scenarios/{id}` → `ScenarioInfo`

## Agents

- `GET /api/agents` → `Agent[]` (built-in agents and their capability grants)

## Runs

- `POST /api/runs` — run a scenario. Body: `{ "scenarioId": "compromised-agent" }` → `201` `RunDetail`
- `GET /api/runs` → `RunSummary[]` (newest first)
- `GET /api/runs/{id}` → `RunDetail` (summary + events + report)
- `GET /api/runs/{id}/events` → `Event[]`
- `GET /api/runs/{id}/report` → `SecurityReport`

**`RunDetail`** = `{ summary: RunSummary, events: Event[], report: SecurityReport }`.

An **`Event`** carries the full evaluation: `actionType`, `resource`, `requiredCapabilities`,
`missingCapabilities`, `resourceSensitivity`, `riskAssessment { score, band, factors[] }`,
`injectionFindings[]`, `anomalyResult`, `policyDecision`, `decision`, `reasons[]`,
`recommendations[]`, `previousEventId`, `agentStateAfter`.

## Policies

- `GET /api/policies` → `Policy[]`
- `GET /api/policies/{id}` → `Policy`
- `POST /api/policies` — create. Body: `PolicyRequest` → `201` `Policy`
- `PUT /api/policies/{id}` — update. Body: `PolicyRequest` → `Policy`
- `DELETE /api/policies/{id}` → `204`
- `POST /api/policies/reset` — restore the default policy set → `Policy[]`

**`PolicyRequest`** example:

```json
{
  "name": "Research agents cannot access secrets",
  "priority": 100,
  "archetypes": ["RESEARCH"],
  "actionTypes": [],
  "resourcePattern": "secrets/**",
  "minSensitivity": "SECRET",
  "requiredCapability": null,
  "effect": "DENY",
  "reason": "Least privilege.",
  "enabled": true
}
```

Empty collections / null scalar conditions are treated as wildcards. When multiple rules match, the
strongest `effect` wins (`DENY` > `REQUIRE_APPROVAL` > `ALLOW` > `WARN` > `LOG_ONLY`).

## Meta & health

- `GET /api/meta` → `EngineMeta` (name, version, deterministic, scorer count, counts)
- `GET /api/health` → `{ "status": "UP", "service": "sentinel" }`

## Errors

```json
{
  "timestamp": "2026-01-01T00:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/runs",
  "fieldErrors": { "scenarioId": "scenarioId is required" }
}
```

`404` for unknown ids, `400` for validation failures.
