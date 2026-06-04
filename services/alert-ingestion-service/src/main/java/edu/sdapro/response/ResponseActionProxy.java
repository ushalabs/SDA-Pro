package edu.sdapro.response;

// PATTERN: Proxy
// RATIONALE: Response actions need authorization checks before reaching mock external systems.
class ResponseActionProxy implements ResponseAction {
  private final ResponseAction action;
  ResponseActionProxy(ResponseAction action) { this.action = action; }
  public String type() { return action.type(); }
  public ActionOutcome execute(String target) {
    if (target == null || target.isBlank()) return new ActionOutcome(type(), false, "Rejected: missing target");
    return action.execute(target);
  }
}
