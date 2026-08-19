import { useState } from "react";
import type { ChronicIssue, Incident } from "../api";
import { calculateFixPriority } from "../lib/fixPriority";

export function FixQueue({ chronicIssues, incidents }: { chronicIssues: ChronicIssue[]; incidents: Incident[] }) {
    const [sortBy, setSortBy] = useState<"priority" | "frequency" | "severity">("priority");

    const rows = chronicIssues
        .filter((c) => c.status === "OPEN")
        .map((c) => {
            const related = incidents.filter((i) => i.serviceName === c.serviceName && i.title === c.representativeTitle);
            const severityCounts: Record<string, number> = {};
            for (const i of related) severityCounts[i.severity] = (severityCounts[i.severity] ?? 0) + 1;
            const priority = calculateFixPriority({
                occurrenceCount: c.occurrenceCount, firstOccurrenceAt: c.firstOccurrenceAt,
                lastOccurrenceAt: c.lastOccurrenceAt, severityCounts,
            });
            const worstSeverity = ["CRITICAL", "HIGH", "MEDIUM", "LOW"].find((s) => severityCounts[s]) ?? "LOW";
            return { ...c, priority, worstSeverity };
        })
        .sort((a, b) =>
            sortBy === "priority" ? b.priority - a.priority :
                sortBy === "frequency" ? b.occurrenceCount - a.occurrenceCount :
                    ["CRITICAL", "HIGH", "MEDIUM", "LOW"].indexOf(a.worstSeverity) - ["CRITICAL", "HIGH", "MEDIUM", "LOW"].indexOf(b.worstSeverity)
        );

    return (
        <div className="p-6">
            <h1 className="text-lg font-semibold text-ink">Permanent Fix Queue</h1>
            <p className="mt-1 font-mono text-xs text-dim">Issues requiring engineering attention</p>

            <div className="mt-4 flex gap-2">
                {(["priority", "frequency", "severity"] as const).map((s) => (
                    <button key={s} onClick={() => setSortBy(s)}
                            className={`rounded-md border px-3 py-1 font-mono text-[11px] capitalize ${sortBy === s ? "border-signal text-signal" : "border-line text-dim"}`}>
                        {s}
                    </button>
                ))}
            </div>

            <div className="mt-4 space-y-3">
                {rows.length === 0 && <p className="font-mono text-xs text-dim">Nothing in the queue — no open chronic issues.</p>}
                {rows.map((r) => (
                    <div key={r.id} className="rounded-lg border border-line bg-surface p-4">
                        <div className="flex items-start justify-between">
                            <div>
                                <p className="flex items-center gap-2 text-sm font-medium text-ink">🔥 {r.representativeTitle}</p>
                                <p className="mt-1 font-mono text-xs text-dim">{r.serviceName} · {r.occurrenceCount} occurrences</p>
                                <p className="mt-1 font-mono text-[11px] text-dim">Owner: Unassigned</p>
                            </div>
                            <div className="text-right">
                                <p className="font-mono text-2xl text-signal">{r.priority}</p>
                                <p className="font-mono text-[10px] uppercase tracking-widest text-dim">Fix Priority</p>
                            </div>
                        </div>
                        <button
                            disabled
                            title="Fix tracking is planned for a future release"
                            className="mt-3 cursor-not-allowed rounded-md border border-line px-3 py-1.5 font-mono text-xs text-dim opacity-60"
                        >
                            Create Fix · Coming soon
                        </button>
                    </div>
                ))}
            </div>
        </div>
    );
}