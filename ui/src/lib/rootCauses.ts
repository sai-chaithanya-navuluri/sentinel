import type { ChronicIssue } from "../api";

// Groups chronic issues by keyword overlap in their titles — a simple,
// honest heuristic (not a real clustering model) that surfaces genuinely
// common failure categories from your actual chronic-issue titles.
const CAUSE_KEYWORDS: Record<string, string[]> = {
    "Database Connection": ["connection", "timeout", "database", "pool"],
    "Memory Pressure": ["memory", "heap", "gc"],
    "Latency / Performance": ["latency", "slow", "performance", "response"],
    "Disk / Storage": ["disk", "storage", "space"],
};

export function groupByRootCause(issues: ChronicIssue[]) {
    const groups = new Map<string, ChronicIssue[]>();
    for (const issue of issues) {
        const title = issue.representativeTitle.toLowerCase();
        const match = Object.entries(CAUSE_KEYWORDS).find(([, kws]) => kws.some((k) => title.includes(k)));
        const cause = match ? match[0] : "Other";
        if (!groups.has(cause)) groups.set(cause, []);
        groups.get(cause)!.push(issue);
    }
    return [...groups.entries()]
        .map(([cause, list]) => ({
            cause,
            incidentCount: list.reduce((s, i) => s + i.occurrenceCount, 0),
            servicesAffected: new Set(list.map((i) => i.serviceName)).size,
            issues: list,
        }))
        .sort((a, b) => b.incidentCount - a.incidentCount);
}