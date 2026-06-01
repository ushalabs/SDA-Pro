package edu.sdapro.response;

class BlockIpAction implements ResponseAction {
  public String type() { return "BLOCK_IP"; }
  public ActionOutcome execute(String target) { return new ActionOutcome(type(), true, "Mock firewall blocked " + target); }
}

class IsolateEndpointAction implements ResponseAction {
  public String type() { return "ISOLATE_ENDPOINT"; }
  public ActionOutcome execute(String target) { return new ActionOutcome(type(), true, "Mock EDR isolated " + target); }
}

class DisableUserAction implements ResponseAction {
  public String type() { return "DISABLE_USER"; }
  public ActionOutcome execute(String target) { return new ActionOutcome(type(), true, "Mock Active Directory disabled " + target); }
}

class EscalateAction implements ResponseAction {
  public String type() { return "ESCALATE"; }
  public ActionOutcome execute(String target) { return new ActionOutcome(type(), true, "Mock PagerDuty escalation opened for " + target); }
}
