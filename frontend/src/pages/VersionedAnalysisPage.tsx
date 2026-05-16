import { useState } from "react";
import { analysisApi } from "../api/analysisApi";
import { ErrorAlert } from "../components/common/ErrorAlert";
import { SchemaEditor } from "../components/schema/SchemaEditor";
import { AnalysisResultView } from "./RawAnalysisPage";
import type { SchemaAnalysisResponse } from "../types/analysis";

const draftSchema = `{
  "type": "record",
  "name": "UserCreated",
  "fields": [
    { "name": "id", "type": "string" },
    { "name": "email", "type": ["null", "string"], "default": null }
  ]
}`;

export function VersionedAnalysisPage() {
  const [subject, setSubject] = useState("user-created");
  const [oldVersion, setOldVersion] = useState("1");
  const [mode, setMode] = useState<"version" | "draft">("draft");
  const [newVersion, setNewVersion] = useState("2");
  const [newSchema, setNewSchema] = useState(draftSchema);
  const [schemaType, setSchemaType] = useState("AVRO");
  const [compatibilityMode, setCompatibilityMode] = useState("BACKWARD");
  const [result, setResult] = useState<SchemaAnalysisResponse | null>(null);
  const [error, setError] = useState("");

  const submit = async () => {
    setError("");
    try {
      setResult(
        await analysisApi.runVersionedAnalysis(subject, {
          oldVersion: Number(oldVersion),
          newVersion: mode === "version" ? Number(newVersion) : undefined,
          newSchema: mode === "draft" ? newSchema : undefined,
          schemaType,
          compatibilityMode,
        }),
      );
    } catch (err) {
      setError(err instanceof Error ? err.message : "Request failed");
    }
  };

  return (
    <div className="space-y-6">
      <div className="panel grid gap-4 p-6 md:grid-cols-2 xl:grid-cols-6">
        <input className="field" value={subject} onChange={(e) => setSubject(e.target.value)} placeholder="subject" />
        <input className="field" value={oldVersion} onChange={(e) => setOldVersion(e.target.value)} placeholder="oldVersion" />
        <select className="field" value={schemaType} onChange={(e) => setSchemaType(e.target.value)}>
          <option value="AVRO">AVRO</option>
        </select>
        <select className="field" value={compatibilityMode} onChange={(e) => setCompatibilityMode(e.target.value)}>
          <option value="BACKWARD">BACKWARD</option>
          <option value="FORWARD">FORWARD</option>
          <option value="FULL">FULL</option>
        </select>
        <select className="field" value={mode} onChange={(e) => setMode(e.target.value as "version" | "draft")}>
          <option value="version">compare with newVersion</option>
          <option value="draft">compare with draft schema</option>
        </select>
        {mode === "version" ? (
          <input className="field" value={newVersion} onChange={(e) => setNewVersion(e.target.value)} placeholder="newVersion" />
        ) : (
          <button className="btn-primary" onClick={submit}>
            Запустить versioned-анализ
          </button>
        )}
        {mode === "version" ? (
          <button className="btn-primary xl:col-span-6" onClick={submit}>
            Запустить versioned-анализ
          </button>
        ) : null}
      </div>
      {mode === "draft" ? (
        <SchemaEditor title="Draft schema" value={newSchema} onChange={setNewSchema} exampleValue={draftSchema} />
      ) : null}
      <ErrorAlert message={error} />
      {result ? <AnalysisResultView result={result} /> : null}
    </div>
  );
}
