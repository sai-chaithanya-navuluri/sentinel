package dev.sentinel.recurrence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChronicIssueRepository extends JpaRepository<ChronicIssue, Long> {

    Optional<ChronicIssue> findByServiceNameAndTitleSignature(String serviceName, String titleSignature);

    List<ChronicIssue> findByStatusOrderByOccurrenceCountDesc(ChronicIssueStatus status);
}