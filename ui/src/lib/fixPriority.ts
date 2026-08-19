export interface FixPriorityInput {
    occurrenceCount: number;
    firstOccurrenceAt: string;
    lastOccurrenceAt: string;
    severityCounts: Record<string, number>;
}

// Weights are named and isolated here specifically so this is easy to retune
// later without hunting through components.
const WEIGHTS = { frequency: 0.30, severity: 0.25, recency: 0.15, duration: 0.10, impact: 0.10, recurrence: 0.10 };
const SEVERITY_SCORE: Record<string, number> = { CRITICAL: 100, HIGH: 75, MEDIUM: 45, LOW: 20 };
const clamp01 = (n: number) => Math.max(0, Math.min(1, n));

export function calculateFixPriority(input: FixPriorityInput): number {
    const frequencyScore = clamp01(input.occurrenceCount / 20) * 100;

    const totalSeen = Object.values(input.severityCounts).reduce((a, b) => a + b, 0);
    const severityScore = totalSeen === 0 ? 50 :
        Object.entries(input.severityCounts).reduce(
            (sum, [sev, count]) => sum + (SEVERITY_SCORE[sev] ?? 40) * (count / totalSeen), 0);

    const daysSinceLast = (Date.now() - new Date(input.lastOccurrenceAt).getTime()) / 86_400_000;
    const recencyScore = clamp01(1 - daysSinceLast / 30) * 100;

    const spanDays = (new Date(input.lastOccurrenceAt).getTime() - new Date(input.firstOccurrenceAt).getTime()) / 86_400_000;
    const durationScore = clamp01(spanDays / 60) * 100;

    // "Impact" and "recurrence" have no dedicated field in the current data
    // model — this is the honest proxy (frequency × severity, and recency)
    // until real customer-impact data exists. Kept as separate weighted terms
    // so swapping in real data later is a one-line change.
    const impactScore = clamp01((frequencyScore / 100) * (severityScore / 100)) * 100;
    const recurrenceScore = recencyScore;

    const total =
        frequencyScore * WEIGHTS.frequency + severityScore * WEIGHTS.severity +
        recencyScore * WEIGHTS.recency + durationScore * WEIGHTS.duration +
        impactScore * WEIGHTS.impact + recurrenceScore * WEIGHTS.recurrence;

    return Math.round(total);
}