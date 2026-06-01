package edu.sdapro.enrichment;

import edu.sdapro.domain.CanonicalAlert;
import edu.sdapro.threat.ThreatIntelService;
import java.util.Map;

class DeduplicationHandler extends EnrichmentHandler {
  protected void doHandle(CanonicalAlert alert, Map<String, Object> context) { context.put("deduplicated", true); }
}

class GeoIpHandler extends EnrichmentHandler {
  protected void doHandle(CanonicalAlert alert, Map<String, Object> context) { context.put("geoIp", alert.sourceIp().startsWith("203.") ? "US" : "PRIVATE"); }
}

class ThreatIntelHandler extends EnrichmentHandler {
  private final ThreatIntelService threatIntelService;
  ThreatIntelHandler(ThreatIntelService threatIntelService) { this.threatIntelService = threatIntelService; }
  protected void doHandle(CanonicalAlert alert, Map<String, Object> context) { context.put("threatIntel", threatIntelService.reputation(alert.sourceIp(), "IP")); }
}

class AssetContextHandler extends EnrichmentHandler {
  protected void doHandle(CanonicalAlert alert, Map<String, Object> context) { context.put("assetCriticality", alert.asset().contains("finance") ? "HIGH" : "MEDIUM"); }
}

class ClassificationHandler extends EnrichmentHandler {
  protected void doHandle(CanonicalAlert alert, Map<String, Object> context) { context.put("classification", alert.severity().name().equals("CRITICAL") ? "IMMEDIATE_RESPONSE" : "TRIAGE"); }
}
