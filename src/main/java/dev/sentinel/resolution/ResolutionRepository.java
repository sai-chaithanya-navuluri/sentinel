package dev.sentinel.resolution;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResolutionRepository extends JpaRepository<Resolution, Long> {

    List<Resolution> findByIncidentIdOrderByCreatedAtDesc(Long incidentId);
}