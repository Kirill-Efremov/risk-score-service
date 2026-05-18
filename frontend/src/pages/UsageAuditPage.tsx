import { useEffect, useState } from "react";
import { ApiError } from "../api/client";
import { usageAuditApi } from "../api/usageAuditApi";
import { ErrorAlert } from "../components/common/ErrorAlert";
import { ServiceUsageAuditTable } from "../components/usage/ServiceUsageAuditTable";
import type {
  ServiceUsageAuditAction,
  ServiceUsageAuditResponse,
} from "../types/usageAudit";

const auditActionOptions: Array<"all" | ServiceUsageAuditAction> = [
  "all",
  "SERVICE_CREATED",
  "SERVICE_UPDATED",
  "SERVICE_DEACTIVATED",
  "USAGE_CREATED",
  "USAGE_UPDATED",
  "USAGE_DEACTIVATED",
  "USAGE_MIGRATED",
];

export function UsageAuditPage() {
  const [records, setRecords] = useState<ServiceUsageAuditResponse[]>([]);
  const [serviceId, setServiceId] = useState("");
  const [usageId, setUsageId] = useState("");
  const [action, setAction] = useState<"all" | ServiceUsageAuditAction>("all");
  const [limit, setLimit] = useState(100);
  const [error, setError] = useState("");

  useEffect(() => {
    void loadAudit();
  }, []);

  const loadAudit = async () => {
    setError("");
    try {
      setRecords(
        await usageAuditApi.getUsageAuditLog({
          serviceId: serviceId ? Number(serviceId) : undefined,
          usageId: usageId ? Number(usageId) : undefined,
          action: action === "all" ? undefined : action,
          limit,
        }),
      );
    } catch (err) {
      setError(toErrorMessage(err));
    }
  };

  return (
    <div className="space-y-6">
      <ErrorAlert message={error} />
      <section className="panel p-6">
        <div className="flex flex-col gap-4 md:flex-row md:items-end">
          <input
            className="field"
            placeholder="Service ID"
            value={serviceId}
            onChange={(e) => setServiceId(e.target.value)}
          />
          <input
            className="field"
            placeholder="Usage ID"
            value={usageId}
            onChange={(e) => setUsageId(e.target.value)}
          />
          <select
            className="field"
            value={action}
            onChange={(e) =>
              setAction(e.target.value as "all" | ServiceUsageAuditAction)
            }
          >
            {auditActionOptions.map((item) => (
              <option key={item} value={item}>
                {item === "all" ? "All actions" : item}
              </option>
            ))}
          </select>
          <input
            className="field"
            type="number"
            min="1"
            max="200"
            placeholder="Limit"
            value={limit}
            onChange={(e) => setLimit(Number(e.target.value))}
          />
          <button className="btn-primary" onClick={() => void loadAudit()}>
            Load audit
          </button>
        </div>
      </section>
      <section className="panel p-6">
        <ServiceUsageAuditTable records={records} />
      </section>
    </div>
  );
}

function toErrorMessage(error: unknown) {
  if (error instanceof ApiError && error.payload) {
    return `${error.payload.errorCode}: ${error.payload.message}`;
  }
  return error instanceof Error ? error.message : "Request failed";
}
