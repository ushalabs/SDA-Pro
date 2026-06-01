package edu.sdapro.events;

import java.util.Map;

public interface Observer {
  void update(String eventType, Map<String, Object> payload);
}
