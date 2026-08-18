package dev.sentinel.resolution;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ResolutionService {

    private final ResolutionRepository repository;

    @Transactional
    public Resolution record(Long incidentId, String summary, String resolvedBy,
                             Integer timeToResolveMinutes, boolean successful) {
        Resolution resolution = new Resolution();
        resolution.setIncidentId(incidentId);
        resolution.setSummary(summary);
        resolution.setResolvedBy(resolvedBy);
        resolution.setTimeToResolveMinutes(timeToResolveMinutes);
        resolution.setSuccessful(successful);
        return repository.save(resolution);
    }

    @Transactional(readOnly = true)
    public List<Resolution> findForIncident(Long incidentId) {
        return repository.findByIncidentIdOrderByCreatedAtDesc(incidentId);
    }

    public record ResolutionSummary(String summary, String resolvedBy, Integer timeToResolveMinutes, boolean successful) {}

    @Transactional(readOnly = true)
    public List<ResolutionSummary> summariesFor(Long incidentId) {
        return findForIncident(incidentId).stream()
                .map(r -> new ResolutionSummary(r.getSummary(), r.getResolvedBy(),
                        r.getTimeToResolveMinutes(), r.isSuccessful()))
                .toList();
    }
}