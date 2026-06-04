package edu.sdapro.events;

import edu.sdapro.repository.PlatformRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// PATTERN: Observer
// RATIONALE: Dashboard, audit, notification, and metrics subscribers react to events without coupling publishers to consumers.
// PATTERN: Singleton
// RATIONALE: The Spring component acts as the single event coordination point for the demo runtime.
@Component
public class EventBusPublisher {
  private final RabbitTemplate rabbitTemplate;
  private final PlatformRepository repository;
  private final List<SseEmitter> emitters = new ArrayList<>();
  public EventBusPublisher(RabbitTemplate rabbitTemplate, PlatformRepository repository) {
    this.rabbitTemplate = rabbitTemplate; this.repository = repository;
  }
  public synchronized SseEmitter subscribe() {
    SseEmitter emitter = new SseEmitter(0L);
    emitters.add(emitter);
    emitter.onCompletion(() -> emitters.remove(emitter));
    emitter.onTimeout(() -> emitters.remove(emitter));
    return emitter;
  }
  public void publish(String eventType, Map<String, Object> payload) {
    Map<String, Object> event = Map.of("eventType", eventType, "occurredAt", Instant.now().toString(), "payload", payload);
    repository.audit(eventType, "system", "event", String.valueOf(payload.getOrDefault("id", eventType)), event.toString());
    try { rabbitTemplate.convertAndSend("sdapro.events", eventType, event); } catch (Exception ignored) {}
    for (SseEmitter emitter : List.copyOf(emitters)) {
      try { emitter.send(SseEmitter.event().name(eventType).data(event)); } catch (IOException ex) { emitters.remove(emitter); }
    }
  }
}
