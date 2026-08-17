package dev.sentinel.ingestion;

import dev.sentinel.incident.Severity;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class PrometheusAlertAdapterTest {

    private final PrometheusAlertAdapter adapter = new PrometheusAlertAdapter();

    private PrometheusAlertPayload payload(String severity, String startsAt) {
        return new PrometheusAlertPayload(
                "firing",
                Map.of("alertname", "HighLatency", "service", "orders-service", "severity", severity),
                Map.of("description", "p99 latency exceeded threshold"),
                startsAt,
                "fp-12345"
        );
    }

    @Test
    void maps_critical_severity_correctly() {
        RawAlertPayload result = adapter.normalize(payload("critical", "2026-08-17T10:00:00Z"));
        assertThat(result.severity()).isEqualTo(Severity.CRITICAL);
    }

    @Test
    void maps_page_severity_to_critical() {
        RawAlertPayload result = adapter.normalize(payload("page", "2026-08-17T10:00:00Z"));
        assertThat(result.severity()).isEqualTo(Severity.CRITICAL);
    }

    @Test
    void maps_warning_severity_to_high() {
        RawAlertPayload result = adapter.normalize(payload("warning", "2026-08-17T10:00:00Z"));
        assertThat(result.severity()).isEqualTo(Severity.HIGH);
    }

    @Test
    void unrecognized_severity_degrades_to_medium_rather_than_failing() {
        RawAlertPayload result = adapter.normalize(payload("some-future-severity-level", "2026-08-17T10:00:00Z"));
        assertThat(result.severity()).isEqualTo(Severity.MEDIUM);
    }

    @Test
    void null_severity_degrades_to_medium() {
        PrometheusAlertPayload noSeverity = new PrometheusAlertPayload(
                "firing",
                Map.of("alertname", "SomeAlert", "service", "orders-service"),  // no "severity" key
                Map.of("description", "something happened"),
                "2026-08-17T10:00:00Z",
                "fp-999"
        );
        RawAlertPayload result = adapter.normalize(noSeverity);
        assertThat(result.severity()).isEqualTo(Severity.MEDIUM);
    }

    @Test
    void fingerprint_becomes_external_id_for_idempotency() {
        RawAlertPayload result = adapter.normalize(payload("critical", "2026-08-17T10:00:00Z"));
        assertThat(result.externalId()).isEqualTo("fp-12345");
    }

    @Test
    void malformed_timestamp_falls_back_to_now_rather_than_failing() {
        RawAlertPayload result = adapter.normalize(payload("critical", "not-a-real-timestamp"));
        assertThat(result.occurredAt()).isNotNull();
        // Should be very close to "now" since it fell back
        assertThat(result.occurredAt()).isCloseTo(Instant.now(), org.assertj.core.api.Assertions.within(5, java.time.temporal.ChronoUnit.SECONDS));
    }

    @Test
    void missing_annotations_fallback_from_description_to_summary() {
        PrometheusAlertPayload noDescription = new PrometheusAlertPayload(
                "firing",
                Map.of("alertname", "SomeAlert", "service", "orders-service", "severity", "critical"),
                Map.of("summary", "brief summary only"),   // no "description" key
                "2026-08-17T10:00:00Z",
                "fp-888"
        );
        RawAlertPayload result = adapter.normalize(noDescription);
        assertThat(result.description()).isEqualTo("brief summary only");
    }
}