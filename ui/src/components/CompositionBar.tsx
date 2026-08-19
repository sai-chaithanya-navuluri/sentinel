export function CompositionBar({
                                   newPct, recurringPct, chronicPct, total,
                               }: { newPct: number; recurringPct: number; chronicPct: number; total: number }) {
    return (
        <div className="rounded-lg border border-line bg-surface p-4">
            <p className="mb-3 text-xs uppercase tracking-widest text-dim">Incident Composition</p>
            <div className="flex h-2.5 w-full overflow-hidden rounded-full bg-line">
                <div className="bg-low" style={{ width: `${newPct}%` }} />
                <div className="bg-medium" style={{ width: `${recurringPct}%` }} />
                <div className="bg-critical" style={{ width: `${chronicPct}%` }} />
            </div>
            <div className="mt-2 flex gap-4 font-mono text-[11px] text-dim">
                <span className="text-low">● New {newPct}%</span>
                <span className="text-medium">● Recurring {recurringPct}%</span>
                <span className="text-critical">● Chronic {chronicPct}%</span>
            </div>
            <p className="mt-2 font-mono text-[10px] text-dim">Based on {total} tracked incidents</p>
        </div>
    );
}