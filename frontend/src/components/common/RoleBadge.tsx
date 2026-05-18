interface RoleBadgeProps {
  role: "PRODUCER" | "CONSUMER";
}

export function RoleBadge({ role }: RoleBadgeProps) {
  return (
    <span
      className={`inline-flex rounded-full px-3 py-1 text-xs font-semibold ${
        role === "PRODUCER"
          ? "bg-sky-100 text-sky-700"
          : "bg-violet-100 text-violet-700"
      }`}
    >
      {role}
    </span>
  );
}
