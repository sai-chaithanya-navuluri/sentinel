package dev.sentinel.incident;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Testcontainers
class IncidentServiceTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("pgvector/pgvector:pg16");

    @Autowired IncidentService service;

    private Incident sample(String externalId) {
        Incident i = new Incident();
        i.setExternalId(externalId);
        i.setTitle("DB connection timeout");
        i.setServiceName("payment-service");
        i.setSeverity(Severity.CRITICAL);
        i.setOccurredAt(Instant.now());
        return i;
    }

    @Test
    void records_an_incident_as_open() {
        Incident saved = service.record(sample("PD-100"));
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getStatus()).isEqualTo(IncidentStatus.OPEN);
    }

    @Test
    void same_external_id_does_not_create_a_duplicate() {
        Incident first  = service.record(sample("PD-101"));
        Incident second = service.record(sample("PD-101"));
        assertThat(second.getId()).isEqualTo(first.getId());
    }

    @Test
    void acknowledge_then_resolve_transitions_correctly() {
        Incident saved = service.record(sample("PD-102"));
        assertThat(service.acknowledge(saved.getId()).getStatus())
                .isEqualTo(IncidentStatus.ACKNOWLEDGED);
        assertThat(service.resolve(saved.getId()).getStatus())
                .isEqualTo(IncidentStatus.RESOLVED);
    }

    @Test
    void cannot_acknowledge_a_resolved_incident() {
        Incident saved = service.record(sample("PD-103"));
        service.resolve(saved.getId());
        assertThatThrownBy(() -> service.acknowledge(saved.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void unknown_id_throws_not_found() {
        assertThatThrownBy(() -> service.get(999_999L))
                .isInstanceOf(IncidentNotFoundException.class);
    }
}