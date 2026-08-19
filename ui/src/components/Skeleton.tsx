// Skeleton.tsx
export function SkeletonCard() {
    return (
        <div className="animate-pulse rounded-lg border border-line bg-surface p-4">
            <div className="h-3 w-24 rounded bg-line" />
            <div className="mt-3 h-4 w-3/4 rounded bg-line" />
            <div className="mt-2 h-3 w-1/2 rounded bg-line" />
        </div>
    );
}
export function SkeletonKpi() {
    return (
        <div className="animate-pulse rounded-lg border border-line bg-surface p-5">
            <div className="h-2.5 w-20 rounded bg-line" />
            <div className="mt-3 h-7 w-12 rounded bg-line" />
        </div>
    );
}