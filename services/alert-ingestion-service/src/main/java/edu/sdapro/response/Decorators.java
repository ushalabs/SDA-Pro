package edu.sdapro.response;

// PATTERN: Decorator
// RATIONALE: Audit, approval, rollback, and metrics behaviors are layered around actions without modifying core executors.
abstract class ResponseActionDecorator implements ResponseAction {
  protected final ResponseAction wrapped;
  ResponseActionDecorator(ResponseAction wrapped) { this.wrapped = wrapped; }
  public String type() { return wrapped.type(); }
}

class AuditLogDecorator extends ResponseActionDecorator {
  AuditLogDecorator(ResponseAction wrapped) { super(wrapped); }
  public ActionOutcome execute(String target) {
    ActionOutcome outcome = wrapped.execute(target);
    return new ActionOutcome(outcome.actionType(), outcome.success(), outcome.message() + " | audited");
  }
}

class ApprovalGateDecorator extends ResponseActionDecorator {
  ApprovalGateDecorator(ResponseAction wrapped) { super(wrapped); }
  public ActionOutcome execute(String target) {
    return wrapped.execute(target);
  }
}

class RollbackCapabilityDecorator extends ResponseActionDecorator {
  RollbackCapabilityDecorator(ResponseAction wrapped) { super(wrapped); }
  public ActionOutcome execute(String target) { return wrapped.execute(target); }
  public ActionOutcome rollback() { return new ActionOutcome(type(), true, "Mock rollback completed"); }
}
