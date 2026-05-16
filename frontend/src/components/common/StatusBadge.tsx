interface StatusBadgeProps {
  value?: string | null;
}

const statusStyles: Record<string, string> = {
  UP: "bg-emerald-100 text-emerald-700",
  DOWN: "bg-rose-100 text-rose-700",
};

export function StatusBadge({ value }: StatusBadgeProps) {
  const normalized = value || "UNKNOWN";
  return (
    <span
      className={`inline-flex rounded-full px-3 py-1 text-xs font-semibold ${
        statusStyles[normalized] || "bg-slate-100 text-slate-700"
      }`}
    >
      {normalized}
    </span>
  );
}
