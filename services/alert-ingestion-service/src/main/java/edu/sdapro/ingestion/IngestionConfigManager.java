package edu.sdapro.ingestion;

import java.util.Map;

// PATTERN: Singleton
// RATIONALE: Alert ingestion configuration is a single source of truth shared by webhook and polling modes.
public final class IngestionConfigManager {
  private static final IngestionConfigManager INSTANCE = new IngestionConfigManager();
  private final Map<String, Boolean> enabledSources = Map.of("splunk", true, "firewall", true, "crowdstrike", true);
  private IngestionConfigManager() {}
  public static IngestionConfigManager getInstance() { return INSTANCE; }
  public boolean isEnabled(String sourceType) { return enabledSources.getOrDefault(sourceType.toLowerCase(), false); }
}
