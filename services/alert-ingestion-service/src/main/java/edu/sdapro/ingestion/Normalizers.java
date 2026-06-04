package edu.sdapro.ingestion;

import edu.sdapro.domain.CanonicalAlert;
import edu.sdapro.domain.Severity;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

class SplunkNormalizer implements AlertNormalizer {
  public CanonicalAlert normalize(Map<String, Object> raw) {
    return new CanonicalAlert(UUID.randomUUID(), "splunk", val(raw, "srcIp", "203.0.113.10"), val(raw, "destHost", "finance-db"), val(raw, "signature", "suspicious-login"), severity(raw), Instant.now(), raw);
  }
  static String val(Map<String,Object> raw, String key, String fallback) { return String.valueOf(raw.getOrDefault(key, fallback)); }
  static Severity severity(Map<String,Object> raw) { return Severity.valueOf(String.valueOf(raw.getOrDefault("severity", "HIGH")).toUpperCase()); }
}

class FirewallNormalizer implements AlertNormalizer {
  public CanonicalAlert normalize(Map<String, Object> raw) {
    return new CanonicalAlert(UUID.randomUUID(), "firewall", SplunkNormalizer.val(raw, "source_ip", "198.51.100.23"), SplunkNormalizer.val(raw, "target", "vpn-gateway"), SplunkNormalizer.val(raw, "rule", "blocked-c2"), SplunkNormalizer.severity(raw), Instant.now(), raw);
  }
}

class CrowdStrikeNormalizer implements AlertNormalizer {
  public CanonicalAlert normalize(Map<String, Object> raw) {
    return new CanonicalAlert(UUID.randomUUID(), "crowdstrike", SplunkNormalizer.val(raw, "ip", "192.0.2.50"), SplunkNormalizer.val(raw, "host", "workstation-77"), SplunkNormalizer.val(raw, "detection", "malware"), SplunkNormalizer.severity(raw), Instant.now(), raw);
  }
}

class GenericNormalizer implements AlertNormalizer {
  private final String sourceType;
  GenericNormalizer(String sourceType) { this.sourceType = sourceType; }
  public CanonicalAlert normalize(Map<String, Object> raw) {
    return new CanonicalAlert(UUID.randomUUID(), sourceType, SplunkNormalizer.val(raw, "ip", "203.0.113.99"), SplunkNormalizer.val(raw, "asset", "unknown-asset"), SplunkNormalizer.val(raw, "signature", "generic-threat"), SplunkNormalizer.severity(raw), Instant.now(), raw);
  }
}
