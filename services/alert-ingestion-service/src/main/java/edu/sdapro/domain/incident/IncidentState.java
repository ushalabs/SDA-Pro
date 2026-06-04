package edu.sdapro.domain.incident;

import java.util.List;

// PATTERN: State
// RATIONALE: Incident behavior changes depending on lifecycle phase.
public interface IncidentState {
  String name();
  List<String> allowedActions();
  IncidentState transition(String action);
}
