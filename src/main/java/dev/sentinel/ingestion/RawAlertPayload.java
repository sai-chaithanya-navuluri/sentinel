package dev.sentinel.ingestion;

import dev.sentinel.incident.Severity;
import java.time.Instant;

public record RawAlertPayload(
        String externalId,
        String title,
        String description,
        String serviceName,
        Severity severity,
        Instant occurredAt
) {}