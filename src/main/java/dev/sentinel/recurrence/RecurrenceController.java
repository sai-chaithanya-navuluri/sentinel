package dev.sentinel.recurrence;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class RecurrenceController {

    private final RecurrenceDetector detector;
    private final ChronicIssueRepository repository;

    public record ChronicIssueResponse(
            Long id, String serviceName, String representativeTitle,
            int occurrenceCount, Instant firstOccurrenceAt, Instant lastOccurrenceAt,
            ChronicIssueStatus status
    ) {
        static ChronicIssueResponse from(ChronicIssue c) {
            return new ChronicIssueResponse(c.getId(), c.getServiceName(), c.getRepresentativeTitle(),
                    c.getOccurrenceCount(), c.getFirstOccurrenceAt(), c.getLastOccurrenceAt(), c.getStatus());
        }
    }

    @GetMapping("/api/chronic-issues")
    public List<ChronicIssueResponse> list() {
        return repository.findByStatusOrderByOccurrenceCountDesc(ChronicIssueStatus.OPEN).stream()
                .map(ChronicIssueResponse::from)
                .toList();
    }

    /** Manual trigger — lets us verify detection immediately rather than
     * waiting up to an hour for the scheduled run. */
    @PostMapping("/admin/detect-recurrence")
    public RecurrenceDetector.DetectionResult detectNow() {
        return detector.runDetection();
    }
}