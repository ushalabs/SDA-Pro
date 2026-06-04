INSERT INTO analysts (name,email,role,password_hash)
SELECT 'SOC Analyst','analyst@sda-pro.local','TIER_2','password'
WHERE NOT EXISTS (SELECT 1 FROM analysts WHERE email='analyst@sda-pro.local');

INSERT INTO alert_sources (name,source_type,ingestion_mode,enabled,config_json)
SELECT 'Mock Splunk','splunk','webhook',true,'{"mock":true}'
WHERE NOT EXISTS (SELECT 1 FROM alert_sources WHERE source_type='splunk');

INSERT INTO alert_sources (name,source_type,ingestion_mode,enabled,config_json)
SELECT 'Mock Firewall','firewall','poll',true,'{"mock":true}'
WHERE NOT EXISTS (SELECT 1 FROM alert_sources WHERE source_type='firewall');

INSERT INTO alerts (external_alert_id,severity,status,raw_payload_json,normalized_payload_json,deduplication_key)
SELECT 'seed-splunk-1','HIGH','ENRICHED','{"source":"splunk","srcIp":"203.0.113.10"}','{"sourceType":"splunk","sourceIp":"203.0.113.10","asset":"finance-db","signature":"lateral-movement"}','203.0.113.10:finance-db'
WHERE NOT EXISTS (SELECT 1 FROM alerts WHERE external_alert_id='seed-splunk-1');

INSERT INTO incidents (title,description,severity,current_state,affected_asset)
SELECT 'Seeded lateral movement incident','Demo incident generated for dashboard visibility','HIGH','NEW','finance-db'
WHERE NOT EXISTS (SELECT 1 FROM incidents WHERE title='Seeded lateral movement incident');

INSERT INTO audit_logs (event_type,actor_id,entity_type,entity_id,payload_json)
SELECT 'AuditLogCreated','system','system','seed','{"message":"Seed demo data loaded"}'
WHERE NOT EXISTS (SELECT 1 FROM audit_logs WHERE entity_id='seed');
