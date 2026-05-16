const governanceStyles: Record<string, string> = {
  ALLOW: "bg-emerald-100 text-emerald-700",
  ALLOW_WITH_CAUTION: "bg-amber-100 text-amber-700",
  REQUIRE_CONSUMER_UPGRADE_FIRST: "bg-orange-100 text-orange-700",
  REJECT: "bg-rose-100 text-rose-700",
  SUGGEST_NEW_SUBJECT: "bg-fuchsia-100 text-fuchsia-700",
};

export function GovernanceDecisionBadge({
  value,
}: {
  value?: string | null;
}) {
  if (!value) return null;
  return (
    <span
      className={`inline-flex rounded-full px-3 py-1 text-xs font-semibold ${
        governanceStyles[value] || "bg-slate-100 text-slate-700"
      }`}
    >
      {value}
    </span>
  );
}
