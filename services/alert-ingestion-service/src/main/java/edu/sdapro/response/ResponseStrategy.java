package edu.sdapro.response;

import java.util.List;
import java.util.Map;

// PATTERN: Strategy
// RATIONALE: Response algorithms vary by severity, asset criticality, and business context.
public interface ResponseStrategy {
  String name();
  List<String> determineActions(Map<String, Object> incident);
}
