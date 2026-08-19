# Sentinel Dashboard

The web interface for [Sentinel](../README.md) — a production intelligence
dashboard that surfaces recurring incidents, resolution history, and
fix-priority scoring for engineering teams.

Built with React, TypeScript, Tailwind CSS, and Vite.

## What it does

- **Live incident feed** — real-time view of open incidents with severity,
  service, and recency
- **Chronic issue detection** — surfaces patterns that have crossed the
  recurrence threshold, with a computed Fix Priority score
- **Incident detail view** — shows semantic/text similarity matches from the
  backend matcher, prior recorded resolutions, and (when enabled) a grounded
  LLM-generated root-cause suggestion
- **Fix Queue** — chronic issues sorted by priority, frequency, or severity
- **Root Causes** — chronic issues grouped by keyword-based category
- **Command palette** (⌘K / Ctrl+K) — search incidents, services, and
  chronic issues from anywhere

## Data honesty

Every number shown is either fetched directly from the backend or derived
client-side from real fetched data — see `src/lib/deriveMetrics.ts` and
`src/lib/fixPriority.ts`. Nothing is fabricated. Where the backend doesn't
yet track something (uptime percentages, deployment correlation, ticket
ownership), the UI either omits it or clearly labels it as not yet available.

## Fix Priority scoring

`src/lib/fixPriority.ts` computes a 0–100 urgency score from frequency,
severity mix, recency, and recurrence span. Weights are named constants at
the top of the file — safe to retune without touching any component.

## Running locally

Requires the Sentinel backend running on `localhost:8080` (see the
[main README](../README.md) for setup — `docker compose up` from the repo
root brings up the full stack including this UI's build target).

```bash
npm install
npm run dev
```

Opens on `localhost:5173`, proxying `/api` and `/actuator` requests to the
backend.

## Build

```bash
npm run build
```

Runs TypeScript type-checking followed by a production Vite build. This is
the same command CI runs on every push.

## What's not built yet

- "Create Fix" is a labeled placeholder — there's no backend `Fix` entity or
  ownership-tracking yet
- Service health status is derived from open/critical incident counts, not
  real uptime data
- Root-cause grouping is keyword-based, not a trained classifier