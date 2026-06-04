package edu.sdapro.response;

import org.springframework.stereotype.Service;
import java.util.Map;

@Service
public class ResponseStrategySelector {
  public ResponseStrategy select(Map<String, Object> incident) {
    String severity = String.valueOf(incident.getOrDefault("severity", "LOW"));
    if ("CRITICAL".equalsIgnoreCase(severity)) return new AggressiveContainmentStrategy();
    if ("HIGH".equalsIgnoreCase(severity)) return new BalancedResponseStrategy();
    return new ConservativeStrategy();
  }
}
