import type { ReactNode } from "react";
import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../auth/AuthContext";
import { ApiError } from "../api/client";
import { promotionApi } from "../api/promotionApi";
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
import { SchemaEditor } from "../components/schema/SchemaEditor";
import type { SchemaPromotionResponse } from "../types/promotion";

const draftSchema = `{
  "type": "record",
  "name": "PaymentCreated",
  "fields": [
    { "name": "paymentId", "type": "string" },
    { "name": "currency", "type": "string" },
    { "name": "customerEmail", "type": ["null", "string"], "default": null }
  ]
}`;

const schemaTypes = ["AVRO", "JSON_SCHEMA", "PROTOBUF"];

export function PromotionPage() {
  const navigate = useNavigate();
  const { isAdmin } = useAuth();
  const [subject, setSubject] = useState("payment-created");
  const [schemaText, setSchemaText] = useState(draftSchema);
  const [schemaType, setSchemaType] = useState("AVRO");
  const [compatibilityMode, setCompatibilityMode] = useState("BACKWARD");
  const [description, setDescription] = useState("Frontend controlled promotion");
  const [result, setResult] = useState<SchemaPromotionResponse | null>(null);
  const [error, setError] = useState<ApiError | null>(null);
  const [loading, setLoading] = useState(false);

  const submit = async () => {
    setLoading(true);
    setError(null);
    try {
      setResult(
        await promotionApi.promoteSchema(subject, {
          schemaType,
          compatibilityMode,
          description,
          schemaText,
        }),
      );
    } catch (err) {
      setError(toApiError(err));
    } finally {
      setLoading(false);
    }
  };

  const analysis = result?.analysis ?? null;

  return (
    <div className="space-y-6">
      <div className="panel p-6">
        <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
          <input className="field" value={subject} onChange={(e) => setSubject(e.target.value)} placeholder="subject" />
          <input className="field" value={description} onChange={(e) => setDescription(e.target.value)} placeholder="description" />
          <select className="field" value={schemaType} onChange={(e) => setSchemaType(e.target.value)}>
            {schemaTypes.map((type) => (
              <option key={type} value={type}>
                {type}
              </option>
            ))}
          </select>
          <select className="field" value={compatibilityMode} onChange={(e) => setCompatibilityMode(e.target.value)}>
            <option>BACKWARD</option>
            <option>FORWARD</option>
            <option>FULL</option>
          </select>
        </div>
        {schemaType !== "AVRO" ? (
          <p className="mt-3 text-sm text-slate-500">
            JSON Schema and Protobuf use enhanced project-level analysis before promotion.
          </p>
        ) : null}
      </div>
      <SchemaEditor title="Candidate schema" value={schemaText} onChange={setSchemaText} exampleValue={draftSchema} />
      <div className="flex gap-3">
        <button className="btn-primary" onClick={submit} disabled={loading}>
          {loading ? "Checking..." : "Check and publish"}
        </button>
      </div>
      <ErrorAlert title="Unable to complete controlled promotion" error={error} />
      {result ? (
        <div className="space-y-6">
          <div
            className={`panel p-6 ${
              result.registered
                ? "border-emerald-200 bg-emerald-50/70"
                : result.approvalRequired || result.registrationStatus?.includes("REQUIRES")
                  ? "border-amber-200 bg-amber-50/70"
                  : "border-rose-200 bg-rose-50/70"
            }`}
          >
            <div className="flex flex-wrap items-center gap-3">
              <PromotionStatusBadge value={result.registrationStatus} />
              <span className="text-sm text-slate-700">{result.registrationMessage}</span>
            </div>
            {result.registered ? (
              <p className="mt-3 text-sm text-emerald-800">
                Schema published to Schema Registry.
              </p>
            ) : null}
            {result.approvalRequired ? (
              <div className="mt-3 flex flex-wrap items-center gap-3 text-sm text-amber-900">
                <span>
                  The schema was not published automatically. Approval request #
                  {result.approvalId} has been created and is waiting for an administrator decision.
                </span>
                {isAdmin && result.approvalId ? (
                  <button
                    className="btn-secondary"
                    onClick={() =>
                      navigate(`/admin/schema-approvals?approvalId=${result.approvalId}`)
                    }
                  >
                    Open request
                  </button>
                ) : null}
              </div>
            ) : null}
            <div className="mt-4 grid gap-4 md:grid-cols-4">
              <Summary label="Registered" value={String(result.registered)} />
              <Summary label="Old version" value={String(result.oldVersion ?? "-")} />
              <Summary label="Registered version" value={String(result.registeredVersion ?? "-")} />
              <Summary label="Schema Registry ID" value={String(result.schemaRegistryId ?? "-")} />
            </div>
            {(result.approvalRequired || result.approvalStatus) ? (
              <div className="mt-4 grid gap-4 md:grid-cols-2">
                <Summary label="Approval ID" value={String(result.approvalId ?? "-")} />
                <Summary label="Approval status" value={result.approvalStatus ?? "-"} />
              </div>
            ) : null}
          </div>
          {analysis ? (
            <>
              <div className="grid gap-4 md:grid-cols-4">
                <Summary label="Risk score" value={String(analysis.riskScore)} />
                <Summary label="Risk level" value={<RiskBadge value={analysis.riskLevel} />} />
                <Summary label="Decision" value={<DecisionBadge value={analysis.decision} />} />
                <Summary label="Governance" value={<GovernanceDecisionBadge value={analysis.governanceDecision} />} />
              </div>
              <SchemaDiffViewer
                oldValue={analysis.oldSchemaText ?? result.oldSchemaText}
                newValue={analysis.newSchemaText ?? result.newSchemaText}
              />
              <DiffChangesTable diff={analysis.diff} />
              <RiskFactorsTable factors={analysis.riskFactors} />
              <StructuredRecommendationsTable items={analysis.structuredRecommendations} />
              <ImpactSummaryCard impact={analysis.impact} />
              <ImpactGraphView graph={analysis.impactGraph} />
              {!!analysis.decisionExplanation?.length && (
                <div className="panel p-5">
                  <h3 className="text-lg font-semibold">Decision explanation</h3>
                  <ul className="mt-3 space-y-2 text-sm text-slate-600">
                    {analysis.decisionExplanation.map((item, index) => (
                      <li key={index}>• {item}</li>
                    ))}
                  </ul>
                </div>
              )}
            </>
          ) : (
            <SchemaDiffViewer oldValue={result.oldSchemaText} newValue={result.newSchemaText} />
          )}
        </div>
      ) : null}
    </div>
  );
}

function toApiError(error: unknown) {
  if (error instanceof ApiError) {
    return error;
  }
  return new ApiError("Unable to complete the request.");
}

function Summary({ label, value }: { label: string; value: ReactNode }) {
  return (
    <div className="rounded-2xl bg-white/80 p-4">
      <p className="text-xs uppercase tracking-[0.24em] text-slate-500">{label}</p>
      <div className="mt-2 text-lg font-semibold text-slate-900">{value}</div>
    </div>
  );
}
