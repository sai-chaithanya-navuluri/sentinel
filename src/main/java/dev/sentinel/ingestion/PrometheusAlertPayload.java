package dev.sentinel.ingestion;

import java.util.Map;

public record PrometheusAlertPayload(
        String status,              // "firing" or "resolved"
        Map<String, String> labels,      // e.g. {"alertname": "...", "service": "...", "severity": "..."}
        Map<String, String> annotations, // e.g. {"summary": "...", "description": "..."}
        String startsAt,
        String fingerprint           // Prometheus's own dedupe identifier
) {}