package edu.sdapro;

import edu.sdapro.domain.CanonicalAlert;
import edu.sdapro.domain.Severity;
import edu.sdapro.domain.incident.States;
import edu.sdapro.enrichment.EnrichmentPipeline;
import edu.sdapro.ingestion.AlertNormalizerFactory;
import edu.sdapro.response.ResponseStrategySelector;
import edu.sdapro.threat.ThreatIntelService;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;

class PatternTests {
  @Test void normalizerFactoryCreatesCanonicalAlert() {
    var alert = new AlertNormalizerFactory().createNormalizer("splunk").normalize(Map.of("severity", "CRITICAL"));
    assertThat(alert.sourceType()).isEqualTo("splunk");
    assertThat(alert.severity()).isEqualTo(Severity.CRITICAL);
  }

  @Test void enrichmentChainAddsContext() {
    var pipeline = new EnrichmentPipeline(new ThreatIntelService());
    var alert = new CanonicalAlert(UUID.randomUUID(), "splunk", "203.0.113.10", "finance-db", "lateral-movement", Severity.HIGH, Instant.now(), Map.of());
    assertThat(pipeline.process(alert)).containsKeys("deduplicated", "geoIp", "threatIntel", "assetCriticality", "classification");
  }

  @Test void incidentStateTransitionsAreLegal() {
    assertThat(States.of("NEW").transition("begin_triage").name()).isEqualTo("UNDER_TRIAGE");
    assertThat(States.of("UNDER_TRIAGE").transition("contain").name()).isEqualTo("CONTAINMENT");
  }

  @Test void responseStrategySelectionUsesSeverity() {
    assertThat(new ResponseStrategySelector().select(Map.of("severity", "CRITICAL")).name()).contains("Aggressive");
  }

  @Test void threatIntelProxyCachesResults() {
    var service = new ThreatIntelService();
    service.reputation("203.0.113.10", "IP");
    service.reputation("203.0.113.10", "IP");
    assertThat(service.vtProxy().cacheSize()).isEqualTo(1);
  }
}
