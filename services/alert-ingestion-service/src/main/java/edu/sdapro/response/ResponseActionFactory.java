package edu.sdapro.response;

// PATTERN: Factory Method
// RATIONALE: Incident response plans request action types without knowing concrete executor classes.
public class ResponseActionFactory {
  public ResponseAction createAction(String type) {
    return switch (type) {
      case "BLOCK_IP" -> new BlockIpAction();
      case "ISOLATE_ENDPOINT" -> new IsolateEndpointAction();
      case "DISABLE_USER" -> new DisableUserAction();
      case "ESCALATE" -> new EscalateAction();
      default -> new EscalateAction();
    };
  }
}
