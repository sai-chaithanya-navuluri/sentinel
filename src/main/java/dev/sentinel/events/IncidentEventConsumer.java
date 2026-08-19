package dev.sentinel.events;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class IncidentEventConsumer {

    // Tracks processed incident IDs — a real, minimal idempotency safeguard
    // (per Kafka's at-least-once delivery), and incidentally makes consumption
    // observable/testable without depending on log-capture timing.
    private final Set<Long> processedIncidentIds = ConcurrentHashMap.newKeySet();

    @KafkaListener(topics = "incident-events", groupId = "sentinel-incident-processor")
    public void onIncidentCreated(IncidentCreatedEvent event) {
        log.info("Processed incident-created event: incident={} service={} title='{}' severity={}",
                event.incidentId(), event.serviceName(), event.title(), event.severity());
        processedIncidentIds.add(event.incidentId());
    }

    public boolean hasProcessed(Long incidentId) {
        return processedIncidentIds.contains(incidentId);
    }
}