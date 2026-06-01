package edu.sdapro.domain.alert;

import edu.sdapro.domain.CanonicalAlert;
import edu.sdapro.domain.Severity;
import java.util.UUID;

public class SingleAlert implements AlertComponent {
  private final CanonicalAlert alert;
  public SingleAlert(CanonicalAlert alert) { this.alert = alert; }
  public UUID getId() { return alert.id(); }
  public Severity getSeverity() { return alert.severity(); }
  public String describe() { return alert.sourceType() + " alert from " + alert.sourceIp(); }
  public CanonicalAlert canonical() { return alert; }
}
