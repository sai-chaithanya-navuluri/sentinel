CREATE TABLE resolutions (
                             id                        BIGSERIAL PRIMARY KEY,
                             incident_id               BIGINT NOT NULL,
                             summary                   TEXT NOT NULL,
                             resolved_by               VARCHAR(120),
                             time_to_resolve_minutes   INTEGER,
                             successful                BOOLEAN NOT NULL DEFAULT true,
                             created_at                TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_resolutions_incident ON resolutions (incident_id);