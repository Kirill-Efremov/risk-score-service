import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { ApiError } from "../api/client";
import { servicesApi } from "../api/servicesApi";
import { ActiveBadge } from "../components/common/ActiveBadge";
import { CriticalBadge } from "../components/common/CriticalBadge";
import { ErrorAlert } from "../components/common/ErrorAlert";
import type {
  CreateServiceRequest,
  ServiceResponse,
  UpdateServiceRequest,
} from "../types/usage";

type ActiveFilter = "all" | "active" | "inactive";
type CriticalFilter = "all" | "critical" | "non-critical";

const emptyCreateForm: CreateServiceRequest = {
  name: "",
  critical: false,
  owner: "",
  description: "",
};

const emptyEditForm: UpdateServiceRequest = {
  name: "",
  critical: false,
  active: true,
  owner: "",
  description: "",
};

export function ServicesPage() {
  const navigate = useNavigate();
  const [services, setServices] = useState<ServiceResponse[]>([]);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const [activeFilter, setActiveFilter] = useState<ActiveFilter>("all");
  const [criticalFilter, setCriticalFilter] =
    useState<CriticalFilter>("all");
  const [createForm, setCreateForm] = useState<CreateServiceRequest>(
    emptyCreateForm,
  );
  const [editingServiceId, setEditingServiceId] = useState<number | null>(null);
  const [editForm, setEditForm] = useState<UpdateServiceRequest>(emptyEditForm);

  useEffect(() => {
    void loadServices();
  }, [activeFilter, criticalFilter]);

  const loadServices = async () => {
    setLoading(true);
    setError("");
    try {
      setServices(
        await servicesApi.getServices({
          active:
            activeFilter === "all" ? undefined : activeFilter === "active",
          critical:
            criticalFilter === "all"
              ? undefined
              : criticalFilter === "critical",
        }),
      );
    } catch (err) {
      setError(toErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  const createService = async () => {
    setError("");
    try {
      const created = await servicesApi.createService({
        name: createForm.name.trim(),
        critical: createForm.critical,
        owner: createForm.owner?.trim() || undefined,
        description: createForm.description?.trim() || undefined,
      });
      setCreateForm(emptyCreateForm);
      await loadServices();
      navigate(`/services/${created.id}`);
    } catch (err) {
      setError(toErrorMessage(err));
    }
  };

  const startEditing = (service: ServiceResponse) => {
    setEditingServiceId(service.id);
    setEditForm({
      name: service.name,
      critical: service.critical,
      active: service.active,
      owner: service.owner ?? "",
      description: service.description ?? "",
    });
  };

  const saveService = async () => {
    if (editingServiceId === null) {
      return;
    }

    setError("");
    try {
      await servicesApi.updateService(editingServiceId, {
        name: editForm.name?.trim(),
        critical: editForm.critical,
        active: editForm.active,
        owner: editForm.owner?.trim() || "",
        description: editForm.description?.trim() || "",
      });
      setEditingServiceId(null);
      setEditForm(emptyEditForm);
      await loadServices();
    } catch (err) {
      setError(toErrorMessage(err));
    }
  };

  const deactivateService = async (serviceId: number) => {
    const confirmed = window.confirm(
      "Deactivate this service and all of its active usage links?",
    );
    if (!confirmed) {
      return;
    }

    setError("");
    try {
      await servicesApi.deactivateService(serviceId);
      if (editingServiceId === serviceId) {
        setEditingServiceId(null);
        setEditForm(emptyEditForm);
      }
      await loadServices();
    } catch (err) {
      setError(toErrorMessage(err));
    }
  };

  const activateService = async (service: ServiceResponse) => {
    setError("");
    try {
      await servicesApi.updateService(service.id, {
        active: true,
      });
      await loadServices();
    } catch (err) {
      setError(toErrorMessage(err));
    }
  };

  return (
    <div className="space-y-6">
      <ErrorAlert message={error} />

      <section className="panel p-6">
        <div className="flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
          <div>
            <h2 className="text-2xl font-semibold text-slate-900">Services</h2>
            <p className="mt-1 text-sm text-slate-500">
              Create services, keep ownership up to date, and open a service
              card to manage schema usage.
            </p>
          </div>
          <button className="btn-secondary" onClick={() => void loadServices()}>
            Refresh
          </button>
        </div>
      </section>

      <div className="grid gap-6 xl:grid-cols-[1.45fr_1fr]">
        <section className="panel p-6">
          <div className="space-y-5">
            <div className="grid gap-3 md:grid-cols-2">
              <select
                className="field"
                value={activeFilter}
                onChange={(event) =>
                  setActiveFilter(event.target.value as ActiveFilter)
                }
              >
                <option value="all">All statuses</option>
                <option value="active">Active only</option>
                <option value="inactive">Inactive only</option>
              </select>
              <select
                className="field"
                value={criticalFilter}
                onChange={(event) =>
                  setCriticalFilter(event.target.value as CriticalFilter)
                }
              >
                <option value="all">All criticalities</option>
                <option value="critical">Critical only</option>
                <option value="non-critical">Non-critical only</option>
              </select>
            </div>

            <div className="table-wrap">
              <table className="table-base">
                <thead>
                  <tr>
                    <th>Name</th>
                    <th>Critical</th>
                    <th>Active</th>
                    <th>Owner</th>
                    <th>Description</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {services.map((service) => (
                    <tr key={service.id}>
                      <td className="font-medium text-slate-900">
                        {service.name}
                      </td>
                      <td>
                        <CriticalBadge critical={service.critical} />
                      </td>
                      <td>
                        <ActiveBadge active={service.active} />
                      </td>
                      <td>{service.owner || "-"}</td>
                      <td className="max-w-[280px] whitespace-normal">
                        {service.description || "-"}
                      </td>
                      <td>
                        <div className="flex flex-wrap gap-2">
                          <button
                            className="btn-secondary"
                            onClick={() => navigate(`/services/${service.id}`)}
                          >
                            Open
                          </button>
                          <button
                            className="btn-secondary"
                            onClick={() => startEditing(service)}
                          >
                            Edit
                          </button>
                          <button
                            className="btn-secondary"
                            onClick={() =>
                              service.active
                                ? void deactivateService(service.id)
                                : void activateService(service)
                            }
                          >
                            {service.active ? "Deactivate" : "Activate"}
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            {!loading && services.length === 0 ? (
              <p className="text-sm text-slate-500">Services not found.</p>
            ) : null}
          </div>
        </section>

        <section className="space-y-6">
          <div className="panel p-6">
            <h3 className="text-lg font-semibold text-slate-900">
              Create service
            </h3>
            <div className="mt-4 grid gap-4">
              <input
                className="field"
                value={createForm.name}
                onChange={(event) =>
                  setCreateForm((current) => ({
                    ...current,
                    name: event.target.value,
                  }))
                }
                placeholder="Service name"
              />
              <input
                className="field"
                value={createForm.owner ?? ""}
                onChange={(event) =>
                  setCreateForm((current) => ({
                    ...current,
                    owner: event.target.value,
                  }))
                }
                placeholder="Owner"
              />
              <textarea
                className="field min-h-[110px]"
                value={createForm.description ?? ""}
                onChange={(event) =>
                  setCreateForm((current) => ({
                    ...current,
                    description: event.target.value,
                  }))
                }
                placeholder="Description"
              />
              <label className="flex items-center gap-3 rounded-xl border border-slate-200 px-4 py-2.5 text-sm text-slate-700">
                <input
                  type="checkbox"
                  checked={createForm.critical}
                  onChange={(event) =>
                    setCreateForm((current) => ({
                      ...current,
                      critical: event.target.checked,
                    }))
                  }
                />
                Critical service
              </label>
              <button className="btn-primary" onClick={createService}>
                Create service
              </button>
            </div>
          </div>

          <div className="panel p-6">
            <div className="flex items-center justify-between">
              <h3 className="text-lg font-semibold text-slate-900">
                Edit service
              </h3>
              {editingServiceId !== null ? (
                <button
                  className="btn-secondary"
                  onClick={() => {
                    setEditingServiceId(null);
                    setEditForm(emptyEditForm);
                  }}
                >
                  Clear
                </button>
              ) : null}
            </div>

            {editingServiceId === null ? (
              <p className="mt-4 text-sm text-slate-500">
                Select a service from the table to update it here.
              </p>
            ) : (
              <div className="mt-4 grid gap-4">
                <input
                  className="field"
                  value={editForm.name ?? ""}
                  onChange={(event) =>
                    setEditForm((current) => ({
                      ...current,
                      name: event.target.value,
                    }))
                  }
                  placeholder="Service name"
                />
                <input
                  className="field"
                  value={editForm.owner ?? ""}
                  onChange={(event) =>
                    setEditForm((current) => ({
                      ...current,
                      owner: event.target.value,
                    }))
                  }
                  placeholder="Owner"
                />
                <textarea
                  className="field min-h-[110px]"
                  value={editForm.description ?? ""}
                  onChange={(event) =>
                    setEditForm((current) => ({
                      ...current,
                      description: event.target.value,
                    }))
                  }
                  placeholder="Description"
                />
                <label className="flex items-center gap-3 rounded-xl border border-slate-200 px-4 py-2.5 text-sm text-slate-700">
                  <input
                    type="checkbox"
                    checked={Boolean(editForm.critical)}
                    onChange={(event) =>
                      setEditForm((current) => ({
                        ...current,
                        critical: event.target.checked,
                      }))
                    }
                  />
                  Critical service
                </label>
                <label className="flex items-center gap-3 rounded-xl border border-slate-200 px-4 py-2.5 text-sm text-slate-700">
                  <input
                    type="checkbox"
                    checked={Boolean(editForm.active)}
                    onChange={(event) =>
                      setEditForm((current) => ({
                        ...current,
                        active: event.target.checked,
                      }))
                    }
                  />
                  Active
                </label>
                <button className="btn-primary" onClick={saveService}>
                  Save changes
                </button>
              </div>
            )}
          </div>
        </section>
      </div>
    </div>
  );
}

function toErrorMessage(error: unknown) {
  if (error instanceof ApiError && error.payload) {
    return `${error.payload.errorCode}: ${error.payload.message}`;
  }
  return error instanceof Error ? error.message : "Request failed";
}
