import type { ReactNode } from "react";
import { useEffect, useState } from "react";
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

const samples = {
  AVRO: {
    oldSchema: `{
  "type": "record",
  "name": "UserCreated",
  "fields": [
    { "name": "id", "type": "string" }
  ]
}`,
    newSchema: `{
  "type": "record",
  "name": "UserCreated",
  "fields": [
    { "name": "id", "type": "string" },
    { "name": "email", "type": ["null", "string"], "default": null }
  ]
}`,
  },
  JSON_SCHEMA: {
    oldSchema: `{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "type": "object",
  "properties": {
    "id": { "type": "string" },
    "email": { "type": "string", "minLength": 3 },
    "address": {
      "type": "object",
      "properties": {
        "zipCode": { "type": "string" }
      }
    }
  },
  "required": ["id", "email"],
  "additionalProperties": true
}`,
    newSchema: `{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "type": "object",
  "properties": {
    "id": { "type": "string" },
    "email": { "type": "string", "minLength": 10 },
    "middleName": { "type": "string" },
    "address": {
      "type": "object",
      "properties": {
        "zipCode": { "type": "integer" }
      }
    }
  },
  "required": ["id", "email"],
  "additionalProperties": false
}`,
  },
  PROTOBUF: {
    oldSchema: `syntax = "proto3";

message UserCreated {
  string id = 1;
  string email = 2;
}`,
    newSchema: `syntax = "proto3";

message UserCreated {
  string id = 1;
  int32 email = 2;
  string middle_name = 3;
}`,
  },
};

const schemaTypes = ["AVRO", "JSON_SCHEMA", "PROTOBUF"];

export function RawAnalysisPage() {
  const [schemaType, setSchemaType] = useState("AVRO");
  const [oldSchema, setOldSchema] = useState(samples.AVRO.oldSchema);
  const [newSchema, setNewSchema] = useState(samples.AVRO.newSchema);
  const [result, setResult] = useState<SchemaAnalysisResponse | null>(null);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const submit = async () => {
    setLoading(true);
    setError("");
    try {
      setResult(
        await analysisApi.runRawAnalysis({
          schemaType,
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

  useEffect(() => {
    const sample = samples[schemaType as keyof typeof samples];
    setOldSchema(sample.oldSchema);
    setNewSchema(sample.newSchema);
    setResult(null);
    setError("");
  }, [schemaType]);

  return (
    <div className="space-y-6">
      <div className="panel p-6">
        <div className="grid gap-4 md:grid-cols-[220px_1fr]">
          <select
            className="field"
            value={schemaType}
            onChange={(event) => setSchemaType(event.target.value)}
          >
            {schemaTypes.map((type) => (
              <option key={type} value={type}>
                {type}
              </option>
            ))}
          </select>
        </div>
      </div>
      <div className="grid gap-6 xl:grid-cols-2">
        <SchemaEditor
          title="Old schema"
          value={oldSchema}
          onChange={setOldSchema}
          exampleValue={samples[schemaType as keyof typeof samples].oldSchema}
        />
        <SchemaEditor
          title="New schema"
          value={newSchema}
          onChange={setNewSchema}
          exampleValue={samples[schemaType as keyof typeof samples].newSchema}
        />
      </div>
      <div className="flex gap-3">
        <button className="btn-primary" onClick={submit} disabled={loading}>
          {loading ? "Analyzing..." : "Run analysis"}
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
              <li key={index}>- {item}</li>
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
