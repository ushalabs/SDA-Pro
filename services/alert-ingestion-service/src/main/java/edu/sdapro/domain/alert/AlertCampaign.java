package edu.sdapro.domain.alert;

import edu.sdapro.domain.Severity;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class AlertCampaign implements AlertComponent {
  private final UUID id = UUID.randomUUID();
  private final String name;
  private final List<AlertComponent> children = new ArrayList<>();
  public AlertCampaign(String name) { this.name = name; }
  public UUID getId() { return id; }
  public Severity getSeverity() { return children.stream().map(AlertComponent::getSeverity).max(Comparator.comparingInt(Enum::ordinal)).orElse(Severity.LOW); }
  public String describe() { return name + " containing " + children.size() + " alert(s)"; }
  public void add(AlertComponent component) { children.add(component); }
  public void remove(AlertComponent component) { children.remove(component); }
  public List<AlertComponent> getChildren() { return List.copyOf(children); }
}
