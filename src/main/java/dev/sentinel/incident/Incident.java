package dev.sentinel.incident;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "incidents")
@Getter @Setter
@NoArgsConstructor
public class Incident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_id", unique = true)
    private String externalId;

    @Column(nullable = false, length = 300)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "service_name", nullable = false, length = 120)
    private String serviceName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Severity severity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private IncidentStatus status;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Column(name = "acknowledged_at")
    private Instant acknowledgedAt;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
        if (status == null) status = IncidentStatus.OPEN;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public void acknowledge() {
        if (status != IncidentStatus.OPEN) {
            throw new IllegalStateException(
                    "Only an OPEN incident can be acknowledged; was " + status);
        }
        status = IncidentStatus.ACKNOWLEDGED;
        acknowledgedAt = Instant.now();
    }

    public void resolve() {
        if (status == IncidentStatus.RESOLVED) {
            throw new IllegalStateException("Incident is already resolved");
        }
        status = IncidentStatus.RESOLVED;
        resolvedAt = Instant.now();
    }
}