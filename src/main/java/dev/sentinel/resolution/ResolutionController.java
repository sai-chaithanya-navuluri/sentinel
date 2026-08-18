package dev.sentinel.resolution;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/incidents/{incidentId}/resolutions")
@RequiredArgsConstructor
public class ResolutionController {

    private final ResolutionService service;

    public record RecordResolutionRequest(
            @NotBlank String summary,
            String resolvedBy,
            Integer timeToResolveMinutes,
            Boolean successful
    ) {}

    public record ResolutionResponse(
            Long id, String summary, String resolvedBy,
            Integer timeToResolveMinutes, boolean successful, Instant createdAt
    ) {
        static ResolutionResponse from(Resolution r) {
            return new ResolutionResponse(r.getId(), r.getSummary(), r.getResolvedBy(),
                    r.getTimeToResolveMinutes(), r.isSuccessful(), r.getCreatedAt());
        }
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResolutionResponse record(@PathVariable Long incidentId,
                                     @Valid @RequestBody RecordResolutionRequest req) {
        boolean successful = req.successful() == null || req.successful();
        Resolution saved = service.record(incidentId, req.summary(), req.resolvedBy(),
                req.timeToResolveMinutes(), successful);
        return ResolutionResponse.from(saved);
    }

    @GetMapping
    public List<ResolutionResponse> list(@PathVariable Long incidentId) {
        return service.findForIncident(incidentId).stream()
                .map(ResolutionResponse::from)
                .toList();
    }
}