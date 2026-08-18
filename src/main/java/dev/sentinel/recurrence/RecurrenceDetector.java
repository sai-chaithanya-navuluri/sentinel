package dev.sentinel.recurrence;

import dev.sentinel.incident.Incident;
import dev.sentinel.incident.IncidentRepository;
import dev.sentinel.matching.TextNormalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecurrenceDetector {

    private final IncidentRepository incidentRepository;
    private final ChronicIssueRepository chronicIssueRepository;
    private final TextNormalizer normalizer;

    @Value("${sentinel.recurrence.threshold-count:3}")
    private int thresholdCount;

    @Value("${sentinel.recurrence.lookback-days:30}")
    private int lookbackDays;

    /**
     * Runs automatically every hour. Also, callable directly (see
     * RecurrenceController) for on-demand detection and testing without
     * waiting for the schedule.
     */
    @Scheduled(fixedDelay = 3_600_000) // 1 hour, in milliseconds
    @Transactional
    public DetectionResult runDetection() {
        Instant cutoff = Instant.now().minus(lookbackDays, ChronoUnit.DAYS);
        List<Incident> recent = incidentRepository.findAll().stream()
                .filter(i -> i.getOccurredAt().isAfter(cutoff))
                .toList();

        Map<GroupKey, List<Incident>> grouped = recent.stream()
                .collect(Collectors.groupingBy(this::signatureFor));

        int flagged = 0;
        for (Map.Entry<GroupKey, List<Incident>> entry : grouped.entrySet()) {
            List<Incident> group = entry.getValue();
            if (group.size() >= thresholdCount) {
                upsertChronicIssue(entry.getKey(), group);
                flagged++;
            }
        }
        return new DetectionResult(recent.size(), grouped.size(), flagged);
    }

    private GroupKey signatureFor(Incident incident) {
        Set<String> tokens = normalizer.tokenize(incident.getTitle());
        String signature = tokens.stream().sorted().collect(Collectors.joining(" "));
        return new GroupKey(incident.getServiceName(), signature);
    }

    private void upsertChronicIssue(GroupKey key, List<Incident> group) {
        Instant first = group.stream().map(Incident::getOccurredAt).min(Instant::compareTo).orElseThrow();
        Instant last = group.stream().map(Incident::getOccurredAt).max(Instant::compareTo).orElseThrow();
        String representativeTitle = group.getFirst().getTitle();

        ChronicIssue issue = chronicIssueRepository
                .findByServiceNameAndTitleSignature(key.serviceName(), key.signature())
                .orElseGet(ChronicIssue::new);

        issue.setServiceName(key.serviceName());
        issue.setTitleSignature(key.signature());
        issue.setRepresentativeTitle(representativeTitle);
        issue.setOccurrenceCount(group.size());
        issue.setFirstOccurrenceAt(first);
        issue.setLastOccurrenceAt(last);
        // Status is intentionally NOT reset here — if a human already
        // acknowledged or resolved this chronic issue, a re-detection run
        // should update the count/dates without silently reopening it.
        chronicIssueRepository.save(issue);
    }

    private record GroupKey(String serviceName, String signature) {}

    public record DetectionResult(int incidentsScanned, int groupsFound, int chronicIssuesFlagged) {}
}