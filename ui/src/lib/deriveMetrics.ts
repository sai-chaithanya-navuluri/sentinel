import type { Incident, ChronicIssue } from "../api";

export function getServiceHealth(incidents: Incident[]) {
    const byService = new Map<string, Incident[]>();
    for (const i of incidents) {
        if (!byService.has(i.serviceName)) byService.set(i.serviceName, []);
        byService.get(i.serviceName)!.push(i);
    }
    return [...byService.entries()].map(([serviceName, list]) => {
        const open = list.filter((i) => i.status !== "RESOLVED").length;
        const critical = list.filter((i) => i.severity === "CRITICAL" && i.status !== "RESOLVED").length;
        const high = list.filter((i) => i.severity === "HIGH" && i.status !== "RESOLVED").length;
        // Status derived from real open/critical/high counts — not a fabricated uptime %.
        const status: "healthy" | "degraded" | "critical" =
            critical > 0 ? "critical" : high > 0 || open > 3 ? "degraded" : "healthy";
        return { serviceName, totalIncidents: list.length, open, status };
    }).sort((a, b) => b.open - a.open);
}

export function getIncidentComposition(incidents: Incident[], chronic: ChronicIssue[]) {
    const chronicKeys = new Set(chronic.map((c) => `${c.serviceName}::${c.representativeTitle}`));
    const seen = new Map<string, number>();
    let chronicCount = 0, recurringCount = 0, newCount = 0;
    for (const i of incidents) {
        const key = `${i.serviceName}::${i.title}`;
        const count = (seen.get(key) ?? 0) + 1;
        seen.set(key, count);
        if (chronicKeys.has(key)) chronicCount++;
        else if (count > 1) recurringCount++;
        else newCount++;
    }
    const total = incidents.length || 1;
    return {
        newPct: Math.round((newCount / total) * 100),
        recurringPct: Math.round((recurringCount / total) * 100),
        chronicPct: Math.round((chronicCount / total) * 100),
    };
}

export function getIncidentTrend(incidents: Incident[], days: number) {
    const buckets = new Map<string, number>();
    const now = Date.now();
    for (let d = days - 1; d >= 0; d--) {
        buckets.set(new Date(now - d * 86_400_000).toISOString().slice(0, 10), 0);
    }
    for (const i of incidents) {
        const key = i.occurredAt.slice(0, 10);
        if (buckets.has(key)) buckets.set(key, (buckets.get(key) ?? 0) + 1);
    }
    return [...buckets.entries()].map(([date, count]) => ({ date, count }));
}

export function getKpis(incidents: Incident[], chronic: ChronicIssue[]) {
    const now = Date.now(), week = 7 * 86_400_000;
    const thisWeek = incidents.filter((i) => now - new Date(i.occurredAt).getTime() < week).length;
    const lastWeek = incidents.filter((i) => {
        const age = now - new Date(i.occurredAt).getTime();
        return age >= week && age < 2 * week;
    }).length;

    const active = incidents.filter((i) => i.status !== "RESOLVED").length;
    const resolved = incidents.filter((i) => i.status === "RESOLVED").length;
    const resolvedThisMonth = incidents.filter((i) => i.status === "RESOLVED" && now - new Date(i.occurredAt).getTime() < 30 * 86_400_000).length;
    const totalThisMonth = incidents.filter((i) => now - new Date(i.occurredAt).getTime() < 30 * 86_400_000).length;

    const { recurringPct, chronicPct } = getIncidentComposition(incidents, chronic);

    return {
        active,
        activeDeltaWeek: thisWeek - lastWeek,
        chronicOpen: chronic.filter((c) => c.status === "OPEN").length,
        resolved,
        resolvedRateThisMonth: totalThisMonth ? Math.round((resolvedThisMonth / totalThisMonth) * 100) : null,
        recurringRate: recurringPct + chronicPct,
    };
}