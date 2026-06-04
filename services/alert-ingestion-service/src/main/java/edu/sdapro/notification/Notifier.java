package edu.sdapro.notification;

public interface Notifier {
  String send(String recipient, String message);
}
