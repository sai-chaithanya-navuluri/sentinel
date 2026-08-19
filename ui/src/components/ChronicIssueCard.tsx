import { calculateFixPriority } from "../lib/fixPriority";
import { serviceStyle } from "../lib/serviceColor";
import type { ChronicIssue, Incident } from "../api";

export function ChronicIssueCard({ issue, relatedIncidents, onClick }: { issue: ChronicIssue; relatedIncidents: Incident[]; onClick: () => void }) {
    const style = serviceStyle(issue.serviceName);
    const severityCounts: Record<string, number> = {};
    for (const i of relatedIncidents) severityCounts[i.severity] = (severityCounts[i.severity] ?? 0) + 1;
    const priority = calculateFixPriority({
        occurrenceCount: issue.occurrenceCount, firstOccurrenceAt: issue.firstOccurrenceAt,
        lastOccurrenceAt: issue.lastOccurrenceAt, severityCounts,
    });
    const daysAgo = Math.round((Date.now() - new Date(issue.lastOccurrenceAt).getTime()) / 86_400_000);

    return (
        <button onClick={onClick} className="w-full rounded-lg border border-signal/20 bg-surface p-4 text-left hover:border-signal/40">
            <div className="flex items-center gap-2"><span>🔥</span><p className="text-sm font-medium text-ink">{issue.representativeTitle}</p></div>
            <span className={`mt-1 inline-block rounded px-1.5 py-0.5 font-mono text-[11px] ${style.bg} ${style.text}`}>{issue.serviceName}</span>
            <p className="mt-3 font-mono text-xs text-dim">{issue.occurrenceCount} occurrences</p>
            <div className="mt-1 h-1.5 w-full rounded-full bg-line">
                <div className="h-1.5 rounded-full bg-signal" style={{ width: `${Math.min(issue.occurrenceCount / 20, 1) * 100}%` }} />
            </div>
            <p className="mt-2 font-mono text-[11px] text-dim">Last occurred {daysAgo}d ago</p>
            <div className="mt-3 flex items-center justify-between">
                <div><p className="text-[10px] uppercase tracking-widest text-dim">Fix Priority</p><p className="font-mono text-lg text-signal">{priority} / 100</p></div>
                {priority >= 70 && <span className="rounded bg-critical/10 px-2 py-1 font-mono text-[10px] text-critical">Permanent fix recommended</span>}
            </div>
        </button>
    );
}