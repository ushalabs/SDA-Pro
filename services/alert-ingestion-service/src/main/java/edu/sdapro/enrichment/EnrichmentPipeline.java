package edu.sdapro.enrichment;

import edu.sdapro.domain.CanonicalAlert;
import edu.sdapro.threat.ThreatIntelService;
import org.springframework.stereotype.Service;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class EnrichmentPipeline {
  private final ThreatIntelService threatIntelService;
  public EnrichmentPipeline(ThreatIntelService threatIntelService) { this.threatIntelService = threatIntelService; }
  public Map<String, Object> process(CanonicalAlert alert) {
    EnrichmentHandler first = new DeduplicationHandler();
    first.setNext(new GeoIpHandler())
      .setNext(new ThreatIntelHandler(threatIntelService))
      .setNext(new AssetContextHandler())
      .setNext(new ClassificationHandler());
    return first.handle(alert, new LinkedHashMap<>());
  }
}
