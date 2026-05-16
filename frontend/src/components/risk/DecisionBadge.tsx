const decisionStyles: Record<string, string> = {
  ALLOW: "bg-emerald-100 text-emerald-700",
  WARN: "bg-amber-100 text-amber-700",
  BLOCK: "bg-rose-100 text-rose-700",
};

export function DecisionBadge({ value }: { value?: string | null }) {
  if (!value) return null;
  return (
    <span
      className={`inline-flex rounded-full px-3 py-1 text-xs font-semibold ${
        decisionStyles[value] || "bg-slate-100 text-slate-700"
      }`}
    >
      {value}
    </span>
  );
}
