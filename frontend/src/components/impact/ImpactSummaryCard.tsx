import type { ImpactResponse } from "../../types/analysis";

export function ImpactSummaryCard({ impact }: { impact?: ImpactResponse | null }) {
  if (!impact) return null;
  return (
    <div className="panel p-5">
      <h3 className="text-lg font-semibold">Impact summary</h3>
      <div className="mt-4 grid gap-4 md:grid-cols-4">
        <Metric label="Consumers" value={impact.affectedConsumersCount} />
        <Metric label="Producers" value={impact.affectedProducersCount} />
        <Metric label="Critical services" value={impact.criticalServices.length} />
        <Metric label="Breaking" value={impact.breaking ? "Yes" : "No"} />
      </div>
      {impact.criticalServices.length ? (
        <p className="mt-4 text-sm text-slate-600">
          Critical: {impact.criticalServices.join(", ")}
        </p>
      ) : null}
    </div>
  );
}

function Metric({ label, value }: { label: string; value: string | number }) {
  return (
    <div className="rounded-2xl bg-slate-50 p-4">
      <p className="text-xs uppercase tracking-wide text-slate-500">{label}</p>
      <p className="mt-2 text-2xl font-semibold text-slate-900">{value}</p>
    </div>
  );
}
