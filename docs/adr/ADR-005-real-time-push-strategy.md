# Real-time Push Strategy

## Status
Accepted

## Decision
SSE is used for simple browser-native realtime updates. It is easier to demo than WebSocket while still proving observer/event-driven dashboard behavior.

## Consequences
The architecture remains easy to run with `docker-compose up --build` while still showing the required architectural style evidence.
