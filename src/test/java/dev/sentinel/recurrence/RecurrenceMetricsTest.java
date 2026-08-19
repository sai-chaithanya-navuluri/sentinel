package dev.sentinel.recurrence;

import dev.sentinel.AbstractIntegrationTest;
import dev.sentinel.incident.Incident;
import dev.sentinel.incident.IncidentRepository;
import dev.sentinel.incident.Severity;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

class RecurrenceMetricsTest extends AbstractIntegrationTest {

    @Autowired IncidentRepository incidentRepository;
    @Autowired ChronicIssueRepository chronicIssueRepository;
    @Autowired RecurrenceDetector detector;
    @Autowired MeterRegistry meterRegistry;

    @BeforeEach
    void cleanDatabase() {
        chronicIssueRepository.deleteAll();
        incidentRepository.deleteAll();
    }

    @Test
    void runDetection_updates_the_scanned_gauge_and_increments_the_flagged_counter() {
        for (int i = 0; i < 4; i++) {
            Incident inc = new Incident();
            inc.setExternalId("RM-" + java.util.UUID.randomUUID());
            inc.setTitle("Recurring failure");
            inc.setServiceName("orders-service");
            inc.setSeverity(Severity.HIGH);
            inc.setOccurredAt(Instant.now());
            incidentRepository.save(inc);
        }

        double before = meterRegistry.find("sentinel.chronic_issues.flagged").counter() != null
                ? Objects.requireNonNull(meterRegistry.find("sentinel.chronic_issues.flagged").counter()).count() : 0;

        detector.runDetection();

        double after = Objects.requireNonNull(meterRegistry.find("sentinel.chronic_issues.flagged").counter()).count();
        assertThat(after).isGreaterThan(before);

        Double scanned = Objects.requireNonNull(meterRegistry.find("sentinel.incidents.scanned_last_run").gauge()).value();
        assertThat(scanned).isEqualTo(4.0);
    }
}