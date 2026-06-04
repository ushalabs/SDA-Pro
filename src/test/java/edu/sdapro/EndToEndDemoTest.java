package edu.sdapro;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import static org.mockito.Mockito.mock;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
  "spring.datasource.url=jdbc:h2:mem:testdb;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE",
  "spring.datasource.driverClassName=org.h2.Driver",
  "spring.sql.init.mode=always",
  "spring.sql.init.schema-locations=classpath:/schema.sql"
})
class EndToEndDemoTest {
  @LocalServerPort int port;
  TestRestTemplate rest = new TestRestTemplate();
  @TestConfiguration static class Config { @Bean RabbitTemplate rabbitTemplate() { return mock(RabbitTemplate.class); } }

  @Test void ingestCreatesIncidentAndAuditTrail() {
    var response = rest.postForEntity("http://localhost:" + port + "/ingest/webhook/splunk", Map.of("severity", "CRITICAL"), Map.class);
    assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    assertThat(response.getBody()).containsKeys("alertId", "incidentId", "enrichment");
  }
}
