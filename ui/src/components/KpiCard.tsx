export function KpiCard({ label, value, sublabel, tone = "neutral" }: { label: string; value: string | number; sublabel?: string; tone?: "up" | "down" | "neutral" }) {
    const toneColor = tone === "up" ? "text-healthy" : tone === "down" ? "text-critical" : "text-dim";
    return (
        <div className="rounded-lg border border-line bg-surface p-5">
            <p className="text-xs uppercase tracking-widest text-dim">{label}</p>
            <p className="mt-2 font-mono text-3xl font-medium text-ink">{value}</p>
            {sublabel && <p className={`mt-1 text-xs ${toneColor}`}>{sublabel}</p>}
        </div>
    );
}