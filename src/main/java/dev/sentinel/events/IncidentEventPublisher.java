package dev.sentinel.events;

import dev.sentinel.incident.Incident;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class IncidentEventPublisher {

    private static final String TOPIC = "incident-events";

    private final KafkaTemplate<String, IncidentCreatedEvent> kafkaTemplate;

    public void publishCreated(Incident incident) {
        IncidentCreatedEvent event = new IncidentCreatedEvent(
                incident.getId(),
                incident.getServiceName(),
                incident.getTitle(),
                incident.getSeverity().name(),
                incident.getOccurredAt()
        );

        // Partition key = serviceName, so same-service events stay ordered
        // relative to each other (see 7.1). Publish failures are logged, not
        // thrown — event publishing is a secondary effect and must not fail
        // the actual incident-recording request (same boundary principle as
        // Part 2's graceful degradation).
        kafkaTemplate.send(TOPIC, incident.getServiceName(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.warn("Failed to publish incident-created event for incident {}: {}",
                                incident.getId(), ex.getMessage());
                    }
                });
    }
}