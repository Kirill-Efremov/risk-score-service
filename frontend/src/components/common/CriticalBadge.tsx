interface CriticalBadgeProps {
  critical: boolean;
}

export function CriticalBadge({ critical }: CriticalBadgeProps) {
  return (
    <span
      className={`inline-flex rounded-full px-3 py-1 text-xs font-semibold ${
        critical ? "bg-rose-100 text-rose-700" : "bg-sky-100 text-sky-700"
      }`}
    >
      {critical ? "Critical" : "Standard"}
    </span>
  );
}
