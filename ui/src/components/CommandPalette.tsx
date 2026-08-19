import { useEffect, useMemo, useState } from "react";
import type { Incident, ChronicIssue } from "../api";

export function CommandPalette({
                                   open, onClose, incidents, chronicIssues, onSelectIncident, onSelectChronic,
                               }: {
    open: boolean; onClose: () => void; incidents: Incident[]; chronicIssues: ChronicIssue[];
    onSelectIncident: (i: Incident) => void;
    onSelectChronic: () => void;
}) {
    const [query, setQuery] = useState("");

    useEffect(() => {
        function onKey(e: KeyboardEvent) {
            if ((e.metaKey || e.ctrlKey) && e.key === "k") { e.preventDefault(); if (!open) window.dispatchEvent(new Event("sentinel:open-palette")); }
            if (e.key === "Escape") onClose();
        }
        window.addEventListener("keydown", onKey);
        return () => window.removeEventListener("keydown", onKey);
    }, [open, onClose]);

    const results = useMemo(() => {
        const q = query.toLowerCase().trim();
        if (!q) return [];
        return incidents
            .filter((i) =>
                i.title.toLowerCase().includes(q) ||
                i.serviceName.toLowerCase().includes(q) ||
                i.description.toLowerCase().includes(q) ||
                String(i.id).includes(q) ||
                `inc-${i.id}`.includes(q)
            )
            .slice(0, 8);
    }, [query, incidents]);

    const chronicMatches = useMemo(() => {
        const q = query.toLowerCase().trim();
        if (!q) return [];
        return chronicIssues.filter((c) => c.representativeTitle.toLowerCase().includes(q)).slice(0, 4);
    }, [query, chronicIssues]);

    if (!open) return null;

    return (
        <div className="fixed inset-0 z-30 flex items-start justify-center bg-black/50 pt-24" onClick={onClose}>
            <div className="w-full max-w-lg rounded-lg border border-line bg-surface shadow-2xl" onClick={(e) => e.stopPropagation()}>
                <input
                    autoFocus
                    value={query}
                    onChange={(e) => setQuery(e.target.value)}
                    placeholder="Search incidents, services, errors…"
                    className="w-full border-b border-line bg-transparent p-4 text-sm text-ink outline-none placeholder:text-dim"
                />
                <div className="max-h-80 overflow-y-auto p-2">
                    {query && results.length === 0 && chronicMatches.length === 0 && (
                        <p className="p-4 font-mono text-xs text-dim">No matches.</p>
                    )}
                    {chronicMatches.map((c) => (
                        <button
                            key={`c-${c.id}`}
                            onClick={() => { onSelectChronic(); onClose(); setQuery(""); }}
                            className="flex w-full items-center gap-2 rounded-md p-2 text-left text-sm text-ink hover:bg-elevated"
                        >
                            <span>🔥</span>
                            <span>{c.representativeTitle}</span>
                            <span className="ml-auto font-mono text-[11px] text-dim">{c.serviceName} · Fix Queue</span>
                        </button>
                    ))}
                    {results.map((i) => (
                        <button
                            key={i.id}
                            onClick={() => { onSelectIncident(i); onClose(); setQuery(""); }}
                            className="flex w-full items-center gap-2 rounded-md p-2 text-left text-sm text-ink hover:bg-elevated"
                        >
                            <span className="font-mono text-[11px] text-dim">INC-{i.id}</span>
                            <span>{i.title}</span>
                            <span className="ml-auto font-mono text-[11px] text-dim">{i.serviceName}</span>
                        </button>
                    ))}
                </div>
            </div>
        </div>
    );
}