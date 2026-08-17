package dev.sentinel.matching;

import dev.sentinel.incident.Incident;
import dev.sentinel.incident.IncidentRepository;
import dev.sentinel.incident.Severity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
class IncidentMatchingTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("pgvector/pgvector:pg16");

    @Autowired IncidentRepository repository;
    @Autowired IncidentMatcher matcher;
    @Autowired TextNormalizer normalizer;
    @Autowired SimilarityScorer scorer;

    @BeforeEach
    void cleanDatabase() {
        repository.deleteAll();
    }

    private Incident save(String title, String description, String service) {
        Incident i = new Incident();
        i.setExternalId("TEST-" + java.util.UUID.randomUUID());
        i.setTitle(title);
        i.setDescription(description);
        i.setServiceName(service);
        i.setSeverity(Severity.CRITICAL);
        i.setOccurredAt(Instant.now());
        return repository.save(i);
    }

    // ── TextNormalizer ──────────────────────────────────────────────────────

    @Test
    void reordered_wording_produces_identical_token_sets() {
        Set<String> a = normalizer.tokenize("Database connection timeout");
        Set<String> b = normalizer.tokenize("database timeout on connection");
        assertThat(a).isEqualTo(b);
    }

    @Test
    void numbers_are_stripped_so_varying_durations_do_not_break_matching() {
        Set<String> a = normalizer.tokenize("timeout after 30s");
        Set<String> b = normalizer.tokenize("timeout after 45s");
        assertThat(a).isEqualTo(b);
    }

    // ── SimilarityScorer: what it CAN catch ─────────────────────────────────

    @Test
    void near_identical_rewording_scores_highly() {
        Set<String> a = normalizer.tokenize("Database connection timeout");
        Set<String> b = normalizer.tokenize("database timeout on connection");
        assertThat(scorer.similarity(a, b)).isEqualTo(1.0);
    }

    // ── SimilarityScorer: documented limitation ─────────────────────────────

    @Test
    void synonym_level_paraphrasing_is_NOT_caught_by_token_overlap() {
        // Same underlying problem (connection failure), described with
        // different vocabulary. This is the known ceiling of Jaccard
        // similarity — addressed by semantic embeddings in Part 4, not here.
        Set<String> a = normalizer.tokenize("payment-service could not acquire connection after 30s");
        Set<String> b = normalizer.tokenize("connection pool exhausted during peak batch overlap");
        double score = scorer.similarity(a, b);
        assertThat(score).isLessThan(0.15);   // documents the gap, doesn't hide it
    }

    // ── IncidentMatcher: end-to-end behavior ────────────────────────────────

    @Test
    void finds_reworded_incident_in_the_same_service() {
        save("Database connection timeout", "connection pool exhausted", "payment-service");
        Incident target = save("DB connection timeout", "connection pool exhausted", "payment-service");

        List<IncidentMatch> matches = matcher.findSimilar(target, 5);

        assertThat(matches).isNotEmpty();
        assertThat(matches.getFirst().combinedScore()).isGreaterThan(0.5);
    }

    @Test
    void does_not_match_across_different_services() {
        save("Database connection timeout", "connection pool exhausted", "auth-service");
        Incident target = save("Database connection timeout", "connection pool exhausted", "payment-service");

        List<IncidentMatch> matches = matcher.findSimilar(target, 5);

        assertThat(matches).isEmpty();   // same words, different service — correctly excluded
    }

    @Test
    void unrelated_incidents_do_not_match() {
        save("Disk space low", "log partition at 95 percent", "payment-service");
        Incident target = save("Database connection timeout", "connection pool exhausted", "payment-service");

        List<IncidentMatch> matches = matcher.findSimilar(target, 5);

        assertThat(matches).isEmpty();
    }
}