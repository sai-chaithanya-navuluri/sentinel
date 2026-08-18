package dev.sentinel.resolution;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "resolutions")
@Getter @Setter
@NoArgsConstructor
public class Resolution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "incident_id", nullable = false)
    private Long incidentId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String summary;

    @Column(name = "resolved_by", length = 120)
    private String resolvedBy;

    @Column(name = "time_to_resolve_minutes")
    private Integer timeToResolveMinutes;

    @Column(nullable = false)
    private boolean successful;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
        successful = true;  // default assumption; explicitly set false if it didn't work
    }
}