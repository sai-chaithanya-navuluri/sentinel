package dev.sentinel.events;

import dev.sentinel.AbstractIntegrationTest;
import dev.sentinel.incident.Incident;
import dev.sentinel.incident.IncidentService;
import dev.sentinel.incident.Severity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

class IncidentEventFlowTest extends AbstractIntegrationTest {

    @Autowired IncidentService service;
    @Autowired IncidentEventConsumer consumer;
    @Autowired KafkaListenerEndpointRegistry kafkaListenerRegistry;

    @Test
    void creating_an_incident_publishes_an_event_the_consumer_processes() {
        // The listener uses Kafka's default "latest" offset. Wait until it
        // owns its partition before publishing, otherwise the event can be
        // produced during consumer-group startup and be skipped.
        await().atMost(15, TimeUnit.SECONDS)
                .until(() -> kafkaListenerRegistry.getListenerContainers().stream()
                        .noneMatch(container -> Objects.requireNonNull(container.getAssignedPartitions()).isEmpty()));

        Incident incident = new Incident();
        incident.setExternalId("EVT-" + UUID.randomUUID());
        incident.setTitle("Kafka flow test incident");
        incident.setServiceName("payment-service");
        incident.setSeverity(Severity.CRITICAL);
        incident.setOccurredAt(Instant.now());

        Incident saved = service.record(incident);

        await().atMost(15, TimeUnit.SECONDS)
                .untilAsserted(() -> assertThat(consumer.hasProcessed(saved.getId())).isTrue());
    }
}
