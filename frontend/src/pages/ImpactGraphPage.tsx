import { useState } from "react";
import { usageApi } from "../api/usageApi";
import { ErrorAlert } from "../components/common/ErrorAlert";
import { ImpactGraphView } from "../components/impact/ImpactGraphView";
import type { ImpactGraph } from "../types/graph";

export function ImpactGraphPage() {
  const [subject, setSubject] = useState("user-created");
  const [graph, setGraph] = useState<ImpactGraph | null>(null);
  const [error, setError] = useState("");

  const loadGraph = async () => {
    setError("");
    try {
      setGraph(await usageApi.getSubjectGraph(subject));
    } catch (err) {
      setError(err instanceof Error ? err.message : "Request failed");
    }
  };

  return (
    <div className="space-y-6">
      <div className="panel flex flex-col gap-4 p-6 md:flex-row">
        <input className="field" value={subject} onChange={(e) => setSubject(e.target.value)} placeholder="subject" />
        <button className="btn-primary" onClick={loadGraph}>Показать граф</button>
      </div>
      <ErrorAlert message={error} />
      <ImpactGraphView graph={graph} />
    </div>
  );
}
