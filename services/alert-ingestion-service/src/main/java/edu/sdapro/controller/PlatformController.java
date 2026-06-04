package edu.sdapro.controller;

import edu.sdapro.events.EventBusPublisher;
import edu.sdapro.repository.PlatformRepository;
import edu.sdapro.response.IncidentResponseFacade;
import edu.sdapro.service.DemoFlowService;
import edu.sdapro.threat.ThreatIntelService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.util.Map;
import java.util.UUID;

@RestController
@CrossOrigin(origins = "*")
public class PlatformController {
  private final DemoFlowService demoFlow;
  private final PlatformRepository repository;
  private final ThreatIntelService threatIntel;
  private final IncidentResponseFacade responseFacade;
  private final EventBusPublisher eventBus;
  public PlatformController(DemoFlowService demoFlow, PlatformRepository repository, ThreatIntelService threatIntel, IncidentResponseFacade responseFacade, EventBusPublisher eventBus) {
    this.demoFlow = demoFlow; this.repository = repository; this.threatIntel = threatIntel; this.responseFacade = responseFacade; this.eventBus = eventBus;
  }
  @PostMapping("/ingest/webhook/{sourceType}") public Map<String,Object> ingest(@PathVariable("sourceType") String sourceType, @RequestBody Map<String,Object> body) { return demoFlow.ingest(sourceType, body); }
  @PostMapping("/ingest/poll/{sourceId}") public Map<String,Object> poll(@PathVariable("sourceId") String sourceId) { return demoFlow.ingest("firewall", Map.of("source_ip", "198.51.100.23", "target", "vpn-gateway", "rule", "blocked-c2", "severity", "HIGH", "sourceId", sourceId)); }
  @GetMapping("/alerts") public Object alerts() { return repository.alerts(); }
  @GetMapping("/alerts/{id}") public Object alert(@PathVariable("id") UUID id) { return repository.alert(id); }
  @PostMapping("/enrichment/process") public Map<String,Object> enrichmentProcess(@RequestBody(required=false) Map<String,Object> body) { return demoFlow.ingest("splunk", body == null ? Map.of("severity","HIGH") : body); }
  @GetMapping("/incidents") public Object incidents() { return repository.incidents(); }
  @PostMapping("/incidents") public Map<String,Object> createIncident(@RequestBody Map<String,Object> body) { return demoFlow.ingest("splunk", body); }
  @GetMapping("/incidents/{id}") public Object incident(@PathVariable("id") UUID id) { return repository.incident(id); }
  @PatchMapping("/incidents/{id}/state") public Map<String,Object> state(@PathVariable("id") UUID id, @RequestBody Map<String,Object> body) {
    repository.transitionIncident(id, String.valueOf(body.getOrDefault("action", "begin_triage")), String.valueOf(body.getOrDefault("reason", "manual")));
    eventBus.publish("IncidentStateChanged", Map.of("id", id, "action", body.getOrDefault("action", "begin_triage")));
    return repository.incident(id);
  }
  @PostMapping("/incidents/{id}/respond") public Object respond(@PathVariable("id") UUID id) { return responseFacade.assessAndRespond(id); }
  @PostMapping("/incidents/{id}/actions") public Object action(@PathVariable("id") UUID id) { return responseFacade.assessAndRespond(id); }
  @PostMapping("/threat-intel/reputation") public Object reputation(@RequestBody Map<String,Object> body) {
    var result = threatIntel.reputation(String.valueOf(body.getOrDefault("indicator", "203.0.113.10")), String.valueOf(body.getOrDefault("type", "IP")));
    eventBus.publish("ThreatIntelUpdated", Map.of("indicator", result.indicator(), "score", result.score(), "verdict", result.verdict()));
    return result;
  }
  @PostMapping("/notifications/dispatch") public Map<String,Object> notify(@RequestBody Map<String,Object> body) {
    repository.audit("NotificationDispatched", "system", "notification", "mock", body.toString());
    eventBus.publish("NotificationDispatched", body);
    return Map.of("status", "SENT", "mock", true);
  }
  @GetMapping("/audit/events") public Object audit() { return repository.auditEvents(); }
  @PostMapping("/auth/login") public Map<String,Object> login(@RequestBody Map<String,Object> body) { return Map.of("token", "mock-jwt-token", "email", body.getOrDefault("email", "analyst@sda-pro.local"), "role", "TIER_2"); }
  @GetMapping("/identity/me") public Map<String,Object> me() { return Map.of("name", "SOC Analyst", "email", "analyst@sda-pro.local", "role", "TIER_2"); }
  @GetMapping("/dashboard/metrics") public Object metrics() { return repository.metrics(); }
  @GetMapping("/dashboard/events") public SseEmitter events() { return eventBus.subscribe(); }
}
