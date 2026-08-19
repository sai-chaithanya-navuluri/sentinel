const BASE = "/api";

export interface Incident {
    id: number;
    externalId: string | null;
    title: string;
    description: string;
    serviceName: string;
    severity: "CRITICAL" | "HIGH" | "MEDIUM" | "LOW";
    status: "OPEN" | "ACKNOWLEDGED" | "RESOLVED";
    occurredAt: string;
    resolvedAt: string | null;
}

export interface ChronicIssue {
    id: number;
    serviceName: string;
    representativeTitle: string;
    occurrenceCount: number;
    firstOccurrenceAt: string;
    lastOccurrenceAt: string;
    status: "OPEN" | "ACKNOWLEDGED" | "RESOLVED";
}

async function get<T>(path: string): Promise<T> {
    const r = await fetch(`${BASE}${path}`);
    if (!r.ok) throw new Error(`${path} → ${r.status}`);
    return r.json();
}

async function post<T>(path: string): Promise<T> {
    const r = await fetch(`${BASE}${path}`, { method: "POST" });
    if (!r.ok) throw new Error(`${path} → ${r.status}`);
    return r.json();
}

export const fetchIncidents = () => get<Incident[]>("/incidents");
export const fetchChronicIssues = () => get<ChronicIssue[]>("/chronic-issues");
export const acknowledgeIncident = (id: number) => post<Incident>(`/incidents/${id}/acknowledge`);
export const resolveIncident = (id: number) => post<Incident>(`/incidents/${id}/resolve`);

export interface ResolutionSummary {
    summary: string;
    resolvedBy: string | null;
    timeToResolveMinutes: number | null;
    successful: boolean;
}

export interface SimilarIncident {
    id: number;
    title: string;
    description: string;
    textScore: number;
    semanticScore: number;
    combinedScore: number;
    priorResolutions: ResolutionSummary[];
}

export interface SimilarResult {
    matches: SimilarIncident[];
    suggestedRootCause: string | null;
}

export const fetchSimilar = (id: number) => get<SimilarResult>(`/incidents/${id}/similar`);