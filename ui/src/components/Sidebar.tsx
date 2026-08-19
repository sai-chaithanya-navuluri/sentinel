import { serviceStyle } from "../lib/serviceColor";

export function Sidebar({
                            active, onNavigate, services, collapsed, onToggle,
                        }: { active: string; onNavigate: (s: string) => void; services: string[]; collapsed: boolean; onToggle: () => void }) {
    return (
        <aside className={`flex h-full flex-col border-r border-line bg-surface transition-all ${collapsed ? "w-14" : "w-60"}`}>
            <div className="flex items-center justify-between px-4 py-4">
                {!collapsed && <span className="font-semibold tracking-tight">SENTINEL</span>}
                <button onClick={onToggle} className="text-dim hover:text-ink" aria-label="Toggle sidebar">‹</button>
            </div>
            <nav className="flex-1 space-y-6 overflow-y-auto px-2">
                <div>
                    <button onClick={() => onNavigate("overview")}
                            className={`flex w-full items-center rounded-md px-3 py-1.5 text-left text-sm ${active === "overview" ? "bg-elevated text-ink" : "text-dim hover:bg-elevated/60 hover:text-ink"}`}>
                        {!collapsed && "Dashboard"}
                    </button>
                    <button onClick={() => onNavigate("fix-queue")}
                            className={`flex w-full items-center rounded-md px-3 py-1.5 text-left text-sm ${active === "fix-queue" ? "bg-elevated text-ink" : "text-dim hover:bg-elevated/60 hover:text-ink"}`}>
                        {!collapsed && "Fix Queue"}
                    </button>
                    <button onClick={() => onNavigate("root-causes")}
                            className={`flex w-full items-center rounded-md px-3 py-1.5 text-left text-sm ${active === "root-causes" ? "bg-elevated text-ink" : "text-dim hover:bg-elevated/60 hover:text-ink"}`}>
                        {!collapsed && "Root Causes"}
                    </button>
                </div>
                <div>
                    {!collapsed && <p className="px-3 pb-1 text-[11px] uppercase tracking-widest text-dim">Services</p>}
                    {services.map((s) => {
                        const style = serviceStyle(s);
                        const isActive = active === s;
                        return (
                            <button key={s} onClick={() => onNavigate(s)}
                                    className={`flex w-full items-center gap-2 rounded-md px-3 py-1.5 text-left font-mono text-xs ${isActive ? "bg-elevated text-ink" : "text-dim hover:bg-elevated/60 hover:text-ink"}`}>
                                <span className={`h-1.5 w-1.5 shrink-0 rounded-full ${style.dot}`} />
                                {!collapsed && s.replace(/-/g, " ").replace(/\b\w/g, (c) => c.toUpperCase())}
                            </button>
                        );
                    })}
                </div>
            </nav>
        </aside>
    );
}