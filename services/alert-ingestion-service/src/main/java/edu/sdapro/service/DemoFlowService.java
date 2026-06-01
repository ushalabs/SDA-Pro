package edu.sdapro.service;

import edu.sdapro.domain.CanonicalAlert;
import edu.sdapro.domain.alert.AlertCampaign;
import edu.sdapro.domain.alert.SingleAlert;
import edu.sdapro.enrichment.EnrichmentPipeline;
import edu.sdapro.events.EventBusPublisher;
import edu.sdapro.ingestion.AlertNormalizerFactory;
import edu.sdapro.ingestion.IngestionConfigManager;
import edu.sdapro.repository.PlatformRepository;
import org.springframework.stereotype.Service;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class DemoFlowService {
  private final PlatformRepository repository;
  private final EnrichmentPipeline pipeline;
  private final EventBusPublisher eventBus;
  private final AlertNormalizerFactory normalizerFactory = new AlertNormalizerFactory();
  public DemoFlowService(PlatformRepository repository, EnrichmentPipeline pipeline, EventBusPublisher eventBus) {
    this.repository = repository; this.pipeline = pipeline; this.eventBus = eventBus;
  }
  public Map<String,Object> ingest(String sourceType, Map<String,Object> raw) {
    if (!IngestionConfigManager.getInstance().isEnabled(sourceType)) throw new IllegalArgumentException("Source disabled: " + sourceType);
    CanonicalAlert alert = normalizerFactory.createNormalizer(sourceType).normalize(raw);
    UUID alertId = repository.saveAlert(alert);
    eventBus.publish("AlertIngested", Map.of("id", alertId, "sourceType", sourceType, "severity", alert.severity().name()));
    Map<String,Object> enrichment = pipeline.process(alert);
    repository.saveEnrichment(alertId, enrichment);
    eventBus.publish("AlertEnriched", Map.of("id", alertId, "context", enrichment.toString()));
    AlertCampaign campaign = new AlertCampaign("Mock Campaign: " + alert.signature());
    campaign.add(new SingleAlert(alert));
    UUID incidentId = repository.createIncident(alertId, campaign.getSeverity().name(), alert.asset());
    eventBus.publish("IncidentCreated", Map.of("id", incidentId, "alertId", alertId, "state", "NEW"));
    Map<String,Object> result = new LinkedHashMap<>();
    result.put("alertId", alertId);
    result.put("incidentId", incidentId);
    result.put("campaign", campaign.describe());
    result.put("enrichment", enrichment);
    return result;
  }
}
