import type { StructuredRecommendation } from "../../types/analysis";

export function StructuredRecommendationsTable({
  items,
}: {
  items: StructuredRecommendation[];
}) {
  if (!items?.length) return null;
  return (
    <div className="table-wrap">
      <table className="table-base">
        <thead>
          <tr>
            <th>Severity</th>
            <th>Code</th>
            <th>Target</th>
            <th>Message</th>
            <th>Action</th>
          </tr>
        </thead>
        <tbody className="divide-y divide-slate-100">
          {items.map((item) => (
            <tr key={`${item.code}-${item.target}`}>
              <td>{item.severity}</td>
              <td>{item.code}</td>
              <td>{item.target}</td>
              <td>{item.message}</td>
              <td>{item.action}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
