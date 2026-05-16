import type { ReactNode } from "react";
import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { historyApi } from "../api/historyApi";
import { ErrorAlert } from "../components/common/ErrorAlert";
import { PromotionStatusBadge } from "../components/common/PromotionStatusBadge";
import { ImpactGraphView } from "../components/impact/ImpactGraphView";
import { ImpactSummaryCard } from "../components/impact/ImpactSummaryCard";
import { StructuredRecommendationsTable } from "../components/recommendations/StructuredRecommendationsTable";
import { DecisionBadge } from "../components/risk/DecisionBadge";
import { GovernanceDecisionBadge } from "../components/risk/GovernanceDecisionBadge";
import { RiskBadge } from "../components/risk/RiskBadge";
import { RiskFactorsTable } from "../components/risk/RiskFactorsTable";
import { DiffChangesTable } from "../components/schema/DiffChangesTable";
import { SchemaDiffViewer } from "../components/schema/SchemaDiffViewer";
import type { AnalysisRecordResponse } from "../types/analysis";

export function AnalysisDetailPage() {
  const { id = "" } = useParams();
  const [record, setRecord] = useState<AnalysisRecordResponse | null>(null);
  const [error, setError] = useState("");

  useEffect(() => {
    historyApi.getAnalysisById(id).then(setRecord).catch((err) => setError(err.message));
  }, [id]);

  if (error) return <ErrorAlert message={error} />;
  if (!record) return null;

  return (
    <div className="space-y-6">
      <div className="grid gap-4 md:grid-cols-4">
        <Summary label="Risk score" value={String(record.riskScore)} />
        <Summary label="Risk level" value={<RiskBadge value={record.riskLevel} />} />
        <Summary label="Decision" value={<DecisionBadge value={record.decision} />} />
        <Summary label="Governance" value={<GovernanceDecisionBadge value={record.governanceDecision} />} />
      </div>
      <div className="panel p-5">
        <div className="flex flex-wrap items-center gap-3">
          <PromotionStatusBadge value={record.registrationStatus} />
          <span className="text-sm text-slate-600">
            registered={String(record.registered)} version={record.registeredVersion ?? "-"} schemaRegistryId={record.schemaRegistryId ?? "-"}
          </span>
        </div>
      </div>
      <SchemaDiffViewer oldValue={record.oldSchemaText} newValue={record.newSchemaText} />
      <DiffChangesTable diff={record.diff} />
      <RiskFactorsTable factors={record.riskFactors} />
      <StructuredRecommendationsTable items={record.structuredRecommendations} />
      <ImpactSummaryCard impact={record.impact} />
      <ImpactGraphView graph={record.impactGraph} />
      {!!record.decisionExplanation?.length && (
        <div className="panel p-5">
          <h3 className="text-lg font-semibold">Decision explanation</h3>
          <ul className="mt-3 space-y-2 text-sm text-slate-600">
            {record.decisionExplanation.map((item, index) => (
              <li key={index}>• {item}</li>
            ))}
          </ul>
        </div>
      )}
    </div>
  );
}

function Summary({ label, value }: { label: string; value: ReactNode }) {
  return (
    <div className="panel p-5">
      <p className="text-xs uppercase tracking-[0.24em] text-slate-500">{label}</p>
      <div className="mt-3 text-lg font-semibold text-slate-900">{value}</div>
    </div>
  );
}
