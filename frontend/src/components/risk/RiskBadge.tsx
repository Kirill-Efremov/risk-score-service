const riskStyles: Record<string, string> = {
  LOW: "bg-emerald-100 text-emerald-700",
  MEDIUM: "bg-amber-100 text-amber-700",
  HIGH: "bg-rose-100 text-rose-700",
};

export function RiskBadge({ value }: { value?: string | null }) {
  if (!value) return null;
  return (
    <span
      className={`inline-flex rounded-full px-3 py-1 text-xs font-semibold ${
        riskStyles[value] || "bg-slate-100 text-slate-700"
      }`}
    >
      {value}
    </span>
  );
}
