package dev.sentinel.incident;

import dev.sentinel.events.IncidentEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IncidentService {

    private final IncidentRepository repository;
    private final IncidentEventPublisher eventPublisher;


    @Transactional
    public Incident record(Incident incident) {
        Incident saved;
        if (incident.getExternalId() != null) {
            var existing = repository.findByExternalId(incident.getExternalId());
            if (existing.isPresent()) {
                return existing.get();  // idempotent — no new event for a duplicate
            }
            saved = repository.save(incident);
        } else {
            saved = repository.save(incident);
        }
        eventPublisher.publishCreated(saved);
        return saved;
    }

    @Transactional(readOnly = true)
    public Incident get(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IncidentNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<Incident> listAll() {
        return repository.findAll();
    }

    @Transactional
    public Incident acknowledge(Long id) {
        Incident incident = get(id);
        incident.acknowledge();
        return incident;
    }

    @Transactional
    public Incident resolve(Long id) {
        Incident incident = get(id);
        incident.resolve();
        return incident;
    }
}