package dev.sentinel.incident;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import dev.sentinel.matching.IncidentMatcher;
import dev.sentinel.resolution.ResolutionService;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/incidents")
@RequiredArgsConstructor
public class IncidentController {

    private final IncidentService service;
    private final IncidentMatcher matcher;
    private final ResolutionService resolutionService;

    public record CreateIncidentRequest(
            String externalId,
            @NotBlank String title,
            String description,
            @NotBlank String serviceName,
            @NotNull Severity severity,
            @NotNull Instant occurredAt
    ) {}

    public record IncidentResponse(
            Long id,
            String externalId,
            String title,
            String description,
            String serviceName,
            Severity severity,
            IncidentStatus status,
            Instant occurredAt,
            Instant resolvedAt
    ) {
        static IncidentResponse from(Incident i) {
            return new IncidentResponse(
                    i.getId(), i.getExternalId(), i.getTitle(), i.getDescription(),
                    i.getServiceName(), i.getSeverity(), i.getStatus(),
                    i.getOccurredAt(), i.getResolvedAt());
        }
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public IncidentResponse create(@Valid @RequestBody CreateIncidentRequest req) {
        Incident incident = new Incident();
        incident.setExternalId(req.externalId());
        incident.setTitle(req.title());
        incident.setDescription(req.description());
        incident.setServiceName(req.serviceName());
        incident.setSeverity(req.severity());
        incident.setOccurredAt(req.occurredAt());
        return IncidentResponse.from(service.record(incident));
    }

    @GetMapping("/{id}")
    public IncidentResponse get(@PathVariable Long id) {
        return IncidentResponse.from(service.get(id));
    }

    @GetMapping
    public List<IncidentResponse> list() {
        return service.listAll().stream().map(IncidentResponse::from).toList();
    }

    @PostMapping("/{id}/acknowledge")
    public IncidentResponse acknowledge(@PathVariable Long id) {
        return IncidentResponse.from(service.acknowledge(id));
    }

    @PostMapping("/{id}/resolve")
    public IncidentResponse resolve(@PathVariable Long id) {
        return IncidentResponse.from(service.resolve(id));
    }

    public record SimilarIncidentResponse(
            Long id,
            String title,
            String description,
            double textScore,
            double semanticScore,
            double combinedScore,
            List<ResolutionService.ResolutionSummary> priorResolutions
    ) {}

    @GetMapping("/{id}/similar")
    public List<SimilarIncidentResponse> similar(@PathVariable Long id) {
        Incident target = service.get(id);
        return matcher.findSimilar(target, 5).stream()
                .map(m -> new SimilarIncidentResponse(
                        m.incident().getId(),
                        m.incident().getTitle(),
                        m.incident().getDescription(),
                        m.textScore(),
                        m.semanticScore(),
                        m.combinedScore(),
                        resolutionService.summariesFor(m.incident().getId())))
                .toList();
    }
}