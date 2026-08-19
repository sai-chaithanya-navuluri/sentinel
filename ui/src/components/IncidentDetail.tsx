import { useEffect, useState } from "react";
import { acknowledgeIncident, resolveIncident, fetchSimilar } from "../api";
import type { Incident, SimilarResult, ChronicIssue } from "../api";
import { SeverityBadge } from "./SeverityBadge";
import { calculateFixPriority } from "../lib/fixPriority";

const STAGES = ["OPEN", "ACKNOWLEDGED", "RESOLVED"] as const;

function elapsedMinutesSince(iso: string): number {
    return Math.max(1, Math.round((Date.now() - new Date(iso).getTime()) / 60000));
}

export function IncidentDetail({
                                   incident, allIncidents, chronicIssues, onClose, onChanged,
                               }: {
    incident: Incident;
    allIncidents: Incident[];
    chronicIssues: ChronicIssue[];
    onClose: () => void;
    onChanged: () => void;
}) {
    const [similar, setSimilar] = useState<SimilarResult | null>(null);
    const [busy, setBusy] = useState(false);
    const [showResolveForm, setShowResolveForm] = useState(false);
    const [summary, setSummary] = useState("");
    const [resolvedBy, setResolvedBy] = useState("");
    const [minutes, setMinutes] = useState("");

    useEffect(() => {
        setSimilar(null);
        fetchSimilar(incident.id).then(setSimilar).catch(() => {});
    }, [incident.id]);

    // Pre-fill elapsed time the moment the form opens, so it's a sensible
    // default the person can override rather than a blank field they must
    // do mental math to fill in.
    function openResolveForm() {
        setMinutes(String(elapsedMinutesSince(incident.occurredAt)));
        setShowResolveForm(true);
    }

    async function act(fn: (id: number) => Promise<Incident>) {
        setBusy(true);
        try {
            await fn(incident.id);
            onChanged();
        } finally {
            setBusy(false);
        }
    }

    async function submitResolution() {
        setBusy(true);
        try {
            await fetch(`/api/incidents/${incident.id}/resolutions`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({
                    summary,
                    resolvedBy: resolvedBy || null,
                    timeToResolveMinutes: minutes ? Number(minutes) : null,
                    successful: true,
                }),
            });
            await resolveIncident(incident.id);
            onChanged();
        } finally {
            setBusy(false);
        }
    }

    const history = allIncidents
        .filter((i) => i.serviceName === incident.serviceName && i.title === incident.title)
        .sort((a, b) => new Date(a.occurredAt).getTime() - new Date(b.occurredAt).getTime());

    const chronicMatch = chronicIssues.find(
        (c) => c.serviceName === incident.serviceName && c.representativeTitle === incident.title
    );
    const severityCounts: Record<string, number> = {};
    for (const i of history) severityCounts[i.severity] = (severityCounts[i.severity] ?? 0) + 1;
    const priority = chronicMatch
        ? calculateFixPriority({
            occurrenceCount: chronicMatch.occurrenceCount,
            firstOccurrenceAt: chronicMatch.firstOccurrenceAt,
            lastOccurrenceAt: chronicMatch.lastOccurrenceAt,
            severityCounts,
        })
        : null;

    const stageIndex = STAGES.indexOf(incident.status);
    const confidence = similar?.matches.length
        ? Math.round(
            (similar.matches.reduce((s, m) => s + m.combinedScore, 0) / similar.matches.length) * 100
        )
        : null;

    return (
        <div className="fixed inset-0 z-20 flex justify-end bg-black/40" onClick={onClose}>
            <div
                className="h-full w-full max-w-xl overflow-y-auto border-l border-line bg-surface p-6"
                onClick={(e) => e.stopPropagation()}
            >
                <button onClick={onClose} className="font-mono text-xs text-dim hover:text-ink">
                    ← Close
                </button>

                <div className="mt-4 flex items-center gap-2">
                    <SeverityBadge severity={incident.severity} />
                    <span className="font-mono text-xs text-dim">{incident.serviceName}</span>
                </div>
                <h2 className="mt-2 text-xl font-semibold text-ink">{incident.title}</h2>
                <p className="mt-1 text-sm text-dim">{incident.description}</p>

                <div className="mt-6 flex items-center gap-1">
                    {STAGES.map((s, i) => (
                        <div key={s} className="flex flex-1 items-center gap-1">
                            <div className={`h-1.5 flex-1 rounded-full ${i <= stageIndex ? "bg-signal" : "bg-line"}`} />
                            {i < STAGES.length - 1 && <span className="text-dim">›</span>}
                        </div>
                    ))}
                </div>
                <div className="mt-1 flex justify-between font-mono text-[10px] uppercase tracking-widest text-dim">
                    {STAGES.map((s) => (
                        <span key={s}>{s}</span>
                    ))}
                </div>

                {incident.status !== "RESOLVED" && !showResolveForm && (
                    <div className="mt-5 flex gap-2">
                        {incident.status === "OPEN" && (
                            <button
                                disabled={busy}
                                onClick={() => act(acknowledgeIncident)}
                                className="rounded-md border border-line px-4 py-2 font-mono text-xs hover:bg-elevated disabled:opacity-50"
                            >
                                Acknowledge
                            </button>
                        )}
                        <button
                            disabled={incident.status !== "ACKNOWLEDGED"}
                            onClick={openResolveForm}
                            title={incident.status === "OPEN" ? "Acknowledge this incident first" : undefined}
                            className="rounded-md bg-signal px-4 py-2 font-mono text-xs text-black hover:opacity-90 disabled:cursor-not-allowed disabled:bg-line disabled:text-dim disabled:opacity-60"
                        >
                            Resolve
                        </button>
                    </div>
                )}

                {showResolveForm && (
                    <div className="mt-5 rounded-lg border border-line p-4">
                        <label className="text-xs uppercase tracking-widest text-dim">What fixed it?</label>
                        <textarea
                            value={summary}
                            onChange={(e) => setSummary(e.target.value)}
                            placeholder="e.g. Restarted the connection pool, cleared the backlog"
                            className="mt-2 w-full rounded-md border border-line bg-elevated p-2 text-sm text-ink"
                            rows={2}
                        />

                        <div className="mt-3 grid grid-cols-2 gap-3">
                            <div>
                                <label className="text-xs uppercase tracking-widest text-dim">Resolved by</label>
                                <input
                                    value={resolvedBy}
                                    onChange={(e) => setResolvedBy(e.target.value)}
                                    placeholder="your name"
                                    className="mt-2 w-full rounded-md border border-line bg-elevated p-2 text-sm text-ink"
                                />
                            </div>
                            <div>
                                <label className="text-xs uppercase tracking-widest text-dim">Time to resolve (min)</label>
                                <input
                                    value={minutes}
                                    onChange={(e) => setMinutes(e.target.value)}
                                    type="number"
                                    className="mt-2 w-full rounded-md border border-line bg-elevated p-2 text-sm text-ink"
                                />
                                <p className="mt-1 font-mono text-[10px] text-dim">
                                    Pre-filled from time since incident occurred — edit if needed.
                                </p>
                            </div>
                        </div>

                        <div className="mt-3 flex gap-2">
                            <button
                                disabled={busy || !summary}
                                onClick={submitResolution}
                                className="rounded-md bg-signal px-4 py-2 font-mono text-xs text-black disabled:opacity-50"
                            >
                                Confirm Resolve
                            </button>
                            <button
                                onClick={() => setShowResolveForm(false)}
                                className="rounded-md border border-line px-4 py-2 font-mono text-xs"
                            >
                                Cancel
                            </button>
                        </div>
                    </div>
                )}

                {history.length > 1 && (
                    <div className="mt-6 rounded-lg border border-line p-4">
                        <p className="text-xs uppercase tracking-widest text-dim">Recurrence</p>
                        <p className="mt-1 text-sm text-ink">This issue has occurred {history.length} times.</p>
                        <div className="mt-3 flex items-center gap-1 overflow-x-auto">
                            {history.map((h) => (
                                <span
                                    key={h.id}
                                    title={new Date(h.occurredAt).toLocaleDateString()}
                                    className="h-2 w-2 shrink-0 rounded-full bg-signal"
                                />
                            ))}
                        </div>
                        <div className="mt-1 flex justify-between font-mono text-[10px] text-dim">
                            <span>{new Date(history[0].occurredAt).toLocaleDateString()}</span>
                            <span>{new Date(history[history.length - 1].occurredAt).toLocaleDateString()}</span>
                        </div>
                    </div>
                )}

                {chronicMatch && priority !== null && (
                    <div className="mt-4 rounded-lg border border-critical/30 bg-critical/5 p-4">
                        <p className="font-mono text-[11px] uppercase tracking-widest text-critical">
                            ⚠ Chronic issue detected
                        </p>
                        <p className="mt-1 text-sm text-ink">
                            This incident has exceeded the recurrence threshold.
                        </p>
                        <p className="mt-2 font-mono text-2xl text-signal">{priority} / 100</p>
                        <p className="font-mono text-[10px] uppercase tracking-widest text-dim">Fix Priority</p>
                    </div>
                )}

                {similar?.suggestedRootCause && (
                    <div className="mt-4 rounded-lg border border-signal/30 bg-signal/5 p-4">
                        <p className="font-mono text-[11px] uppercase tracking-widest text-signal">
                            Sentinel Analysis
                        </p>
                        <p className="mt-2 text-sm text-ink/90">{similar.suggestedRootCause}</p>
                        {confidence !== null && (
                            <p className="mt-2 font-mono text-[11px] text-dim">
                                Confidence {confidence}% (avg. match similarity)
                            </p>
                        )}
                    </div>
                )}

                <div className="mt-6">
                    <p className="text-xs uppercase tracking-widest text-dim">
                        Similar incidents {similar ? `(${similar.matches.length})` : ""}
                    </p>
                    {similar === null && <p className="mt-2 font-mono text-xs text-dim">Searching…</p>}
                    <ul className="mt-2 space-y-2">
                        {similar?.matches.map((m) => (
                            <li key={m.id} className="rounded-lg border border-line p-3">
                                <div className="flex items-baseline justify-between">
                                    <p className="text-sm text-ink">{m.title}</p>
                                    <span className="font-mono text-[11px] text-signal">
                    {Math.round(m.combinedScore * 100)}% match
                  </span>
                                </div>
                                {m.priorResolutions.length > 0 ? (
                                    m.priorResolutions.map((r, i) => (
                                        <p key={i} className="mt-1 font-mono text-xs text-dim">
                                            ✓ {r.summary} {r.timeToResolveMinutes && `(${r.timeToResolveMinutes}m)`}
                                        </p>
                                    ))
                                ) : (
                                    <p className="mt-1 font-mono text-xs text-dim">No recorded resolution.</p>
                                )}
                            </li>
                        ))}
                    </ul>
                </div>
            </div>
        </div>
    );
}