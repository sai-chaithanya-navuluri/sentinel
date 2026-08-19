import type { ChronicIssue } from "../api";
import { groupByRootCause } from "../lib/rootCauses";

export function RootCauses({ chronicIssues }: { chronicIssues: ChronicIssue[] }) {
    const groups = groupByRootCause(chronicIssues);
    return (
        <div className="p-6">
            <h1 className="text-lg font-semibold text-ink">Root Causes</h1>
            <p className="mt-1 font-mono text-xs text-dim">Chronic issues grouped by likely common cause</p>
            <div className="mt-4 grid grid-cols-2 gap-4">
                {groups.map((g) => (
                    <div key={g.cause} className="rounded-lg border border-line bg-surface p-4">
                        <p className="text-sm font-medium text-ink">{g.cause}</p>
                        <p className="mt-1 font-mono text-xs text-dim">{g.incidentCount} incidents</p>
                        <p className="font-mono text-xs text-dim">{g.servicesAffected} service{g.servicesAffected !== 1 ? "s" : ""} affected</p>
                    </div>
                ))}
            </div>
        </div>
    );
}