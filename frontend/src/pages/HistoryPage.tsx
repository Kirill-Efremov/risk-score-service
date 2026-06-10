import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { historyApi } from "../api/historyApi";
import { ErrorAlert } from "../components/common/ErrorAlert";
import { PromotionStatusBadge } from "../components/common/PromotionStatusBadge";
import { GovernanceDecisionBadge } from "../components/risk/GovernanceDecisionBadge";
import { RiskBadge } from "../components/risk/RiskBadge";
import type { AnalysisRecordResponse } from "../types/analysis";

export function HistoryPage() {
  const [subject, setSubject] = useState("user-created");
  const [records, setRecords] = useState<AnalysisRecordResponse[]>([]);
  const [error, setError] = useState("");
  const navigate = useNavigate();

  const loadHistory = async () => {
    setError("");
    try {
      setRecords(await historyApi.getSubjectAnalysisHistory(subject));
    } catch (err) {
      setError(err instanceof Error ? err.message : "Request failed");
    }
  };

  return (
    <div className="space-y-6">
      <div className="panel flex flex-col gap-4 p-6 md:flex-row">
        <input
          className="field"
          value={subject}
          onChange={(e) => setSubject(e.target.value)}
          placeholder="subject"
        />
        <button className="btn-primary" onClick={loadHistory}>
          Load history
        </button>
      </div>
      <ErrorAlert message={error} />
      {records.length ? (
        <div className="table-wrap">
          <table className="table-base">
            <thead>
              <tr>
                <th>id</th>
                <th>subject</th>
                <th>versions</th>
                <th>risk</th>
                <th>decision</th>
                <th>governance</th>
                <th>promotion</th>
                <th>createdAt</th>
                <th>createdBy</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {records.map((record) => (
                <tr
                  key={record.id}
                  className="cursor-pointer hover:bg-slate-50"
                  onClick={() => navigate(`/history/${record.id}`)}
                >
                  <td>{record.id}</td>
                  <td>{record.subject}</td>
                  <td>
                    {record.oldVersion ?? "-"} → {record.newVersion ?? "-"}
                  </td>
                  <td>
                    <div className="flex items-center gap-2">
                      <span>{record.riskScore}</span>
                      <RiskBadge value={record.riskLevel} />
                    </div>
                  </td>
                  <td>{record.decision}</td>
                  <td>
                    <GovernanceDecisionBadge value={record.governanceDecision} />
                  </td>
                  <td>
                    {record.promotionAttempted ? (
                      <PromotionStatusBadge value={record.registrationStatus} />
                    ) : (
                      "No"
                    )}
                  </td>
                  <td>{record.createdAt}</td>
                  <td>{record.createdBy ?? "-"}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      ) : null}
    </div>
  );
}
