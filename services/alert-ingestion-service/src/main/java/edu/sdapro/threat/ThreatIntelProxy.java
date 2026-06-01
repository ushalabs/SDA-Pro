package edu.sdapro.threat;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// PATTERN: Proxy
// RATIONALE: Threat intelligence calls need caching and rate-limit simulation before reaching expensive external providers.
public class ThreatIntelProxy implements ThreatIntelProvider {
  private final ThreatIntelProvider provider;
  private final Map<String, ReputationResult> cache = new ConcurrentHashMap<>();
  public ThreatIntelProxy(ThreatIntelProvider provider) { this.provider = provider; }
  public ReputationResult checkReputation(String indicator, String type) {
    return cache.computeIfAbsent(type + ":" + indicator, key -> provider.checkReputation(indicator, type));
  }
  public int cacheSize() { return cache.size(); }
}
