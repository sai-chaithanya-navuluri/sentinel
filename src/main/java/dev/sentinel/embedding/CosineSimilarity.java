package dev.sentinel.embedding;

import org.springframework.stereotype.Component;

@Component
public class CosineSimilarity {

    /**
     * Cosine similarity between two vectors: 1.0 = identical direction (same
     * meaning), 0.0 = unrelated, -1.0 = opposite. Unlike Jaccard, this
     * captures semantic closeness rather than exact word overlap.
     */
    public double similarity(float[] a, float[] b) {
        if (a.length != b.length) {
            throw new IllegalArgumentException("Vectors must be the same length");
        }

        double dot = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }

        if (normA == 0.0 || normB == 0.0) return 0.0;
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}