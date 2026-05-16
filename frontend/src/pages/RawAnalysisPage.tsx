import type { ReactNode } from "react";
import { useState } from "react";
import { analysisApi } from "../api/analysisApi";
import { ErrorAlert } from "../components/common/ErrorAlert";
import { DecisionBadge } from "../components/risk/DecisionBadge";
import { GovernanceDecisionBadge } from "../components/risk/GovernanceDecisionBadge";
import { RiskBadge } from "../components/risk/RiskBadge";
import { StructuredRecommendationsTable } from "../components/recommendations/StructuredRecommendationsTable";
import { DiffChangesTable } from "../components/schema/DiffChangesTable";
import { SchemaDiffViewer } from "../components/schema/SchemaDiffViewer";
import { SchemaEditor } from "../components/schema/SchemaEditor";
import { RiskFactorsTable } from "../components/risk/RiskFactorsTable";
import type { SchemaAnalysisResponse } from "../types/analysis";

const sampleOld = `{
  "type": "record",
  "name": "UserCreated",
  "fields": [
    { "name": "id", "type": "string" }
  ]
}`;

const sampleNew = `{
  "type": "record",
  "name": "UserCreated",
  "fields": [
    { "name": "id", "type": "string" },
    { "name": "email", "type": ["null", "string"], "default": null }
  ]
}`;

export function RawAnalysisPage() {
  const [oldSchema, setOldSchema] = useState(sampleOld);
  const [newSchema, setNewSchema] = useState(sampleNew);
  const [result, setResult] = useState<SchemaAnalysisResponse | null>(null);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const submit = async () => {
    setLoading(true);
    setError("");
    try {
      setResult(
        await analysisApi.runRawAnalysis({
          schemaType: "AVRO",
          compatibilityMode: "BACKWARD",
          oldSchema,
          newSchema,
        }),
      );
    } catch (err) {
      setError(err instanceof Error ? err.message : "Request failed");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="space-y-6">
      <div className="panel p-6">
        <p className="text-sm text-slate-600">
          Raw-анализ не использует Schema Registry и не публикует схему.
        </p>
      </div>
      <div className="grid gap-6 xl:grid-cols-2">
        <SchemaEditor title="Old schema" value={oldSchema} onChange={setOldSchema} exampleValue={sampleOld} />
        <SchemaEditor title="New schema" value={newSchema} onChange={setNewSchema} exampleValue={sampleNew} />
      </div>
      <div className="flex gap-3">
        <button className="btn-primary" onClick={submit} disabled={loading}>
          {loading ? "Анализируем..." : "Запустить анализ"}
        </button>
      </div>
      <ErrorAlert message={error} />
      {result ? <AnalysisResultView result={result} /> : null}
    </div>
  );
}

export function AnalysisResultView({ result }: { result: SchemaAnalysisResponse }) {
  return (
    <div className="space-y-6">
      <div className="grid gap-4 md:grid-cols-4">
        <Summary label="Risk score" value={String(result.riskScore)} />
        <Summary label="Risk level" value={<RiskBadge value={result.riskLevel} />} />
        <Summary label="Decision" value={<DecisionBadge value={result.decision} />} />
        <Summary label="Governance" value={<GovernanceDecisionBadge value={result.governanceDecision} />} />
      </div>
      <SchemaDiffViewer oldValue={result.oldSchemaText} newValue={result.newSchemaText} />
      <DiffChangesTable diff={result.diff} />
      <RiskFactorsTable factors={result.riskFactors} />
      <StructuredRecommendationsTable items={result.structuredRecommendations} />
      {!!result.decisionExplanation?.length && (
        <div className="panel p-5">
          <h3 className="text-lg font-semibold">Decision explanation</h3>
          <ul className="mt-3 space-y-2 text-sm text-slate-600">
            {result.decisionExplanation.map((item, index) => (
              <li key={index}>• {item}</li>
            ))}
          </ul>
        </div>
      )}
      {!!result.issues?.length && (
        <div className="panel p-5">
          <h3 className="text-lg font-semibold">Issues</h3>
          <pre className="mt-3 overflow-auto rounded-2xl bg-slate-950 p-4 text-xs text-slate-100">
            {JSON.stringify(result.issues, null, 2)}
          </pre>
        </div>
      )}
    </div>
  );
}

function Summary({ label, value }: { label: string; value: ReactNode }) {
  return (
    <div className="panel p-5">
      <p className="text-xs uppercase tracking-[0.24em] text-slate-500">{label}</p>
      <div className="mt-3 text-xl font-semibold">{value}</div>
    </div>
  );
}
