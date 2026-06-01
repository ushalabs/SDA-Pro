package edu.sdapro.threat;

// PATTERN: Adapter
// RATIONALE: Mock VirusTotal, MISP, and custom feeds expose different shapes but the platform needs one threat intel interface.
class VirusTotalAdapter implements ThreatIntelProvider {
  public ReputationResult checkReputation(String indicator, String type) {
    int score = indicator.startsWith("203.") ? 92 : 35;
    return new ReputationResult(indicator, score, score > 70 ? "MALICIOUS" : "SUSPICIOUS", "VirusTotalMock");
  }
}

class MispAdapter implements ThreatIntelProvider {
  public ReputationResult checkReputation(String indicator, String type) {
    int score = indicator.contains("198.51") ? 88 : 25;
    return new ReputationResult(indicator, score, score > 70 ? "MALICIOUS" : "BENIGN", "MISPMock");
  }
}
