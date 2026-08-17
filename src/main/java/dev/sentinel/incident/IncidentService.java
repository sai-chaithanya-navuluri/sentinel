package dev.sentinel.incident;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IncidentService {

    private final IncidentRepository repository;

    @Transactional
    public Incident record(Incident incident) {
        if (incident.getExternalId() != null) {
            return repository.findByExternalId(incident.getExternalId())
                    .orElseGet(() -> repository.save(incident));
        }
        return repository.save(incident);
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