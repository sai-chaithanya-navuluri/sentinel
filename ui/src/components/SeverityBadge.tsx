const COLORS: Record<string, string> = {
    CRITICAL: "bg-critical/15 text-critical border-critical/30",
    HIGH: "bg-high/15 text-high border-high/30",
    MEDIUM: "bg-medium/15 text-medium border-medium/30",
    LOW: "bg-low/15 text-low border-low/30",
};
export function SeverityBadge({ severity }: { severity: string }) {
    return (
        <span className={`inline-flex items-center gap-1.5 rounded border px-2 py-0.5 font-mono text-[11px] uppercase tracking-wide ${COLORS[severity] ?? ""}`}>
      <span className="h-1.5 w-1.5 rounded-full bg-current" />{severity}
    </span>
    );
}