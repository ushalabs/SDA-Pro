package edu.sdapro.domain.alert;

import edu.sdapro.domain.Severity;
import java.util.List;
import java.util.UUID;

// PATTERN: Composite
// RATIONALE: Single alerts, campaigns, and incident clusters must be treated uniformly by enrichment, correlation, and response flows.
public interface AlertComponent {
  UUID getId();
  Severity getSeverity();
  String describe();
  default void add(AlertComponent component) { throw new UnsupportedOperationException("Leaf alerts cannot contain children"); }
  default void remove(AlertComponent component) { throw new UnsupportedOperationException("Leaf alerts cannot contain children"); }
  default List<AlertComponent> getChildren() { return List.of(); }
}
