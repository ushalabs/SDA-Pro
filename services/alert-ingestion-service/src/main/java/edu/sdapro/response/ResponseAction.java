package edu.sdapro.response;

public interface ResponseAction {
  String type();
  ActionOutcome execute(String target);
  default ActionOutcome rollback() { return new ActionOutcome(type(), false, "No rollback available"); }
}
