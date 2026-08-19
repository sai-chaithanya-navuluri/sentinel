export function Header({ lastUpdated }: { lastUpdated: Date | null }) {
    const secondsAgo = lastUpdated ? Math.max(0, Math.round((Date.now() - lastUpdated.getTime()) / 1000)) : null;
    return (
        <header className="flex items-center justify-between border-b border-line bg-surface px-6 py-3">
            <h1 className="text-sm font-semibold tracking-tight">Production Intelligence</h1>
            <div className="flex items-center gap-4">
                <div className="flex items-center gap-1.5 font-mono text-xs text-dim">
                    <span className="h-1.5 w-1.5 rounded-full bg-healthy" />PRODUCTION
                </div>
                {secondsAgo !== null && <span className="font-mono text-xs text-dim">Updated {secondsAgo}s ago</span>}
                <button onClick={() => window.dispatchEvent(new Event("sentinel:open-palette"))} className="flex items-center gap-2 rounded-md border border-line bg-elevated px-3 py-1.5 text-xs text-dim">
                    Search <kbd className="rounded border border-line px-1 font-mono text-[10px]">⌘K</kbd>
                </button>
            </div>
        </header>
    );
}