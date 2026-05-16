import { useState } from "react";
import { usageApi } from "../api/usageApi";
import { ErrorAlert } from "../components/common/ErrorAlert";
import type { ServiceResponse, ServiceUsageResponse } from "../types/usage";

export function UsageMapPage() {
  const [serviceName, setServiceName] = useState("billing-service");
  const [critical, setCritical] = useState(true);
  const [service, setService] = useState<ServiceResponse | null>(null);
  const [usageSubject, setUsageSubject] = useState("user-created");
  const [usageVersion, setUsageVersion] = useState("1");
  const [usageRole, setUsageRole] = useState<"PRODUCER" | "CONSUMER">("CONSUMER");
  const [usages, setUsages] = useState<ServiceUsageResponse[]>([]);
  const [querySubject, setQuerySubject] = useState("user-created");
  const [error, setError] = useState("");

  const createService = async () => {
    setError("");
    try {
      setService(await usageApi.createService({ name: serviceName, critical }));
    } catch (err) {
      setError(err instanceof Error ? err.message : "Request failed");
    }
  };

  const createUsage = async () => {
    if (!service?.id) {
      setError("Create a service first.");
      return;
    }
    setError("");
    try {
      const created = await usageApi.createUsage(service.id, {
        subject: usageSubject,
        version: Number(usageVersion),
        role: usageRole,
        active: true,
      });
      setUsages((prev) => [created, ...prev]);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Request failed");
    }
  };

  const loadUsages = async () => {
    setError("");
    try {
      setUsages(await usageApi.getSubjectUsages(querySubject));
    } catch (err) {
      setError(err instanceof Error ? err.message : "Request failed");
    }
  };

  return (
    <div className="space-y-6">
      <ErrorAlert message={error} />
      <div className="grid gap-6 xl:grid-cols-2">
        <section className="panel p-6">
          <h3 className="text-lg font-semibold">Create service</h3>
          <div className="mt-4 space-y-4">
            <input className="field" value={serviceName} onChange={(e) => setServiceName(e.target.value)} placeholder="service name" />
            <label className="flex items-center gap-3 text-sm text-slate-700">
              <input type="checkbox" checked={critical} onChange={(e) => setCritical(e.target.checked)} />
              critical
            </label>
            <button className="btn-primary" onClick={createService}>Create service</button>
            {service ? (
              <div className="rounded-2xl bg-slate-50 p-4 text-sm">
                Created service <strong>{service.name}</strong> with id {service.id}
              </div>
            ) : null}
          </div>
        </section>
        <section className="panel p-6">
          <h3 className="text-lg font-semibold">Register usage</h3>
          <div className="mt-4 grid gap-4">
            <input className="field" value={usageSubject} onChange={(e) => setUsageSubject(e.target.value)} placeholder="subject" />
            <input className="field" value={usageVersion} onChange={(e) => setUsageVersion(e.target.value)} placeholder="version" />
            <select className="field" value={usageRole} onChange={(e) => setUsageRole(e.target.value as "PRODUCER" | "CONSUMER")}>
              <option value="PRODUCER">PRODUCER</option>
              <option value="CONSUMER">CONSUMER</option>
            </select>
            <button className="btn-primary" onClick={createUsage}>Add usage</button>
          </div>
        </section>
      </div>
      <section className="panel p-6">
        <div className="flex flex-col gap-4 md:flex-row">
          <input className="field" value={querySubject} onChange={(e) => setQuerySubject(e.target.value)} placeholder="subject" />
          <button className="btn-primary" onClick={loadUsages}>Get usages by subject</button>
        </div>
      </section>
      {usages.length ? (
        <div className="table-wrap">
          <table className="table-base">
            <thead>
              <tr>
                <th>Service</th>
                <th>Role</th>
                <th>Critical</th>
                <th>Subject</th>
                <th>Version</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {usages.map((usage) => (
                <tr key={usage.id}>
                  <td>{usage.serviceName}</td>
                  <td>{usage.role}</td>
                  <td>{usage.critical ? "Yes" : "No"}</td>
                  <td>{usage.subject}</td>
                  <td>{usage.version ?? "-"}</td>
                  <td>{usage.status}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      ) : null}
    </div>
  );
}
