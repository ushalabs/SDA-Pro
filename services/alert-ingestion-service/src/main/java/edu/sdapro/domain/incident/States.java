package edu.sdapro.domain.incident;

import java.util.List;

public class States {
  public static IncidentState of(String state) {
    return switch (state.toUpperCase()) {
      case "NEW" -> new NewState();
      case "UNDER_TRIAGE" -> new UnderTriageState();
      case "CONTAINMENT" -> new ContainmentState();
      case "ERADICATION" -> new EradicationState();
      case "RECOVERY" -> new RecoveryState();
      case "POST_INCIDENT_REVIEW" -> new PostIncidentReviewState();
      case "CLOSED" -> new ClosedState();
      default -> new NewState();
    };
  }
}

class NewState implements IncidentState {
  public String name() { return "NEW"; }
  public List<String> allowedActions() { return List.of("begin_triage"); }
  public IncidentState transition(String action) { return "begin_triage".equals(action) ? new UnderTriageState() : this; }
}

class UnderTriageState implements IncidentState {
  public String name() { return "UNDER_TRIAGE"; }
  public List<String> allowedActions() { return List.of("contain", "escalate"); }
  public IncidentState transition(String action) { return "contain".equals(action) ? new ContainmentState() : this; }
}

class ContainmentState implements IncidentState {
  public String name() { return "CONTAINMENT"; }
  public List<String> allowedActions() { return List.of("eradicate", "rollback"); }
  public IncidentState transition(String action) { return "eradicate".equals(action) ? new EradicationState() : this; }
}

class EradicationState implements IncidentState {
  public String name() { return "ERADICATION"; }
  public List<String> allowedActions() { return List.of("recover"); }
  public IncidentState transition(String action) { return "recover".equals(action) ? new RecoveryState() : this; }
}

class RecoveryState implements IncidentState {
  public String name() { return "RECOVERY"; }
  public List<String> allowedActions() { return List.of("review"); }
  public IncidentState transition(String action) { return "review".equals(action) ? new PostIncidentReviewState() : this; }
}

class PostIncidentReviewState implements IncidentState {
  public String name() { return "POST_INCIDENT_REVIEW"; }
  public List<String> allowedActions() { return List.of("close"); }
  public IncidentState transition(String action) { return "close".equals(action) ? new ClosedState() : this; }
}

class ClosedState implements IncidentState {
  public String name() { return "CLOSED"; }
  public List<String> allowedActions() { return List.of(); }
  public IncidentState transition(String action) { return this; }
}
