import type { ServiceUsageAuditAction } from "../../types/usageAudit";

interface AuditActionBadgeProps {
  action: ServiceUsageAuditAction;
}

const actionStyles: Record<ServiceUsageAuditAction, string> = {
  SERVICE_CREATED: "bg-emerald-100 text-emerald-700",
  SERVICE_UPDATED: "bg-sky-100 text-sky-700",
  SERVICE_DEACTIVATED: "bg-rose-100 text-rose-700",
  USAGE_CREATED: "bg-emerald-100 text-emerald-700",
  USAGE_UPDATED: "bg-sky-100 text-sky-700",
  USAGE_DEACTIVATED: "bg-rose-100 text-rose-700",
  USAGE_MIGRATED: "bg-violet-100 text-violet-700",
};

export function AuditActionBadge({ action }: AuditActionBadgeProps) {
  return (
    <span
      className={`inline-flex rounded-full px-3 py-1 text-xs font-semibold ${actionStyles[action]}`}
    >
      {action}
    </span>
  );
}
