package edu.sdapro.response;

import edu.sdapro.events.EventBusPublisher;
import edu.sdapro.repository.PlatformRepository;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

// PATTERN: Facade
// RATIONALE: Clients trigger response through one operation instead of coordinating state validation, strategy selection, action execution, persistence, events, and notification.
@Service
public class IncidentResponseFacade {
  private final PlatformRepository repository;
  private final ResponseStrategySelector selector;
  private final EventBusPublisher eventBus;
  private final ResponseActionFactory factory = new ResponseActionFactory();
  public IncidentResponseFacade(PlatformRepository repository, ResponseStrategySelector selector, EventBusPublisher eventBus) {
    this.repository = repository; this.selector = selector; this.eventBus = eventBus;
  }
  public Map<String, Object> assessAndRespond(UUID incidentId) {
    Map<String, Object> incident = repository.incident(incidentId);
    ResponseStrategy strategy = selector.select(incident);
    List<ActionOutcome> outcomes = new ArrayList<>();
    for (String actionType : strategy.determineActions(incident)) {
      ResponseAction action = new ResponseActionProxy(new RollbackCapabilityDecorator(new ApprovalGateDecorator(new AuditLogDecorator(factory.createAction(actionType)))));
      ActionOutcome outcome = action.execute(String.valueOf(incident.getOrDefault("affected_asset", "unknown")));
      UUID actionId = repository.saveResponseAction(incidentId, actionType, String.valueOf(incident.get("affected_asset")), outcome);
      outcomes.add(outcome);
      eventBus.publish("ResponseActionExecuted", Map.of("incidentId", incidentId, "actionId", actionId, "actionType", actionType, "success", outcome.success()));
    }
    repository.transitionIncident(incidentId, "contain", "automated response facade");
    return Map.of("incidentId", incidentId, "strategy", strategy.name(), "outcomes", outcomes);
  }
}
