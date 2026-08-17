package dev.sentinel.ingestion;

import dev.sentinel.incident.Severity;
import java.util.List;

public record IncidentTemplate(
        String titlePattern,
        List<String> descriptionVariants,
        String serviceName,
        Severity severity
) {}