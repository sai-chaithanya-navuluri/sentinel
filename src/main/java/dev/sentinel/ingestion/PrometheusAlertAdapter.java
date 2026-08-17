package dev.sentinel.ingestion;

import dev.sentinel.incident.Severity;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;

@Component
public class PrometheusAlertAdapter {

    public RawAlertPayload normalize(PrometheusAlertPayload payload) {
        Map<String, String> labels = payload.labels();
        Map<String, String> annotations = payload.annotations();

        return new RawAlertPayload(
                payload.fingerprint(),
                labels.getOrDefault("alertname", "Unknown alert"),
                annotations.getOrDefault("description", annotations.getOrDefault("summary", "")),
                labels.getOrDefault("service", "unknown-service"),
                mapSeverity(labels.get("severity")),
                parseTimestamp(payload.startsAt())
        );
    }

    private Severity mapSeverity(String prometheusSeverity) {
        if (prometheusSeverity == null) return Severity.MEDIUM;
        return switch (prometheusSeverity.toLowerCase()) {
            case "critical", "page" -> Severity.CRITICAL;
            case "warning" -> Severity.HIGH;
            case "info" -> Severity.LOW;
            default -> Severity.MEDIUM;
        };
    }

    private Instant parseTimestamp(String startsAt) {
        try {
            return Instant.parse(startsAt);
        } catch (Exception e) {
            return Instant.now();
        }
    }
}