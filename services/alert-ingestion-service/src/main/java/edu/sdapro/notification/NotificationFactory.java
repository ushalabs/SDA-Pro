package edu.sdapro.notification;

// PATTERN: Abstract Factory
// RATIONALE: Notification channels are created as compatible families for basic and enterprise SOC deployments.
public interface NotificationFactory {
  Notifier createEmailNotifier();
  Notifier createSlackNotifier();
  Notifier createPagerDutyNotifier();
}
