import { serviceStyle } from "../lib/serviceColor";
const STATUS_DOT = { healthy: "bg-healthy", degraded: "bg-medium", critical: "bg-critical" };

export function ServiceHealth({ services, active, onSelect }: {
    services: { serviceName: string; open: number; status: keyof typeof STATUS_DOT }[];
    active: string | null; onSelect: (s: string | null) => void;
}) {
    return (
        <div className="rounded-lg border border-line bg-surface p-4">
            <p className="mb-3 text-xs uppercase tracking-widest text-dim">Services</p>
            <ul className="space-y-1">
                {services.map((s) => {
                    const style = serviceStyle(s.serviceName);
                    const isActive = active === s.serviceName;
                    return (
                        <li key={s.serviceName}>
                            <button onClick={() => onSelect(isActive ? null : s.serviceName)}
                                    className={`flex w-full items-center justify-between rounded-md px-2 py-2 text-left ${isActive ? "bg-elevated" : "hover:bg-elevated/60"}`}>
                <span className="flex items-center gap-2 font-mono text-xs text-ink">
                  <span className={`h-1.5 w-1.5 rounded-full ${style.dot}`} />{s.serviceName}
                </span>
                                <span className="flex items-center gap-2">
                  <span className="font-mono text-[11px] text-dim">{s.open} open</span>
                  <span className={`h-2 w-2 rounded-full ${STATUS_DOT[s.status]}`} />
                </span>
                            </button>
                        </li>
                    );
                })}
            </ul>
        </div>
    );
}