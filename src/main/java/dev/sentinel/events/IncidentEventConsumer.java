package dev.sentinel.events;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class IncidentEventConsumer {

    @KafkaListener(topics = "incident-events", groupId = "sentinel-incident-processor")
    public void onIncidentCreated(IncidentCreatedEvent event) {
        // This is where independent, asynchronous reactions to new incidents
        // live — deliberately kept minimal here. A real system might trigger
        // notification dispatch, external system sync, or ML re-scoring.
        // Processing must be idempotent: Kafka's at-least-once delivery means
        // this method can genuinely be called more than once for the same event.
        log.info("Processed incident-created event: incident={} service={} title='{}' severity={}",
                event.incidentId(), event.serviceName(), event.title(), event.severity());
    }
}