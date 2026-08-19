export function TrendChart({ data }: { data: { date: string; count: number }[] }) {
    const max = Math.max(...data.map((d) => d.count), 1);
    const w = 100 / Math.max(data.length - 1, 1);
    const points = data.map((d, i) => `${i * w},${100 - (d.count / max) * 90}`).join(" ");
    return (
        <div className="rounded-lg border border-line bg-surface p-4">
            <p className="mb-3 text-xs uppercase tracking-widest text-dim">Incident Activity</p>
            <svg viewBox="0 0 100 100" preserveAspectRatio="none" className="h-32 w-full">
                <polyline points={points} fill="none" stroke="#E8A33D" strokeWidth="1.5" vectorEffect="non-scaling-stroke" />
            </svg>
            <div className="mt-2 flex justify-between font-mono text-[10px] text-dim">
                <span>{data[0]?.date.slice(5)}</span><span>{data[data.length - 1]?.date.slice(5)}</span>
            </div>
        </div>
    );
}