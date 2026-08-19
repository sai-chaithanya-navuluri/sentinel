import { useEffect, useState } from "react";
import { fetchIncidents, fetchChronicIssues } from "./api";
import type { Incident, ChronicIssue } from "./api";
import { Sidebar } from "./components/Sidebar";
import { Header } from "./components/Header";
import { KpiCard } from "./components/KpiCard";
import { TrendChart } from "./components/TrendChart";
import { CompositionBar } from "./components/CompositionBar";
import { ServiceHealth } from "./components/ServiceHealth";
import { IncidentCard } from "./components/IncidentCard";
import { ChronicIssueCard } from "./components/ChronicIssueCard";
import { IncidentDetail } from "./components/IncidentDetail";
import { CommandPalette } from "./components/CommandPalette";
import { EmptyState } from "./components/EmptyState";
import { ErrorState } from "./components/ErrorState";
import { SkeletonCard, SkeletonKpi } from "./components/Skeleton";
import { FixQueue } from "./pages/FixQueue";
import { RootCauses } from "./pages/RootCauses";
import { getKpis, getServiceHealth, getIncidentComposition, getIncidentTrend } from "./lib/deriveMetrics";

function usePoll<T>(fn: () => Promise<T>, ms: number) {
  const [data, setData] = useState<T | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [tick, setTick] = useState(0);
  useEffect(() => {
    let live = true;
    const run = () => fn().then((d) => { if (live) { setData(d); setError(null); } })
        .catch((e) => live && setError(String(e)));
    run();
    const t = setInterval(run, ms);
    return () => { live = false; clearInterval(t); };
  }, [tick]);
  return { data, error, retry: () => setTick((t) => t + 1) };
}

