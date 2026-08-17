package dev.sentinel.matching;

import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class SimilarityScorer {

    /**
     * Jaccard similarity: the size of the intersection divided by the size of
     * the union. Returns 0.0 (no overlap) to 1.0 (identical token sets).
     */
    public double similarity(Set<String> a, Set<String> b) {
        if (a.isEmpty() && b.isEmpty()) {
            return 0.0;   // two empty texts are not meaningfully "similar"
        }

        Set<String> intersection = new HashSet<>(a);
        intersection.retainAll(b);

        Set<String> union = new HashSet<>(a);
        union.addAll(b);

        return (double) intersection.size() / union.size();
    }
}