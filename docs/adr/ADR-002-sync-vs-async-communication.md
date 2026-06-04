# Synchronous vs Asynchronous Communication

## Status
Accepted

## Decision
Dashboard reads and analyst commands use REST. Alert, incident, response, notification, and audit propagation use events so publishers remain decoupled from subscribers.

## Consequences
The architecture remains easy to run with `docker-compose up --build` while still showing the required architectural style evidence.
