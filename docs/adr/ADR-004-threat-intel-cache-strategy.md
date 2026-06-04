# Threat Intel Cache Strategy

## Status
Accepted

## Decision
Threat intelligence is cached behind a Proxy to avoid repeated external calls and to demonstrate rate-limit protection. Mock adapters make the behavior deterministic.

## Consequences
The architecture remains easy to run with `docker-compose up --build` while still showing the required architectural style evidence.
