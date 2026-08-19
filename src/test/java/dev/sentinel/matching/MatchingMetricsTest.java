package dev.sentinel.matching;

import dev.sentinel.AbstractIntegrationTest;
import dev.sentinel.incident.Incident;
import dev.sentinel.incident.IncidentRepository;
import dev.sentinel.incident.Severity;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class MatchingMetricsTest extends AbstractIntegrationTest {

    @Autowired IncidentRepository repository;
    @Autowired IncidentMatcher matcher;
    @Autowired MeterRegistry meterRegistry;

    @BeforeEach
    void cleanDatabase() {
        repository.deleteAll();
    }

    private Incident save(String title, String service) {
        Incident i = new Incident();
        i.setExternalId("METRIC-" + java.util.UUID.randomUUID());
        i.setTitle(title);
        i.setDescription("test description");
        i.setServiceName(service);
        i.setSeverity(Severity.HIGH);
        i.setOccurredAt(Instant.now());
        return repository.save(i);
    }

    @Test
    void findSimilar_records_a_timer_measurement_tagged_by_service() {
        save("Timeout issue", "payment-service");
        Incident target = save("Timeout issue", "payment-service");

        matcher.findSimilar(target, 5);

        Timer timer = meterRegistry.find("sentinel.matching.duration")
                .tag("service", "payment-service")
                .timer();

        assertThat(timer).isNotNull();
        assertThat(timer.count()).isGreaterThanOrEqualTo(1);
        assertThat(timer.totalTime(java.util.concurrent.TimeUnit.MILLISECONDS)).isGreaterThan(0);
    }
}