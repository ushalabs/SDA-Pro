package edu.sdapro.ingestion;

import java.util.Map;

// PATTERN: Factory Method
// RATIONALE: Different security sources produce different schemas but the pipeline needs canonical alerts.
public class AlertNormalizerFactory {
  public AlertNormalizer createNormalizer(String sourceType) {
    return switch (sourceType.toLowerCase()) {
      case "splunk" -> new SplunkNormalizer();
      case "firewall" -> new FirewallNormalizer();
      case "crowdstrike", "edr" -> new CrowdStrikeNormalizer();
      default -> new GenericNormalizer(sourceType);
    };
  }
}
