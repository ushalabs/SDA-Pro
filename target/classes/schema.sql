CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS analysts (id UUID PRIMARY KEY DEFAULT uuid_generate_v4(), name TEXT, email TEXT UNIQUE, role TEXT, password_hash TEXT, created_at TIMESTAMP DEFAULT now());
CREATE TABLE IF NOT EXISTS alert_sources (id UUID PRIMARY KEY DEFAULT uuid_generate_v4(), name TEXT, source_type TEXT, ingestion_mode TEXT, enabled BOOLEAN, config_json TEXT, created_at TIMESTAMP DEFAULT now());
CREATE TABLE IF NOT EXISTS alerts (id UUID PRIMARY KEY DEFAULT uuid_generate_v4(), source_id UUID, external_alert_id TEXT, severity TEXT, status TEXT, raw_payload_json TEXT, normalized_payload_json TEXT, deduplication_key TEXT, received_at TIMESTAMP DEFAULT now(), created_at TIMESTAMP DEFAULT now());
CREATE TABLE IF NOT EXISTS alert_groups (id UUID PRIMARY KEY DEFAULT uuid_generate_v4(), group_type TEXT, name TEXT, attack_pattern TEXT, correlation_rule TEXT, max_severity TEXT, created_at TIMESTAMP DEFAULT now());
CREATE TABLE IF NOT EXISTS alert_group_members (group_id UUID, alert_id UUID);
CREATE TABLE IF NOT EXISTS enrichment_results (id UUID PRIMARY KEY DEFAULT uuid_generate_v4(), alert_id UUID, provider TEXT, enrichment_type TEXT, result_json TEXT, reputation_score INT, verdict TEXT, created_at TIMESTAMP DEFAULT now());
CREATE TABLE IF NOT EXISTS incidents (id UUID PRIMARY KEY DEFAULT uuid_generate_v4(), title TEXT, description TEXT, severity TEXT, current_state TEXT, affected_asset TEXT, assigned_analyst_id UUID, created_at TIMESTAMP DEFAULT now(), updated_at TIMESTAMP DEFAULT now(), closed_at TIMESTAMP);
CREATE TABLE IF NOT EXISTS incident_alerts (incident_id UUID, alert_id UUID);
CREATE TABLE IF NOT EXISTS incident_state_transitions (id UUID PRIMARY KEY DEFAULT uuid_generate_v4(), incident_id UUID, from_state TEXT, to_state TEXT, reason TEXT, changed_by TEXT, changed_at TIMESTAMP DEFAULT now());
CREATE TABLE IF NOT EXISTS response_actions (id UUID PRIMARY KEY DEFAULT uuid_generate_v4(), incident_id UUID, action_type TEXT, target TEXT, status TEXT, requested_by TEXT, approval_required BOOLEAN, approved_by TEXT, created_at TIMESTAMP DEFAULT now());
CREATE TABLE IF NOT EXISTS response_action_outcomes (id UUID PRIMARY KEY DEFAULT uuid_generate_v4(), action_id UUID, success BOOLEAN, result_json TEXT, rollback_available BOOLEAN, executed_at TIMESTAMP DEFAULT now());
CREATE TABLE IF NOT EXISTS threat_indicators (id UUID PRIMARY KEY DEFAULT uuid_generate_v4(), indicator TEXT, indicator_type TEXT, provider TEXT, reputation_score INT, verdict TEXT, first_seen TIMESTAMP DEFAULT now(), last_seen TIMESTAMP DEFAULT now());
CREATE TABLE IF NOT EXISTS notifications (id UUID PRIMARY KEY DEFAULT uuid_generate_v4(), incident_id UUID, channel TEXT, recipient TEXT, message TEXT, status TEXT, sent_at TIMESTAMP DEFAULT now());
CREATE TABLE IF NOT EXISTS audit_logs (id UUID PRIMARY KEY DEFAULT uuid_generate_v4(), event_type TEXT, actor_id TEXT, entity_type TEXT, entity_id TEXT, payload_json TEXT, created_at TIMESTAMP DEFAULT now());
