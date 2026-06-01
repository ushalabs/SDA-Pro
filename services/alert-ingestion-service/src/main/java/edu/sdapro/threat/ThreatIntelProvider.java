package edu.sdapro.threat;

public interface ThreatIntelProvider {
  ReputationResult checkReputation(String indicator, String type);
}
