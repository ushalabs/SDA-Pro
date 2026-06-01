package edu.sdapro.threat;

import org.springframework.stereotype.Service;

@Service
public class ThreatIntelService {
  private final ThreatIntelProxy vtProxy = new ThreatIntelProxy(new VirusTotalAdapter());
  private final ThreatIntelProxy mispProxy = new ThreatIntelProxy(new MispAdapter());
  public ReputationResult reputation(String indicator, String type) {
    ReputationResult vt = vtProxy.checkReputation(indicator, type);
    ReputationResult misp = mispProxy.checkReputation(indicator, type);
    return vt.score() >= misp.score() ? vt : misp;
  }
  public ThreatIntelProxy vtProxy() { return vtProxy; }
}
