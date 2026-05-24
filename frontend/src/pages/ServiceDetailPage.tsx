import { useEffect, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { useAuth } from "../auth/AuthContext";
import { ApiError } from "../api/client";
import { servicesApi } from "../api/servicesApi";
import { usageApi } from "../api/usageApi";
import { usageAuditApi } from "../api/usageAuditApi";
import { ActiveBadge } from "../components/common/ActiveBadge";
import { CriticalBadge } from "../components/common/CriticalBadge";
import { ErrorAlert } from "../components/common/ErrorAlert";
import { RoleBadge } from "../components/common/RoleBadge";
import { ServiceUsageAuditTable } from "../components/usage/ServiceUsageAuditTable";
import type {
  CreateServiceUsageRequest,
  MigrateServiceUsageRequest,
  ServiceResponse,
  ServiceUsageResponse,
  UpdateServiceUsageRequest,
} from "../types/usage";
import type { ServiceUsageAuditResponse } from "../types/usageAudit";

type ActiveFilter = "all" | "active" | "inactive";
type RoleFilter = "all" | "PRODUCER" | "CONSUMER";
type DetailTab = "usages" | "history";

const emptyCreateUsageForm: CreateServiceUsageRequest = {
  subject: "",
  version: 1,
  role: "CONSUMER",
};

const emptyEditUsageForm: UpdateServiceUsageRequest = {
  subject: "",
  version: 1,
  role: "CONSUMER",
  active: true,
};

const emptyMigrateForm: MigrateServiceUsageRequest = {
  targetVersion: 1,
};

export function ServiceDetailPage() {
  const navigate = useNavigate();
  const { isAdmin } = useAuth();
  const { serviceId } = useParams();
  const parsedServiceId = Number(serviceId);
  const [service, setService] = useState<ServiceResponse | null>(null);
  const [usages, setUsages] = useState<ServiceUsageResponse[]>([]);
  const [auditRecords, setAuditRecords] = useState<ServiceUsageAuditResponse[]>(
    [],
  );
  const [usageAuditRecords, setUsageAuditRecords] = useState<
    ServiceUsageAuditResponse[]
  >([]);
  const [selectedUsageAuditId, setSelectedUsageAuditId] = useState<
    number | null
  >(null);
  const [selectedTab, setSelectedTab] = useState<DetailTab>("usages");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const [usageActiveFilter, setUsageActiveFilter] =
    useState<ActiveFilter>("active");
  const [usageRoleFilter, setUsageRoleFilter] = useState<RoleFilter>("all");
  const [subjectFilter, setSubjectFilter] = useState("");
  const [auditLimit, setAuditLimit] = useState(20);
  const [createUsageForm, setCreateUsageForm] = useState<CreateServiceUsageRequest>(
    emptyCreateUsageForm,
  );
  const [editingUsageId, setEditingUsageId] = useState<number | null>(null);
  const [editUsageForm, setEditUsageForm] =
    useState<UpdateServiceUsageRequest>(emptyEditUsageForm);
  const [migratingUsageId, setMigratingUsageId] = useState<number | null>(null);
  const [migrateForm, setMigrateForm] =
    useState<MigrateServiceUsageRequest>(emptyMigrateForm);

  useEffect(() => {
    if (Number.isNaN(parsedServiceId)) {
      navigate("/services", { replace: true });
      return;
    }
    void loadPage();
  }, [parsedServiceId, usageActiveFilter, usageRoleFilter]);

  const loadPage = async () => {
    setLoading(true);
    setError("");
    try {
      const [serviceData, usageData] = await Promise.all([
        servicesApi.getService(parsedServiceId),
        usageApi.getServiceUsages(parsedServiceId, {
          active:
            usageActiveFilter === "all"
              ? undefined
              : usageActiveFilter === "active",
          role: usageRoleFilter === "all" ? undefined : usageRoleFilter,
        }),
      ]);
      setService(serviceData);
      setUsages(usageData);
    } catch (err) {
      setError(toErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  const loadAudit = async () => {
    setError("");
    try {
      setAuditRecords(
        await usageAuditApi.getServiceAudit(parsedServiceId, {
          limit: auditLimit,
        }),
      );
    } catch (err) {
      setError(toErrorMessage(err));
    }
  };

  const loadUsageAudit = async (usageId: number) => {
    setError("");
    try {
      setUsageAuditRecords(
        await usageAuditApi.getUsageAudit(parsedServiceId, usageId, {
          limit: auditLimit,
        }),
      );
      setSelectedUsageAuditId(usageId);
      setSelectedTab("history");
      await loadAudit();
    } catch (err) {
      setError(toErrorMessage(err));
    }
  };

  const createUsage = async () => {
    setError("");
    try {
      await usageApi.createUsage(parsedServiceId, {
        subject: createUsageForm.subject.trim(),
        version: createUsageForm.version,
        role: createUsageForm.role,
      });
      setCreateUsageForm(emptyCreateUsageForm);
      await loadPage();
      if (selectedTab === "history") {
        await loadAudit();
      }
    } catch (err) {
      setError(toErrorMessage(err));
    }
  };

  const startEditingUsage = (usage: ServiceUsageResponse) => {
    setMigratingUsageId(null);
    setEditingUsageId(usage.id);
    setEditUsageForm({
      subject: usage.subject,
      version: usage.version ?? 1,
      role: usage.role,
      active: usage.active,
    });
  };

  const saveUsage = async () => {
    if (editingUsageId === null) {
      return;
    }

    setError("");
    try {
      await usageApi.updateServiceUsage(parsedServiceId, editingUsageId, {
        subject: editUsageForm.subject?.trim(),
        version: editUsageForm.version,
        role: editUsageForm.role,
        active: editUsageForm.active,
      });
      setEditingUsageId(null);
      setEditUsageForm(emptyEditUsageForm);
      await loadPage();
      if (selectedTab === "history") {
        await loadAudit();
      }
    } catch (err) {
      setError(toErrorMessage(err));
    }
  };

  const deactivateUsage = async (usageId: number) => {
    const confirmed = window.confirm("Deactivate this usage link?");
    if (!confirmed) {
      return;
    }

    setError("");
    try {
      await usageApi.deactivateServiceUsage(parsedServiceId, usageId);
      if (editingUsageId === usageId) {
        setEditingUsageId(null);
        setEditUsageForm(emptyEditUsageForm);
      }
      if (migratingUsageId === usageId) {
        setMigratingUsageId(null);
        setMigrateForm(emptyMigrateForm);
      }
      await loadPage();
      if (selectedTab === "history") {
        await loadAudit();
      }
    } catch (err) {
      setError(toErrorMessage(err));
    }
  };

  const startMigrating = (usage: ServiceUsageResponse) => {
    setEditingUsageId(null);
    setMigratingUsageId(usage.id);
    setMigrateForm({
      targetVersion: usage.version ? usage.version + 1 : 1,
    });
  };

  const migrateUsage = async () => {
    if (migratingUsageId === null) {
      return;
    }

    setError("");
    try {
      await usageApi.migrateServiceUsage(
        parsedServiceId,
        migratingUsageId,
        migrateForm,
      );
      setMigratingUsageId(null);
      setMigrateForm(emptyMigrateForm);
      await loadPage();
      setSelectedTab("history");
      await loadAudit();
    } catch (err) {
      setError(toErrorMessage(err));
    }
  };

  const deactivateService = async () => {
    if (!service) {
      return;
    }

    const confirmed = window.confirm(
      "Deactivate this service and all of its active usage links?",
    );
    if (!confirmed) {
      return;
    }

    setError("");
    try {
      await servicesApi.deactivateService(service.id);
      await loadPage();
      setSelectedTab("history");
      await loadAudit();
    } catch (err) {
      setError(toErrorMessage(err));
    }
  };

  const activateService = async () => {
    if (!service) {
      return;
    }

    setError("");
    try {
      await servicesApi.updateService(service.id, {
        active: true,
      });
      await loadPage();
    } catch (err) {
      setError(toErrorMessage(err));
    }
  };

  const visibleUsages = usages.filter((usage) =>
    subjectFilter.trim()
      ? usage.subject.toLowerCase().includes(subjectFilter.trim().toLowerCase())
      : true,
  );

  return (
    <div className="space-y-6">
      <ErrorAlert message={error} />

      <section className="panel p-6">
        <div className="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
          <div>
            <Link
              to="/services"
              className="text-sm font-medium text-teal-700 hover:text-teal-800"
            >
              Back to services
            </Link>
            <h2 className="mt-2 text-2xl font-semibold text-slate-900">
              {service?.name ?? "Service card"}
            </h2>
          </div>
          <div className="flex flex-wrap items-center gap-2">
            {service ? <CriticalBadge critical={service.critical} /> : null}
            {service ? <ActiveBadge active={service.active} /> : null}
            <button className="btn-secondary" onClick={() => void loadPage()}>
              Refresh
            </button>
            {isAdmin && service ? (
              <button
                className="btn-secondary"
                onClick={() =>
                  service.active
                    ? void deactivateService()
                    : void activateService()
                }
              >
                {service.active ? "Deactivate service" : "Activate service"}
              </button>
            ) : null}
          </div>
        </div>

        {service ? (
          <div className="mt-5 grid gap-4 md:grid-cols-3">
            <div className="rounded-2xl border border-slate-200 bg-slate-50 p-4">
              <p className="text-xs font-semibold uppercase tracking-[0.12em] text-slate-500">
                Owner
              </p>
              <p className="mt-2 text-sm text-slate-800">{service.owner || "-"}</p>
            </div>
            <div className="rounded-2xl border border-slate-200 bg-slate-50 p-4 md:col-span-2">
              <p className="text-xs font-semibold uppercase tracking-[0.12em] text-slate-500">
                Description
              </p>
              <p className="mt-2 text-sm text-slate-800">
                {service.description || "-"}
              </p>
            </div>
          </div>
        ) : !loading ? (
          <p className="mt-4 text-sm text-slate-500">Service not found.</p>
        ) : null}
      </section>

      <section className="panel p-4">
        <div className="flex flex-wrap gap-3">
          <button
            className={selectedTab === "usages" ? "btn-primary" : "btn-secondary"}
            onClick={() => setSelectedTab("usages")}
          >
            Usages
          </button>
          {isAdmin ? (
            <button
              className={selectedTab === "history" ? "btn-primary" : "btn-secondary"}
              onClick={async () => {
                setSelectedTab("history");
                await loadAudit();
              }}
            >
              History
            </button>
          ) : null}
        </div>
      </section>

      {selectedTab === "usages" ? (
        <div className="grid gap-6 xl:grid-cols-[1.45fr_1fr]">
          <section className="space-y-6">
            <div className="panel p-6">
              <div className="flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
                <h3 className="text-lg font-semibold text-slate-900">Usages</h3>
                <div className="grid gap-3 md:grid-cols-3">
                  <select
                    className="field"
                    value={usageActiveFilter}
                    onChange={(event) =>
                      setUsageActiveFilter(event.target.value as ActiveFilter)
                    }
                  >
                    <option value="all">All statuses</option>
                    <option value="active">Active only</option>
                    <option value="inactive">Inactive only</option>
                  </select>
                  <select
                    className="field"
                    value={usageRoleFilter}
                    onChange={(event) =>
                      setUsageRoleFilter(event.target.value as RoleFilter)
                    }
                  >
                    <option value="all">All roles</option>
                    <option value="PRODUCER">Producer</option>
                    <option value="CONSUMER">Consumer</option>
                  </select>
                  <input
                    className="field"
                    value={subjectFilter}
                    onChange={(event) => setSubjectFilter(event.target.value)}
                    placeholder="Filter by subject"
                  />
                </div>
              </div>

              <div className="mt-5 table-wrap">
                <table className="table-base">
                  <thead>
                    <tr>
                      <th>Subject</th>
                      <th>Version</th>
                      <th>Role</th>
                      <th>Active</th>
                      <th>Actions</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-slate-100">
                    {visibleUsages.map((usage) => (
                      <tr key={usage.id}>
                        <td className="font-medium text-slate-900">
                          {usage.subject}
                        </td>
                        <td>{usage.version ?? "-"}</td>
                        <td>
                          <RoleBadge role={usage.role} />
                        </td>
                        <td>
                          <ActiveBadge active={usage.active} />
                        </td>
                        <td>
                          <div className="flex flex-wrap gap-2">
                            {isAdmin ? (
                              <>
                                <button
                                  className="btn-secondary"
                                  onClick={() => startEditingUsage(usage)}
                                >
                                  Edit
                                </button>
                                <button
                                  className="btn-secondary"
                                  onClick={() => startMigrating(usage)}
                                >
                                  Migrate
                                </button>
                                <button
                                  className="btn-secondary"
                                  onClick={() => void deactivateUsage(usage.id)}
                                >
                                  Deactivate
                                </button>
                                <button
                                  className="btn-secondary"
                                  onClick={() => void loadUsageAudit(usage.id)}
                                >
                                  Audit
                                </button>
                              </>
                            ) : null}
                          </div>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>

              {!loading && visibleUsages.length === 0 ? (
                <p className="mt-4 text-sm text-slate-500">
                  Usage links not found for the current filters.
                </p>
              ) : null}
            </div>

            {isAdmin ? (
              <div className="panel p-6">
                <h3 className="text-lg font-semibold text-slate-900">Add usage</h3>
                <div className="mt-4 grid gap-4 md:grid-cols-3">
                  <input
                    className="field"
                    value={createUsageForm.subject}
                    onChange={(event) =>
                      setCreateUsageForm((current) => ({
                        ...current,
                        subject: event.target.value,
                      }))
                    }
                    placeholder="Subject"
                  />
                  <input
                    className="field"
                    type="number"
                    min="1"
                    value={createUsageForm.version}
                    onChange={(event) =>
                      setCreateUsageForm((current) => ({
                        ...current,
                        version: Number(event.target.value),
                      }))
                    }
                    placeholder="Version"
                  />
                  <select
                    className="field"
                    value={createUsageForm.role}
                    onChange={(event) =>
                      setCreateUsageForm((current) => ({
                        ...current,
                        role: event.target.value as "PRODUCER" | "CONSUMER",
                      }))
                    }
                  >
                    <option value="CONSUMER">CONSUMER</option>
                    <option value="PRODUCER">PRODUCER</option>
                  </select>
                </div>
                <button className="btn-primary mt-4" onClick={createUsage}>
                  Add usage
                </button>
              </div>
            ) : null}
          </section>

          <section className="space-y-6">
            {isAdmin && editingUsageId !== null ? (
              <div className="panel p-6">
                <div className="flex items-center justify-between">
                  <h3 className="text-lg font-semibold text-slate-900">
                    Edit usage
                  </h3>
                  <button
                    className="btn-secondary"
                    onClick={() => {
                      setEditingUsageId(null);
                      setEditUsageForm(emptyEditUsageForm);
                    }}
                  >
                    Close
                  </button>
                </div>
                <div className="mt-4 grid gap-4">
                  <input
                    className="field"
                    value={editUsageForm.subject ?? ""}
                    onChange={(event) =>
                      setEditUsageForm((current) => ({
                        ...current,
                        subject: event.target.value,
                      }))
                    }
                    placeholder="Subject"
                  />
                  <input
                    className="field"
                    type="number"
                    min="1"
                    value={editUsageForm.version ?? 1}
                    onChange={(event) =>
                      setEditUsageForm((current) => ({
                        ...current,
                        version: Number(event.target.value),
                      }))
                    }
                    placeholder="Version"
                  />
                  <select
                    className="field"
                    value={editUsageForm.role ?? "CONSUMER"}
                    onChange={(event) =>
                      setEditUsageForm((current) => ({
                        ...current,
                        role: event.target.value as "PRODUCER" | "CONSUMER",
                      }))
                    }
                  >
                    <option value="CONSUMER">CONSUMER</option>
                    <option value="PRODUCER">PRODUCER</option>
                  </select>
                  <label className="flex items-center gap-3 rounded-xl border border-slate-200 px-4 py-2.5 text-sm text-slate-700">
                    <input
                      type="checkbox"
                      checked={Boolean(editUsageForm.active)}
                      onChange={(event) =>
                        setEditUsageForm((current) => ({
                          ...current,
                          active: event.target.checked,
                        }))
                      }
                    />
                    Active
                  </label>
                  <button className="btn-primary" onClick={saveUsage}>
                    Save usage
                  </button>
                </div>
              </div>
            ) : null}

            {isAdmin && migratingUsageId !== null ? (
              <div className="panel p-6">
                <div className="flex items-center justify-between">
                  <h3 className="text-lg font-semibold text-slate-900">
                    Migrate usage
                  </h3>
                  <button
                    className="btn-secondary"
                    onClick={() => {
                      setMigratingUsageId(null);
                      setMigrateForm(emptyMigrateForm);
                    }}
                  >
                    Close
                  </button>
                </div>
                <div className="mt-4 grid gap-4">
                  <input
                    className="field"
                    type="number"
                    min="1"
                    value={migrateForm.targetVersion}
                    onChange={(event) =>
                      setMigrateForm({
                        targetVersion: Number(event.target.value),
                      })
                    }
                    placeholder="Target version"
                  />
                  <button className="btn-primary" onClick={migrateUsage}>
                    Migrate to version
                  </button>
                </div>
              </div>
            ) : null}
          </section>
        </div>
      ) : (
        <section className="panel p-6">
          <div className="flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
            <div>
              <h3 className="text-lg font-semibold text-slate-900">History</h3>
            </div>
            <div className="flex flex-wrap items-center gap-3">
              <input
                className="field w-[120px]"
                type="number"
                min="1"
                max="200"
                value={auditLimit}
                onChange={(event) => setAuditLimit(Number(event.target.value))}
                placeholder="Limit"
              />
              <button className="btn-secondary" onClick={() => void loadAudit()}>
                Refresh history
              </button>
            </div>
          </div>

          <div className="mt-5 space-y-4">
            {isAdmin ? <ServiceUsageAuditTable records={auditRecords} /> : (
              <p className="text-sm text-slate-600">
                История аудита доступна только администраторам.
              </p>
            )}

            {isAdmin && selectedUsageAuditId !== null ? (
              <div className="rounded-2xl border border-slate-200 bg-slate-50 p-4">
                <div className="flex items-center justify-between gap-3">
                  <h4 className="text-base font-semibold text-slate-900">
                    Usage audit #{selectedUsageAuditId}
                  </h4>
                  <button
                    className="btn-secondary"
                    onClick={() => {
                      setSelectedUsageAuditId(null);
                      setUsageAuditRecords([]);
                    }}
                  >
                    Clear
                  </button>
                </div>
                <div className="mt-4">
                  <ServiceUsageAuditTable records={usageAuditRecords} />
                </div>
              </div>
            ) : null}
          </div>
        </section>
      )}
    </div>
  );
}

function toErrorMessage(error: unknown) {
  if (error instanceof ApiError && error.payload) {
    if (error.payload.status === 403 || error.payload.errorCode === "ACCESS_DENIED") {
      return "Недостаточно прав для выполнения операции.";
    }
    return `${error.payload.errorCode}: ${error.payload.message}`;
  }
  return error instanceof Error ? error.message : "Request failed";
}