export default function App() {
  const { data: incidents, error: incidentsError, retry: retryIncidents } = usePoll(fetchIncidents, 5000);
  const { data: chronic, error: chronicError, retry: retryChronic } = usePoll(fetchChronicIssues, 15000);
  const [collapsed, setCollapsed] = useState(false);
  const [page, setPage] = useState<"overview" | "fix-queue" | "root-causes" | string>("overview");
  const [selected, setSelected] = useState<Incident | null>(null);
  const [lastUpdated, setLastUpdated] = useState<Date | null>(null);
  const [paletteOpen, setPaletteOpen] = useState(false);
  const [mobileNavOpen, setMobileNavOpen] = useState(false);

  useEffect(() => { if (incidents) setLastUpdated(new Date()); }, [incidents]);
  useEffect(() => {
    function onOpen() { setPaletteOpen(true); }
    window.addEventListener("sentinel:open-palette", onOpen);
    return () => window.removeEventListener("sentinel:open-palette", onOpen);
  }, []);

  if (incidentsError || chronicError) {
    return (
        <div className="flex h-screen items-center justify-center bg-bg p-6">
          <ErrorState message={incidentsError ?? chronicError ?? "Unknown error"} onRetry={() => { retryIncidents(); retryChronic(); }} />
        </div>
    );
  }

  if (!incidents || !chronic) {
    return (
        <div className="grid h-screen grid-cols-4 gap-4 bg-bg p-6">
          {Array.from({ length: 4 }).map((_, i) => <SkeletonKpi key={i} />)}
          <div className="col-span-4 grid grid-cols-3 gap-4">
            {Array.from({ length: 3 }).map((_, i) => <SkeletonCard key={i} />)}
          </div>
        </div>
    );
  }

  const services = [...new Set(incidents.map((i: Incident) => i.serviceName))].sort();
  const activeService = ["overview", "fix-queue", "root-causes"].includes(page) ? null : page;
  const kpis = getKpis(incidents, chronic);
  const health = getServiceHealth(incidents);
  const composition = getIncidentComposition(incidents, chronic);
  const trend = getIncidentTrend(incidents, 30);

  const open = incidents.filter((i: Incident) => i.status !== "RESOLVED");
  const filtered = activeService ? open.filter((i: Incident) => i.serviceName === activeService) : open;
  const openChronic = chronic.filter((c: ChronicIssue) => c.status === "OPEN");

  return (
      <div className="flex h-screen overflow-hidden bg-bg text-ink">
        <div className="hidden md:block">
          <Sidebar active={page} onNavigate={setPage} services={services} collapsed={collapsed} onToggle={() => setCollapsed((c) => !c)} />
        </div>

        {mobileNavOpen && (
            <div className="fixed inset-0 z-40 md:hidden" onClick={() => setMobileNavOpen(false)}>
              <div className="h-full w-60" onClick={(e) => e.stopPropagation()}>
                <Sidebar active={page} onNavigate={(p) => { setPage(p); setMobileNavOpen(false); }} services={services} collapsed={false} onToggle={() => setMobileNavOpen(false)} />
              </div>
            </div>
        )}

        <div className="flex flex-1 flex-col overflow-hidden">
          <div className="flex items-center gap-2 border-b border-line bg-surface px-3 py-2 md:hidden">
            <button onClick={() => setMobileNavOpen(true)} className="rounded-md border border-line px-2 py-1 text-xs">☰</button>
            <span className="text-sm font-semibold">SENTINEL</span>
          </div>
          <div className="hidden md:block">
            <Header lastUpdated={lastUpdated} />
          </div>

          <main className="flex-1 overflow-y-auto p-4 md:p-6">
            {page === "fix-queue" ? (
                <FixQueue chronicIssues={chronic} incidents={incidents} />
            ) : page === "root-causes" ? (
                <RootCauses chronicIssues={chronic} />
            ) : (
                <>
                  <div className="grid grid-cols-2 gap-4 md:grid-cols-4">
                    <KpiCard label="Active Incidents" value={kpis.active}
                             sublabel={`${kpis.activeDeltaWeek >= 0 ? "+" : ""}${kpis.activeDeltaWeek} this week`}
                             tone={kpis.activeDeltaWeek > 0 ? "down" : "up"} />
                    <KpiCard label="Chronic Issues" value={kpis.chronicOpen} sublabel={`${kpis.chronicOpen} require action`} tone="down" />
                    <KpiCard label="Resolved" value={kpis.resolved}
                             sublabel={kpis.resolvedRateThisMonth !== null ? `${kpis.resolvedRateThisMonth}% this month` : undefined} tone="up" />
                    <KpiCard label="Recurring Rate" value={`${kpis.recurringRate}%`} sublabel="of tracked incidents" />
                  </div>

                  <div className="mt-4 grid gap-4 md:grid-cols-2">
                    <TrendChart data={trend} />
                    <CompositionBar {...composition} total={incidents.length} />
                  </div>

                  <div className="mt-6 grid gap-6 lg:grid-cols-[1fr_280px]">
                    <section>
                      <div className="mb-3 flex items-center justify-between">
                        <p className="text-xs uppercase tracking-widest text-dim">Incident Feed {activeService && `· ${activeService}`}</p>
                        {activeService && <button onClick={() => setPage("overview")} className="font-mono text-[11px] text-signal">clear filter</button>}
                      </div>
                      <div className="space-y-3">
                        {filtered.length === 0 ? (
                            <EmptyState icon="✓" title="All clear" message="No active production incidents." />
                        ) : (
                            filtered.map((inc: Incident) => <IncidentCard key={inc.id} incident={inc} onClick={() => setSelected(inc)} />)
                        )}
                      </div>
                    </section>
                    <ServiceHealth services={health} active={activeService} onSelect={(s) => setPage(s ?? "overview")} />
                  </div>

                  <div className="mt-6">
                    <p className="text-xs uppercase tracking-widest text-dim">🔥 Chronic Issues</p>
                    <p className="mb-3 font-mono text-[11px] text-dim">Repeated production failures requiring permanent attention</p>
                    {openChronic.length === 0 ? (
                        <EmptyState icon="✓" title="No chronic issues" message="No recurring incidents currently require permanent remediation." />
                    ) : (
                        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
                          {openChronic.map((c: ChronicIssue) => {
                            const related = incidents.filter((i: Incident) => i.serviceName === c.serviceName && i.title === c.representativeTitle);
                            return <ChronicIssueCard key={c.id} issue={c} relatedIncidents={related} onClick={() => related[0] && setSelected(related[0])} />;
                          })}
                        </div>
                    )}
                  </div>
                </>
            )}
          </main>
        </div>

        {selected && (
            <IncidentDetail incident={selected} allIncidents={incidents} chronicIssues={chronic}
                            onClose={() => setSelected(null)} onChanged={() => setSelected(null)} />
        )}

        <CommandPalette open={paletteOpen} onClose={() => setPaletteOpen(false)}
                        incidents={incidents} chronicIssues={chronic} onSelectIncident={setSelected}
                        onSelectChronic={() => setPage("fix-queue")} />
      </div>
  );
}