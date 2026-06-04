package edu.sdapro.domain;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record CanonicalAlert(UUID id, String sourceType, String sourceIp, String asset, String signature, Severity severity, Instant receivedAt, Map<String, Object> raw) {
  public String dedupeKey() {
    return sourceIp + ":" + asset + ":" + signature;
  }
}
