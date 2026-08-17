package dev.sentinel.matching;

import dev.sentinel.incident.Incident;

public record IncidentMatch(Incident incident, double score) {}