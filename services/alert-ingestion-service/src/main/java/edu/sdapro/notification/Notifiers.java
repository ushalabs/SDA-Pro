package edu.sdapro.notification;

class EmailNotifier implements Notifier { public String send(String recipient, String message) { return "Mock email sent to " + recipient; } }
class SlackNotifier implements Notifier { public String send(String recipient, String message) { return "Mock Slack message sent to " + recipient; } }
class PagerDutyNotifier implements Notifier { public String send(String recipient, String message) { return "Mock PagerDuty incident sent to " + recipient; } }

class EnterpriseNotificationFactory implements NotificationFactory {
  public Notifier createEmailNotifier() { return new EmailNotifier(); }
  public Notifier createSlackNotifier() { return new SlackNotifier(); }
  public Notifier createPagerDutyNotifier() { return new PagerDutyNotifier(); }
}
