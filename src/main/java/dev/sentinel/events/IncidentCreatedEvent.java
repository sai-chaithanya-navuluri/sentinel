package dev.sentinel.events;

import java.time.Instant;

public record IncidentCreatedEvent(
        Long incidentId,
        String serviceName,
        String title,
        String severity,
        Instant occurredAt
) {}