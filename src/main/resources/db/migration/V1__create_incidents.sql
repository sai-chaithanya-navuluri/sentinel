CREATE TABLE incidents (
                           id              BIGSERIAL PRIMARY KEY,
                           external_id     VARCHAR(120) UNIQUE,
                           title           VARCHAR(300)  NOT NULL,
                           description     TEXT,
                           service_name    VARCHAR(120)  NOT NULL,
                           severity        VARCHAR(20)   NOT NULL,
                           status          VARCHAR(20)   NOT NULL,
                           occurred_at     TIMESTAMPTZ   NOT NULL,
                           acknowledged_at TIMESTAMPTZ,
                           resolved_at     TIMESTAMPTZ,
                           created_at      TIMESTAMPTZ   NOT NULL DEFAULT now(),
                           updated_at      TIMESTAMPTZ   NOT NULL DEFAULT now()
);

CREATE INDEX idx_incidents_service   ON incidents (service_name);
CREATE INDEX idx_incidents_status    ON incidents (status);
CREATE INDEX idx_incidents_occurred  ON incidents (occurred_at DESC);