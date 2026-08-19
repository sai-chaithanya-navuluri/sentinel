import { SeverityBadge } from "./SeverityBadge";
import { serviceStyle } from "../lib/serviceColor";
import type { Incident } from "../api";

function timeAgo(iso: string) {
    const diffMin = Math.round((Date.now() - new Date(iso).getTime()) / 60000);
    if (diffMin < 1) return "just now";
    if (diffMin < 60) return `${diffMin}m ago`;
    const h = Math.round(diffMin / 60);
    return h < 24 ? `${h}h ago` : `${Math.round(h / 24)}d ago`;
}

export function IncidentCard({ incident, onClick }: { incident: Incident; onClick: () => void }) {
    const style = serviceStyle(incident.serviceName);
    return (
        <button onClick={onClick} className="w-full rounded-lg border border-line bg-surface p-4 text-left transition-colors hover:bg-elevated/40">
            <div className="flex items-center gap-2">
                <SeverityBadge severity={incident.severity} />
                <span className={`rounded px-1.5 py-0.5 font-mono text-[11px] ${style.bg} ${style.text}`}>{incident.serviceName}</span>
                <span className="ml-auto font-mono text-[11px] text-dim">{timeAgo(incident.occurredAt)}</span>
            </div>
            <p className="mt-2 text-sm font-medium text-ink">{incident.title}</p>
            <p className="mt-1 line-clamp-1 text-xs text-dim">{incident.description}</p>
            <p className="mt-3 font-mono text-[10px] uppercase tracking-wider text-dim">INC-{incident.id}</p>
        </button>
    );
}