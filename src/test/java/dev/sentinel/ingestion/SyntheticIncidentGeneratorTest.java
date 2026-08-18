package dev.sentinel.ingestion;

import dev.sentinel.incident.IncidentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

class SyntheticIncidentGeneratorTest extends dev.sentinel.AbstractIntegrationTest{

    @Autowired SyntheticIncidentGenerator generator;
    @Autowired IncidentRepository repository;

    @BeforeEach
    void cleanDatabase() {
        repository.deleteAll();
    }

    @Test
    void generates_the_requested_count() {
        generator.generateHistory(20, 10);
        assertThat(repository.count()).isEqualTo(20);
    }

    @Test
    void spreads_incidents_within_the_requested_day_range() {
        generator.generateHistory(30, 5);
        Instant cutoff = Instant.now().minus(5, ChronoUnit.DAYS).minus(1, ChronoUnit.HOURS);
        boolean allWithinRange = repository.findAll().stream()
                .allMatch(i -> i.getOccurredAt().isAfter(cutoff));
        assertThat(allWithinRange).isTrue();
    }

    @Test
    void generated_incidents_use_synthetic_prefix_for_easy_identification() {
        generator.generateHistory(10, 5);
        boolean allPrefixed = repository.findAll().stream()
                .allMatch(i -> i.getExternalId().startsWith("SYN-"));
        assertThat(allPrefixed).isTrue();
    }
}