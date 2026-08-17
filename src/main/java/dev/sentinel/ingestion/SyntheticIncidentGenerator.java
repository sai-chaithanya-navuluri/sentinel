package dev.sentinel.ingestion;

import dev.sentinel.incident.Incident;
import dev.sentinel.incident.IncidentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SyntheticIncidentGenerator {

    private final IncidentService incidentService;
    private final Random random = new Random();

    private static final List<IncidentTemplate> TEMPLATES = List.of(
            new IncidentTemplate(
                    "Database connection timeout",
                    List.of(
                            "payment-service could not acquire connection after 30s",
                            "connection pool exhausted during peak batch overlap",
                            "DB connection timeout observed on primary replica"
                    ),
                    "payment-service",
                    dev.sentinel.incident.Severity.CRITICAL
            ),
            new IncidentTemplate(
                    "Elevated API latency",
                    List.of(
                            "p99 latency exceeded 2000ms on /api/orders",
                            "response times degraded following deploy",
                            "latency spike correlated with increased traffic"
                    ),
                    "orders-service",
                    dev.sentinel.incident.Severity.HIGH
            ),
            new IncidentTemplate(
                    "Memory usage critical",
                    List.of(
                            "JVM heap usage exceeded 90% threshold",
                            "OutOfMemoryError observed in application logs",
                            "memory pressure triggered GC pauses"
                    ),
                    "auth-service",
                    dev.sentinel.incident.Severity.HIGH
            ),
            new IncidentTemplate(
                    "Disk space low",
                    List.of(
                            "log partition at 95% capacity",
                            "disk usage alert on /var/log",
                            "insufficient disk space for temp files"
                    ),
                    "notification-service",
                    dev.sentinel.incident.Severity.MEDIUM
            )
    );

    /**
     * Generates {count} incidents spread across the last {daysBack} days,
     * drawn from the recurring templates above with realistic variation.
     */
    public void generateHistory(int count, int daysBack) {
        for (int i = 0; i < count; i++) {
            IncidentTemplate template = TEMPLATES.get(random.nextInt(TEMPLATES.size()));
            String description = template.descriptionVariants()
                    .get(random.nextInt(template.descriptionVariants().size()));

            Instant occurredAt = Instant.now()
                    .minus(random.nextInt(daysBack * 24), ChronoUnit.HOURS);

            Incident incident = new Incident();
            incident.setExternalId("SYN-" + UUID.randomUUID());
            incident.setTitle(template.titlePattern());
            incident.setDescription(description);
            incident.setServiceName(template.serviceName());
            incident.setSeverity(template.severity());
            incident.setOccurredAt(occurredAt);

            incidentService.record(incident);
        }
    }
}