package dev.sentinel.incident;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface IncidentRepository extends JpaRepository<Incident, Long> {

    Optional<Incident> findByExternalId(String externalId);

    List<Incident> findByServiceNameOrderByOccurredAtDesc(String serviceName);

    List<Incident> findByStatusOrderByOccurredAtDesc(IncidentStatus status);
}