import type { RiskFactor } from "../../types/analysis";

export function RiskFactorsTable({ factors }: { factors: RiskFactor[] }) {
  if (!factors?.length) return null;
  return (
    <div className="table-wrap">
      <table className="table-base">
        <thead>
          <tr>
            <th>Source</th>
            <th>Code</th>
            <th>Message</th>
            <th>Weight</th>
          </tr>
        </thead>
        <tbody className="divide-y divide-slate-100">
          {factors.map((factor) => (
            <tr key={`${factor.source}-${factor.code}-${factor.message}`}>
              <td>{factor.source}</td>
              <td>{factor.code}</td>
              <td>{factor.message}</td>
              <td>{factor.weight}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
