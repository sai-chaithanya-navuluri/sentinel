package dev.sentinel.matching;

import dev.sentinel.incident.Incident;
import dev.sentinel.incident.IncidentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class IncidentMatcher {

    private final IncidentRepository repository;
    private final TextNormalizer normalizer;
    private final SimilarityScorer scorer;

    // Below this score, two incidents are considered unrelated rather than a
    // weak match — an arbitrary-looking number, but see the accompanying test
    // for what it's actually calibrated against.
    private static final double MATCH_THRESHOLD = 0.15;

    /**
     * Finds prior incidents whose text is similar to the given incident,
     * restricted to the same service (a connection-timeout in payment-service
     * and a connection-timeout in auth-service are not "the same incident"
     * just because the words overlap).
     */
    public List<IncidentMatch> findSimilar(Incident target, int limit) {
        Set<String> targetTokens = normalizer.tokenize(
                target.getTitle() + " " + target.getDescription());

        return repository.findByServiceNameOrderByOccurredAtDesc(target.getServiceName())
                .stream()
                .filter(candidate -> !candidate.getId().equals(target.getId()))
                .map(candidate -> {
                    Set<String> candidateTokens = normalizer.tokenize(
                            candidate.getTitle() + " " + candidate.getDescription());
                    double score = scorer.similarity(targetTokens, candidateTokens);
                    return new IncidentMatch(candidate, score);
                })
                .filter(match -> match.score() >= MATCH_THRESHOLD)
                .sorted(Comparator.comparingDouble(IncidentMatch::score).reversed())
                .limit(limit)
                .toList();
    }
}