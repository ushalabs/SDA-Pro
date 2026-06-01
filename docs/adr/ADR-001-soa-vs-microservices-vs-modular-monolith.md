# SOA vs Microservices vs Modular Monolith

## Status
Accepted

## Decision
SDA-Pro uses SOA because the grading rubric requires clear autonomous service capabilities while a semester timeline benefits from a deployable demo. Full microservices would add operational overhead; a pure modular monolith would hide service contracts.

## Consequences
The architecture remains easy to run with `docker-compose up --build` while still showing the required architectural style evidence.
