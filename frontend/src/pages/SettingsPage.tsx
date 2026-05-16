import { API_BASE_URL } from "../api/client";
import { statusApi } from "../api/statusApi";
import { useEffect, useState } from "react";
import { StatusBadge } from "../components/common/StatusBadge";
import type { SystemStatusResponse } from "../types/common";

export function SettingsPage() {
  const [status, setStatus] = useState<SystemStatusResponse | null>(null);

  useEffect(() => {
    statusApi.getStatus().then(setStatus).catch(() => undefined);
  }, []);

  return (
    <div className="space-y-6">
      <div className="panel p-6">
        <h3 className="text-lg font-semibold">Backend connectivity</h3>
        <div className="mt-4 rounded-2xl bg-slate-50 p-4 font-mono text-sm">{API_BASE_URL}</div>
        {status ? (
          <div className="mt-4 flex flex-wrap gap-3">
            <StatusBadge value={status.backend} />
            <StatusBadge value={status.schemaRegistry} />
            <StatusBadge value={status.database} />
          </div>
        ) : null}
      </div>
      <div className="grid gap-4 md:grid-cols-2">
        <LinkCard href="http://localhost:8080/swagger-ui/index.html" title="Swagger UI" />
        <LinkCard href="http://localhost:8080/v3/api-docs" title="OpenAPI" />
      </div>
    </div>
  );
}

function LinkCard({ href, title }: { href: string; title: string }) {
  return (
    <a className="panel block p-5" href={href} target="_blank" rel="noreferrer">
      <h4 className="font-semibold text-slate-900">{title}</h4>
      <p className="mt-2 break-all text-sm text-slate-600">{href}</p>
    </a>
  );
}
