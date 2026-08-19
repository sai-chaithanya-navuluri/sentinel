// EmptyState.tsx
export function EmptyState({ icon = "○", title, message }: { icon?: string; title: string; message: string }) {
    return (
        <div className="rounded-lg border border-line bg-surface p-10 text-center">
            <p className="text-lg">{icon}</p>
            <p className="mt-2 text-sm font-medium text-ink">{title}</p>
            <p className="mt-1 font-mono text-xs text-dim">{message}</p>
        </div>
    );
}