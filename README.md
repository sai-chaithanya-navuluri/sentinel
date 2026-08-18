# Sentinel — Incident Intelligence Platform

An incident-management system that recognizes recurring problems — even when
worded differently — and surfaces what fixed them last time.

## The problem

The same production issues recur, but the knowledge of how they were fixed
often lives only in one engineer's memory or an old chat thread. Every
recurrence gets re-investigated from scratch.

## What it does

- **Captures** incidents from webhooks (Prometheus-compatible) or a REST API
- **Recognizes recurrence** using two complementary techniques:
    - Text similarity (Jaccard token overlap) — catches near-identical rewording
    - Semantic embeddings (local, offline ONNX model) — catches paraphrased
      duplicates that text matching misses (measured: ~0.08 → ~0.29 similarity
      on a real paraphrase pair — see `docs/` for the full comparison)
- **Surfaces prior resolutions** — when a match is found, shows what actually
  fixed it, by whom, and how long it took
- **Flags chronic issues automatically** — a scheduled job detects when a
  problem class crosses a recurrence threshold and needs a permanent fix,
  not another patch
- **Publishes events to Kafka** on incident creation, decoupling ingestion
  from any downstream processing
- **Optionally synthesizes a root-cause suggestion** via Claude — grounded
  strictly in retrieved historical resolutions, refusing to speculate when
  the data doesn't support a conclusion. Fully functional with this disabled;
  zero cost by default.
- **Exposes Prometheus metrics** for matching latency, detection runs, and
  LLM call outcomes

## Architecture

Java 21, Spring Boot 3.5, PostgreSQL (with `pgvector`), Kafka (KRaft mode,
no ZooKeeper), local ONNX embeddings via Deep Java Library — no external AI
API required for the core system.

## Running it

```bash
docker compose up --build
```

Everything — the app, Postgres, and Kafka — starts together. First run will
be slower (Maven build + ONNX model download inside the container).

Verify: `curl http://localhost:8080/actuator/health`

### Optional: LLM-assisted suggestions

Disabled by default. To enable, set before starting:
```bash
export SENTINEL_LLM_ENABLED=true
export ANTHROPIC_API_KEY=your-key
```

### Optional: admin endpoint protection

```bash
export SENTINEL_ADMIN_API_KEY=your-chosen-key
```
Unset by default (open for local development).

## Key design decisions

- **Text matching before embeddings** — built and measured the simpler
  approach first, to have real evidence for where semantic matching was
  actually needed, rather than adding it by default
- **KRaft over ZooKeeper** — one fewer moving part for the same guarantees
- **SQLAlchemy... wait, not applicable — JPA/Hibernate with explicit
  `Long` foreign keys instead of `@ManyToOne`** where full relationship
  loading isn't needed, avoiding lazy-loading pitfalls
- **Graceful degradation at every external boundary** — Prometheus payload
  parsing, embedding computation, LLM calls, Kafka publishing — all fail
  soft, never take down the primary request
- **Chronic-issue status is preserved across re-detection runs** — a human
  acknowledging an issue isn't silently overwritten by the next scheduled scan

## What I'd build next

- Persist embeddings (schema is ready — `pgvector` column exists) instead of
  recomputing per request
- Semantic-similarity-based chronic-issue grouping, not just title-signature
  matching
- A minimal frontend for the approval/acknowledgment workflows currently
  only exposed via API