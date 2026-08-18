package dev.sentinel.matching;

import dev.sentinel.embedding.CosineSimilarity;
import dev.sentinel.embedding.EmbeddingService;
import dev.sentinel.incident.Incident;
import dev.sentinel.incident.IncidentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import io.micrometer.core.instrument.MeterRegistry;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class IncidentMatcher {

    private final IncidentRepository repository;
    private final TextNormalizer normalizer;
    private final SimilarityScorer textScorer;
    private final EmbeddingService embeddingService;
    private final CosineSimilarity semanticScorer;
    private final MeterRegistry meterRegistry;

    private static final double COMBINED_THRESHOLD = 0.20;

    // Semantic weighted higher: Part 3/4's own measurements showed text
    // overlap missing paraphrased duplicates that embeddings correctly
    // surface as related (see IncidentMatchingTest for the documented case).
    private static final double TEXT_WEIGHT = 0.4;
    private static final double SEMANTIC_WEIGHT = 0.6;

    public List<IncidentMatch> findSimilar(Incident target, int limit) {
        return io.micrometer.core.instrument.Timer.builder("sentinel.matching.duration")
                .description("Time to find similar incidents, including embedding computation")
                .tag("service", target.getServiceName())
                .register(meterRegistry)
                .record(() -> findSimilarInternal(target, limit));
    }

    private List<IncidentMatch> findSimilarInternal(Incident target, int limit) {
        String targetText = target.getTitle() + " " + target.getDescription();
        Set<String> targetTokens = normalizer.tokenize(targetText);

        float[] targetEmbedding = null;
        try {
            targetEmbedding = embeddingService.embed(targetText);
        } catch (Exception e) {
            // Falls through to text-only matching for this request.
        }
        final float[] finalTargetEmbedding = targetEmbedding;   // effectively-final for the lambda below

        return repository.findByServiceNameOrderByOccurredAtDesc(target.getServiceName())
                .stream()
                .filter(candidate -> !candidate.getId().equals(target.getId()))
                .map(candidate -> score(candidate, targetTokens, finalTargetEmbedding))
                .filter(match -> match.combinedScore() >= COMBINED_THRESHOLD)
                .sorted(Comparator.comparingDouble(IncidentMatch::combinedScore).reversed())
                .limit(limit)
                .toList();
    }

    private IncidentMatch score(Incident candidate, Set<String> targetTokens, float[] targetEmbedding) {
        String candidateText = candidate.getTitle() + " " + candidate.getDescription();

        Set<String> candidateTokens = normalizer.tokenize(candidateText);
        double text = textScorer.similarity(targetTokens, candidateTokens);

        double semantic = 0.0;
        if (targetEmbedding != null) {
            try {
                float[] candidateEmbedding = embeddingService.embed(candidateText);
                semantic = semanticScorer.similarity(targetEmbedding, candidateEmbedding);
            } catch (Exception e) {
                // Embedding failure degrades to text-only scoring for this candidate
                // rather than failing the whole match request — same boundary
                // principle as PrometheusAlertAdapter's graceful severity fallback.
            }
        }

        double combined = (text * TEXT_WEIGHT) + (semantic * SEMANTIC_WEIGHT);
        return new IncidentMatch(candidate, text, semantic, combined);
    }
}