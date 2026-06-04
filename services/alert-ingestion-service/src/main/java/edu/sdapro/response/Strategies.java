package edu.sdapro.response;

import java.util.List;
import java.util.Map;

class AggressiveContainmentStrategy implements ResponseStrategy {
  public String name() { return "AggressiveContainmentStrategy"; }
  public List<String> determineActions(Map<String, Object> incident) { return List.of("BLOCK_IP", "ISOLATE_ENDPOINT", "ESCALATE"); }
}

class BalancedResponseStrategy implements ResponseStrategy {
  public String name() { return "BalancedResponseStrategy"; }
  public List<String> determineActions(Map<String, Object> incident) { return List.of("BLOCK_IP", "ESCALATE"); }
}

class ConservativeStrategy implements ResponseStrategy {
  public String name() { return "ConservativeStrategy"; }
  public List<String> determineActions(Map<String, Object> incident) { return List.of("ESCALATE"); }
}
