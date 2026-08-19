package dev.sentinel.recurrence;

import dev.sentinel.AbstractIntegrationTest;
import dev.sentinel.incident.Incident;
import dev.sentinel.incident.IncidentRepository;
import dev.sentinel.incident.Severity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

class RecurrenceDetectorTest extends AbstractIntegrationTest {

    @Autowired IncidentRepository incidentRepository;
    @Autowired ChronicIssueRepository chronicIssueRepository;
    @Autowired RecurrenceDetector detector;

    @BeforeEach
    void cleanDatabase() {
        chronicIssueRepository.deleteAll();
        incidentRepository.deleteAll();
    }

    private void seed(String title, String service, int count) {
        for (int i = 0; i < count; i++) {
            Incident inc = new Incident();
            inc.setExternalId("SEED-" + java.util.UUID.randomUUID());
            inc.setTitle(title);
            inc.setServiceName(service);
            inc.setSeverity(Severity.HIGH);
            inc.setOccurredAt(Instant.now().minus(i, ChronoUnit.HOURS));
            incidentRepository.save(inc);
        }
    }

    @Test
    void flags_a_pattern_that_crosses_the_threshold() {
        seed("Connection timeout", "payment-service", 5);

        RecurrenceDetector.DetectionResult result = detector.runDetection();

        assertThat(result.chronicIssuesFlagged()).isEqualTo(1);
        var issues = chronicIssueRepository.findByStatusOrderByOccurrenceCountDesc(ChronicIssueStatus.OPEN);
        assertThat(issues).hasSize(1);
        assertThat(issues.getFirst().getOccurrenceCount()).isEqualTo(5);
    }

    @Test
    void does_not_flag_a_pattern_below_the_threshold() {
        seed("One-off issue", "payment-service", 2); // default threshold is 3

        RecurrenceDetector.DetectionResult result = detector.runDetection();

        assertThat(result.chronicIssuesFlagged()).isZero();
    }

    @Test
    void same_title_in_different_services_are_tracked_separately() {
        seed("Connection timeout", "payment-service", 3);
        seed("Connection timeout", "auth-service", 3);

        detector.runDetection();

        var issues = chronicIssueRepository.findByStatusOrderByOccurrenceCountDesc(ChronicIssueStatus.OPEN);
        assertThat(issues).hasSize(2);
        assertThat(issues).extracting(ChronicIssue::getServiceName)
                .containsExactlyInAnyOrder("payment-service", "auth-service");
    }

    @Test
    void re_detection_does_not_reset_a_human_acknowledged_status() {
        seed("Connection timeout", "payment-service", 5);
        detector.runDetection();

        var issue = chronicIssueRepository.findByStatusOrderByOccurrenceCountDesc(ChronicIssueStatus.OPEN).getFirst();
        issue.setStatus(ChronicIssueStatus.ACKNOWLEDGED);
        chronicIssueRepository.save(issue);

        seed("Connection timeout", "payment-service", 1); // one more occurrence
        detector.runDetection();

        var updated = chronicIssueRepository.findById(issue.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(ChronicIssueStatus.ACKNOWLEDGED);
        assertThat(updated.getOccurrenceCount()).isEqualTo(6); // count updated
    }
}