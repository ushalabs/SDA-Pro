# Database Strategy

## Status
Accepted

## Decision
PostgreSQL stores incidents, alerts, state transitions, actions, notifications, and immutable audit logs. Redis is reserved for cache/session/rate-limit simulation.

## Consequences
The architecture remains easy to run with `docker-compose up --build` while still showing the required architectural style evidence.
