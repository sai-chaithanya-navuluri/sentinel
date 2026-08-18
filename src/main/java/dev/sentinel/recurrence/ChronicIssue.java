package dev.sentinel.recurrence;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "chronic_issues")
@Getter @Setter
@NoArgsConstructor
public class ChronicIssue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "service_name", nullable = false, length = 120)
    private String serviceName;

    @Column(name = "title_signature", nullable = false, length = 300)
    private String titleSignature;

    @Column(name = "representative_title", nullable = false, length = 300)
    private String representativeTitle;

    @Column(name = "occurrence_count", nullable = false)
    private int occurrenceCount;

    @Column(name = "first_occurrence_at", nullable = false)
    private Instant firstOccurrenceAt;

    @Column(name = "last_occurrence_at", nullable = false)
    private Instant lastOccurrenceAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ChronicIssueStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
        if (status == null) status = ChronicIssueStatus.OPEN;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}