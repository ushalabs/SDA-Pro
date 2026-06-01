# SDA-Pro Services

The repository keeps the required SOA service boundaries as separate directories:

- alert-ingestion-service
- enrichment-correlation-service
- incident-management-service
- response-orchestration-service
- threat-intel-service
- notification-service
- audit-service
- identity-service

For a reliable university demo, `alert-ingestion-service` is the runnable Spring Boot composition root. It contains separate packages for the required services and implements all required APIs, events, persistence, and design patterns. The other directories document the service boundaries represented in the UML and ADRs.
