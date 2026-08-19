const PALETTE = [
    { dot: "bg-rose-400", text: "text-rose-300", bg: "bg-rose-400/10" },
    { dot: "bg-sky-400", text: "text-sky-300", bg: "bg-sky-400/10" },
    { dot: "bg-violet-400", text: "text-violet-300", bg: "bg-violet-400/10" },
    { dot: "bg-emerald-400", text: "text-emerald-300", bg: "bg-emerald-400/10" },
    { dot: "bg-amber-400", text: "text-amber-300", bg: "bg-amber-400/10" },
];

export function serviceStyle(name: string) {
    let hash = 0;
    for (let i = 0; i < name.length; i++) hash = (hash * 31 + name.charCodeAt(i)) >>> 0;
    return PALETTE[hash % PALETTE.length];
}