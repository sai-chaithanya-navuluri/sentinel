CREATE TABLE chronic_issues (
                                id                     BIGSERIAL PRIMARY KEY,
                                service_name           VARCHAR(120) NOT NULL,
                                title_signature        VARCHAR(300) NOT NULL,
                                representative_title   VARCHAR(300) NOT NULL,
                                occurrence_count       INTEGER NOT NULL,
                                first_occurrence_at    TIMESTAMPTZ NOT NULL,
                                last_occurrence_at     TIMESTAMPTZ NOT NULL,
                                status                 VARCHAR(20) NOT NULL,
                                created_at             TIMESTAMPTZ NOT NULL DEFAULT now(),
                                updated_at             TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX idx_chronic_issues_signature
    ON chronic_issues (service_name, title_signature);