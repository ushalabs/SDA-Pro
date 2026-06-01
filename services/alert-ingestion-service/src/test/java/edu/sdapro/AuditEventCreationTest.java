package edu.sdapro;

import edu.sdapro.events.EventBusPublisher;
import edu.sdapro.repository.PlatformRepository;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import java.util.Map;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class AuditEventCreationTest {
  @Test void publishingEventCreatesAuditRecord() {
    RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
    PlatformRepository repository = mock(PlatformRepository.class);
    EventBusPublisher publisher = new EventBusPublisher(rabbitTemplate, repository);

    publisher.publish("AuditLogCreated", Map.of("id", "audit-test"));

    verify(repository).audit(anyString(), anyString(), anyString(), anyString(), anyString());
  }
}
