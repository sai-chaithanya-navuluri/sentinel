// ErrorState.tsx
export function ErrorState({ message, onRetry }: { message: string; onRetry: () => void }) {
    return (
        <div className="rounded-lg border border-critical/30 bg-critical/5 p-10 text-center">
            <p className="text-sm font-medium text-critical">Unable to load data</p>
            <p className="mt-1 font-mono text-xs text-dim">{message}</p>
            <button onClick={onRetry} className="mt-4 rounded-md border border-line px-4 py-1.5 font-mono text-xs hover:bg-elevated">
                Retry
            </button>
        </div>
    );
}