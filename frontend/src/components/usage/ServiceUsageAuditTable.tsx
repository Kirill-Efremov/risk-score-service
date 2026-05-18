import { AuditActionBadge } from "../common/AuditActionBadge";
import type { ServiceUsageAuditResponse } from "../../types/usageAudit";

interface ServiceUsageAuditTableProps {
  records: ServiceUsageAuditResponse[];
}

function formatValue(value?: number | boolean | string | null) {
  return value ?? "-";
}

export function ServiceUsageAuditTable({
  records,
}: ServiceUsageAuditTableProps) {
  if (!records.length) {
    return <p className="text-sm text-slate-500">No audit records yet.</p>;
  }

  return (
    <div className="table-wrap">
      <table className="table-base">
        <thead>
          <tr>
            <th>Created at</th>
            <th>Action</th>
            <th>Usage ID</th>
            <th>Old version</th>
            <th>New version</th>
            <th>Old active</th>
            <th>New active</th>
            <th>Changed by</th>
          </tr>
        </thead>
        <tbody className="divide-y divide-slate-100">
          {records.map((record) => (
            <tr key={record.id}>
              <td>{new Date(record.createdAt).toLocaleString()}</td>
              <td>
                <AuditActionBadge action={record.action} />
              </td>
              <td>{formatValue(record.usageId)}</td>
              <td>{formatValue(record.oldVersion)}</td>
              <td>{formatValue(record.newVersion)}</td>
              <td>{formatValue(record.oldActive)}</td>
              <td>{formatValue(record.newActive)}</td>
              <td>{formatValue(record.changedBy)}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
