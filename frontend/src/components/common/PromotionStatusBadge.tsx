const promotionStyles: Record<string, string> = {
  REGISTERED: "bg-emerald-100 text-emerald-700",
  REQUIRES_MANUAL_APPROVAL: "bg-amber-100 text-amber-700",
  REQUIRES_CONSUMER_UPGRADE: "bg-orange-100 text-orange-700",
  BLOCKED_BY_GOVERNANCE: "bg-rose-100 text-rose-700",
  SUGGEST_NEW_SUBJECT: "bg-fuchsia-100 text-fuchsia-700",
  REGISTRY_REJECTED: "bg-rose-100 text-rose-700",
  ANALYSIS_ONLY: "bg-slate-100 text-slate-700",
};

export function PromotionStatusBadge({ value }: { value?: string | null }) {
  if (!value) return null;
  return (
    <span
      className={`inline-flex rounded-full px-3 py-1 text-xs font-semibold ${
        promotionStyles[value] || "bg-slate-100 text-slate-700"
      }`}
    >
      {value}
    </span>
  );
}
