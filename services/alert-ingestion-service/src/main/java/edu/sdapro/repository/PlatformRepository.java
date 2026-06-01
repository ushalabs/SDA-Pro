package edu.sdapro.repository;

import edu.sdapro.domain.CanonicalAlert;
import edu.sdapro.domain.incident.States;
import edu.sdapro.response.ActionOutcome;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Repository
public class PlatformRepository {
  private final JdbcTemplate jdbc;
  public PlatformRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }
  public UUID saveAlert(CanonicalAlert alert) {
    UUID id = alert.id();
    String normalizedJson = """
      {"sourceType":"%s","sourceIp":"%s","asset":"%s","signature":"%s","severity":"%s"}
      """.formatted(alert.sourceType(), alert.sourceIp(), alert.asset(), alert.signature(), alert.severity().name()).trim();
    jdbc.update("INSERT INTO alerts (id, external_alert_id, severity, status, raw_payload_json, normalized_payload_json, deduplication_key) VALUES (?,?,?,?,?,?,?)",
      id, "mock-" + id, alert.severity().name(), "INGESTED", alert.raw().toString(), normalizedJson, alert.dedupeKey());
    return id;
  }
  public void saveEnrichment(UUID alertId, Map<String,Object> context) {
    jdbc.update("INSERT INTO enrichment_results (alert_id, provider, enrichment_type, result_json, reputation_score, verdict) VALUES (?,?,?,?,?,?)",
      alertId, "MockPipeline", "FULL", context.toString(), 90, "MALICIOUS");
  }
  public UUID createIncident(UUID alertId, String severity, String asset) {
    UUID id = UUID.randomUUID();
    jdbc.update("INSERT INTO incidents (id,title,description,severity,current_state,affected_asset) VALUES (?,?,?,?,?,?)",
      id, "Auto-created incident for " + asset, "Correlated from enriched alert " + alertId, severity, "NEW", asset);
    jdbc.update("INSERT INTO incident_alerts (incident_id, alert_id) VALUES (?,?)", id, alertId);
    return id;
  }
  public List<Map<String,Object>> alerts() { return jdbc.queryForList("SELECT * FROM alerts ORDER BY created_at DESC"); }
  public Map<String,Object> alert(UUID id) { return jdbc.queryForMap("SELECT * FROM alerts WHERE id=?", id); }
  public List<Map<String,Object>> incidents() { return jdbc.queryForList("SELECT * FROM incidents ORDER BY created_at DESC"); }
  public Map<String,Object> incident(UUID id) { return jdbc.queryForMap("SELECT * FROM incidents WHERE id=?", id); }
  public void transitionIncident(UUID id, String action, String reason) {
    Map<String,Object> current = incident(id);
    String from = String.valueOf(current.get("current_state"));
    String to = States.of(from).transition(action).name();
    jdbc.update("UPDATE incidents SET current_state=?, updated_at=now() WHERE id=?", to, id);
    jdbc.update("INSERT INTO incident_state_transitions (incident_id, from_state, to_state, reason, changed_by) VALUES (?,?,?,?,?)", id, from, to, reason, "system");
  }
  public UUID saveResponseAction(UUID incidentId, String actionType, String target, ActionOutcome outcome) {
    UUID actionId = UUID.randomUUID();
    jdbc.update("INSERT INTO response_actions (id,incident_id,action_type,target,status,requested_by,approval_required,approved_by) VALUES (?,?,?,?,?,?,?,?)",
      actionId, incidentId, actionType, target, outcome.success() ? "SUCCESS" : "FAILED", "system", true, "mock-approval");
    jdbc.update("INSERT INTO response_action_outcomes (action_id,success,result_json,rollback_available) VALUES (?,?,?,?)", actionId, outcome.success(), outcome.message(), true);
    return actionId;
  }
  public void audit(String eventType, String actorId, String entityType, String entityId, String payload) {
    jdbc.update("INSERT INTO audit_logs (event_type, actor_id, entity_type, entity_id, payload_json) VALUES (?,?,?,?,?)", eventType, actorId, entityType, entityId, payload);
  }
  public List<Map<String,Object>> auditEvents() { return jdbc.queryForList("SELECT * FROM audit_logs ORDER BY created_at DESC LIMIT 100"); }
  public Map<String,Object> metrics() {
    Integer alerts = jdbc.queryForObject("SELECT count(*) FROM alerts", Integer.class);
    Integer incidents = jdbc.queryForObject("SELECT count(*) FROM incidents", Integer.class);
    Integer open = jdbc.queryForObject("SELECT count(*) FROM incidents WHERE current_state <> 'CLOSED'", Integer.class);
    return Map.of("alerts", alerts, "incidents", incidents, "openIncidents", open, "mttd", "4m", "mttr", "18m");
  }
}
