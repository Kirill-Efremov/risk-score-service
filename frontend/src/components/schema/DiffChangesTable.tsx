import type { DiffResult } from "../../types/analysis";

export function DiffChangesTable({ diff }: { diff?: DiffResult | null }) {
  if (!diff?.changes?.length) return null;
  return (
    <div className="table-wrap">
      <table className="table-base">
        <thead>
          <tr>
            <th>fieldName</th>
            <th>type</th>
            <th>oldType</th>
            <th>newType</th>
            <th>oldDefault</th>
            <th>newDefault</th>
          </tr>
        </thead>
        <tbody className="divide-y divide-slate-100">
          {diff.changes.map((change, index) => (
            <tr key={`${change.fieldName}-${index}`}>
              <td className="font-mono text-xs">{change.fieldName}</td>
              <td>{change.type}</td>
              <td>{change.oldType ?? "-"}</td>
              <td>{change.newType ?? "-"}</td>
              <td>{change.oldDefault ?? "-"}</td>
              <td>{change.newDefault ?? "-"}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
