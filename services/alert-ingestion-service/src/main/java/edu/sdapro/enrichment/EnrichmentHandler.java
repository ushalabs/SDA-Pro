package edu.sdapro.enrichment;

import edu.sdapro.domain.CanonicalAlert;
import java.util.LinkedHashMap;
import java.util.Map;

// PATTERN: Chain of Responsibility
// RATIONALE: Alert processing stages are independently replaceable and can be assembled dynamically.
public abstract class EnrichmentHandler {
  private EnrichmentHandler next;
  public EnrichmentHandler setNext(EnrichmentHandler next) { this.next = next; return next; }
  public Map<String, Object> handle(CanonicalAlert alert, Map<String, Object> context) {
    Map<String, Object> enriched = new LinkedHashMap<>(context);
    doHandle(alert, enriched);
    return next == null ? enriched : next.handle(alert, enriched);
  }
  protected abstract void doHandle(CanonicalAlert alert, Map<String, Object> context);
}
