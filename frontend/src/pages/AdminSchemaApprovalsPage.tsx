import type { ReactNode } from "react";
import { useEffect, useState } from "react";
import { Link, useSearchParams } from "react-router-dom";
import { ApiError } from "../api/client";
import { schemaApprovalsApi } from "../api/schemaApprovalsApi";
import { ErrorAlert } from "../components/common/ErrorAlert";
import { GovernanceDecisionBadge } from "../components/risk/GovernanceDecisionBadge";
import { RiskBadge } from "../components/risk/RiskBadge";
import type {
  SchemaApprovalResponse,
  SchemaApprovalStatus,
} from "../types/approval";
import type { GovernanceDecision, RiskLevel } from "../types/analysis";

type StatusFilter = "all" | SchemaApprovalStatus;

export function AdminSchemaApprovalsPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const [approvals, setApprovals] = useState<SchemaApprovalResponse[]>([]);
  const [selectedApproval, setSelectedApproval] =
    useState<SchemaApprovalResponse | null>(null);
  const [statusFilter, setStatusFilter] = useState<StatusFilter>("all");
  const [subjectFilter, setSubjectFilter] = useState("");
  const [error, setError] = useState<ApiError | null>(null);
  const [successMessage, setSuccessMessage] = useState("");
  const [loading, setLoading] = useState(false);
  const [actionLoading, setActionLoading] = useState(false);
  const [decisionComment, setDecisionComment] = useState("");

  useEffect(() => {
    void loadApprovals();
  }, [statusFilter]);

  useEffect(() => {
    const approvalId = searchParams.get("approvalId");
    if (!approvalId) {
      return;
    }

    const parsedId = Number(approvalId);
    if (Number.isNaN(parsedId)) {
      return;
    }

    void openApproval(parsedId);
  }, [searchParams]);

  const loadApprovals = async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await schemaApprovalsApi.getAdminApprovals({
        status: statusFilter === "all" ? undefined : statusFilter,
        subject: subjectFilter.trim() || undefined,
        limit: 100,
      });
      setApprovals(response);

      if (selectedApproval) {
        const updatedSelected =
          response.find((item) => item.id === selectedApproval.id) ?? null;
        setSelectedApproval(updatedSelected);
      }
    } catch (err) {
      setError(mapApprovalError(err));
    } finally {
      setLoading(false);
    }
  };

  const openApproval = async (approvalId: number) => {
    setError(null);
    try {
      const approval = await schemaApprovalsApi.getAdminApproval(approvalId);
      setSelectedApproval(approval);
      setDecisionComment(approval.adminComment ?? "");
      if (searchParams.get("approvalId") !== String(approvalId)) {
        setSearchParams((current) => {
          const next = new URLSearchParams(current);
          next.set("approvalId", String(approvalId));
          return next;
        });
      }
    } catch (err) {
      setError(mapApprovalError(err));
    }
  };

  const approveDirect = async (approvalId: number) => {
    setActionLoading(true);
    setError(null);
    setSuccessMessage("");
    try {
      const updated = await schemaApprovalsApi.approveApproval(approvalId, {});
      patchApproval(updated);
      await loadApprovals();
      setSelectedApproval(updated);
      setDecisionComment(updated.adminComment ?? "");
      setSuccessMessage("Schema published to Schema Registry.");
    } catch (err) {
      setError(mapApprovalError(err));
    } finally {
      setActionLoading(false);
    }
  };

  const rejectDirect = async (approvalId: number) => {
    setActionLoading(true);
    setError(null);
    setSuccessMessage("");
    try {
      const updated = await schemaApprovalsApi.rejectApproval(approvalId, {});
      patchApproval(updated);
      await loadApprovals();
      setSelectedApproval(updated);
      setDecisionComment(updated.adminComment ?? "");
      setSuccessMessage("Request rejected.");
    } catch (err) {
      setError(mapApprovalError(err));
    } finally {
      setActionLoading(false);
    }
  };

  const approve = async () => {
    if (!selectedApproval) {
      return;
    }

    setActionLoading(true);
    setError(null);
    setSuccessMessage("");
    try {
      const updated = await schemaApprovalsApi.approveApproval(
        selectedApproval.id,
        { comment: decisionComment.trim() || undefined },
      );
      patchApproval(updated);
      await loadApprovals();
      setSelectedApproval(updated);
      setSuccessMessage("Schema published to Schema Registry.");
    } catch (err) {
      setError(mapApprovalError(err));
    } finally {
      setActionLoading(false);
    }
  };

  const reject = async () => {
    if (!selectedApproval) {
      return;
    }

    setActionLoading(true);
    setError(null);
    setSuccessMessage("");
    try {
      const updated = await schemaApprovalsApi.rejectApproval(
        selectedApproval.id,
        { comment: decisionComment.trim() || undefined },
      );
      patchApproval(updated);
      await loadApprovals();
      setSelectedApproval(updated);
      setSuccessMessage("Request rejected.");
    } catch (err) {
      setError(mapApprovalError(err));
    } finally {
      setActionLoading(false);
    }
  };

  const patchApproval = (updated: SchemaApprovalResponse) => {
    setApprovals((current) =>
      current.map((approval) =>
        approval.id === updated.id ? updated : approval,
      ),
    );
  };

  return (
    <div className="space-y-6">
      <section className="panel p-6">
        <div className="flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
          <div>
            <h2 className="text-2xl font-semibold text-slate-900">
              Schema approvals
            </h2>
            <p className="mt-1 text-sm text-slate-500">
              Risky but formally compatible schema changes are waiting for an administrator decision.
            </p>
          </div>
          <button className="btn-secondary" onClick={() => void loadApprovals()}>
            Refresh
          </button>
        </div>
      </section>

      <ErrorAlert
        title="Unable to complete the approval action"
        error={error}
      />

      {successMessage ? (
        <div className="rounded-2xl border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm text-emerald-700">
          {successMessage}
        </div>
      ) : null}

      <section className="panel p-6">
        <div className="grid gap-3 md:grid-cols-[220px_1fr_auto]">
          <select
            className="field"
            value={statusFilter}
            onChange={(event) =>
              setStatusFilter(event.target.value as StatusFilter)
            }
          >
            <option value="all">All statuses</option>
            <option value="PENDING">PENDING</option>
            <option value="PUBLISHED">PUBLISHED</option>
            <option value="REJECTED">REJECTED</option>
            <option value="REGISTRY_REJECTED">REGISTRY_REJECTED</option>
            <option value="CANCELLED">CANCELLED</option>
          </select>
          <input
            className="field"
            value={subjectFilter}
            onChange={(event) => setSubjectFilter(event.target.value)}
            placeholder="Filter by subject"
          />
          <button className="btn-secondary" onClick={() => void loadApprovals()}>
            Apply
          </button>
        </div>

        <div className="mt-5 table-wrap">
          <table className="table-base">
            <thead>
              <tr>
                <th>ID</th>
                <th>Subject</th>
                <th>Old version</th>
                <th>Risk score</th>
                <th>Risk level</th>
                <th>Governance decision</th>
                <th>Status</th>
                <th>Requested by</th>
                <th>Requested at</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {approvals.map((approval) => (
                <tr key={approval.id}>
                  <td>{approval.id}</td>
                  <td className="font-medium text-slate-900">
                    {approval.subject}
                  </td>
                  <td>{approval.oldVersion ?? "-"}</td>
                  <td>{approval.riskScore}</td>
                  <td>
                    <RiskBadge value={approval.riskLevel as RiskLevel} />
                  </td>
                  <td>
                    <GovernanceDecisionBadge
                      value={approval.governanceDecision as GovernanceDecision}
                    />
                  </td>
                  <td>
                    <ApprovalStatusBadge value={approval.status} />
                  </td>
                  <td>{approval.requestedBy ?? "-"}</td>
                  <td>{formatDate(approval.requestedAt)}</td>
                  <td>
                    <div className="flex flex-wrap gap-2">
                      <button
                        className="btn-secondary"
                        onClick={() => void openApproval(approval.id)}
                      >
                        Open
                      </button>
                      {approval.status === "PENDING" ? (
                        <>
                          <button
                            className="btn-secondary"
                            disabled={actionLoading}
                            onClick={() => void approveDirect(approval.id)}
                          >
                            Approve
                          </button>
                          <button
                            className="btn-secondary"
                            disabled={actionLoading}
                            onClick={() => void rejectDirect(approval.id)}
                          >
                            Reject
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

        {!loading && approvals.length === 0 ? (
          <p className="mt-4 text-sm text-slate-500">
            No requests found for the current filters.
          </p>
        ) : null}
      </section>

      {selectedApproval ? (
        <section className="panel p-6">
          <div className="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
            <div>
              <h3 className="text-xl font-semibold text-slate-900">
                Request #{selectedApproval.id}
              </h3>
              <p className="mt-1 text-sm text-slate-500">
                Approval details and the schema text before publication.
              </p>
            </div>
            {selectedApproval.analysisId ? (
              <Link
                className="btn-secondary"
                to={`/history/${selectedApproval.analysisId}`}
              >
                Open analysis
              </Link>
            ) : null}
          </div>

          <div className="mt-6 grid gap-4 md:grid-cols-2 xl:grid-cols-4">
            <Summary label="Subject" value={selectedApproval.subject} />
            <Summary label="Schema type" value={selectedApproval.schemaType} />
            <Summary
              label="Compatibility"
              value={selectedApproval.compatibilityMode ?? "-"}
            />
            <Summary
              label="Old version"
              value={String(selectedApproval.oldVersion ?? "-")}
            />
            <Summary
              label="Risk score"
              value={String(selectedApproval.riskScore)}
            />
            <Summary
              label="Risk level"
              value={<RiskBadge value={selectedApproval.riskLevel as RiskLevel} />}
            />
            <Summary
              label="Governance"
              value={
                <GovernanceDecisionBadge
                  value={selectedApproval.governanceDecision as GovernanceDecision}
                />
              }
            />
            <Summary
              label="Status"
              value={<ApprovalStatusBadge value={selectedApproval.status} />}
            />
            <Summary
              label="Formal compatible"
              value={selectedApproval.formalCompatible ? "true" : "false"}
            />
            <Summary
              label="Requested by"
              value={selectedApproval.requestedBy ?? "-"}
            />
            <Summary
              label="Requested at"
              value={formatDate(selectedApproval.requestedAt)}
            />
            <Summary
              label="Reviewed by"
              value={selectedApproval.reviewedBy ?? "-"}
            />
            <Summary
              label="Reviewed at"
              value={formatDate(selectedApproval.reviewedAt)}
            />
            <Summary
              label="Registered version"
              value={String(selectedApproval.registeredVersion ?? "-")}
            />
            <Summary
              label="Schema Registry ID"
              value={String(selectedApproval.schemaRegistryId ?? "-")}
            />
          </div>

          <div className="mt-6">
            <label
              htmlFor="approval-comment"
              className="text-sm font-medium text-slate-700"
            >
              Administrator comment
            </label>
            <textarea
              id="approval-comment"
              className="field mt-2 min-h-[120px]"
              value={decisionComment}
              onChange={(event) => setDecisionComment(event.target.value)}
              placeholder="Comment for the decision"
            />
          </div>

          {selectedApproval.adminComment ? (
            <div className="mt-4 rounded-2xl bg-slate-50 px-4 py-3 text-sm text-slate-700">
              <span className="font-semibold text-slate-900">
                Saved comment:
              </span>{" "}
              {selectedApproval.adminComment}
            </div>
          ) : null}

          <div className="mt-6">
            <p className="text-sm font-medium text-slate-700">Schema text</p>
            <pre className="mt-2 overflow-x-auto rounded-2xl bg-slate-950 p-4 text-xs text-slate-100">
              {selectedApproval.newSchemaText}
            </pre>
          </div>

          {selectedApproval.status === "PENDING" ? (
            <div className="mt-6 flex flex-wrap gap-3">
              <button
                className="btn-primary"
                disabled={actionLoading}
                onClick={() => void approve()}
              >
                {actionLoading ? "Processing..." : "Approve"}
              </button>
              <button
                className="btn-secondary"
                disabled={actionLoading}
                onClick={() => void reject()}
              >
                Reject
              </button>
            </div>
          ) : null}
        </section>
      ) : null}
    </div>
  );
}

function ApprovalStatusBadge({ value }: { value: SchemaApprovalStatus }) {
  const styles: Record<SchemaApprovalStatus, string> = {
    PENDING: "bg-amber-100 text-amber-700",
    REJECTED: "bg-rose-100 text-rose-700",
    PUBLISHED: "bg-emerald-100 text-emerald-700",
    REGISTRY_REJECTED: "bg-orange-100 text-orange-700",
    CANCELLED: "bg-slate-100 text-slate-700",
  };

  return (
    <span
      className={`inline-flex rounded-full px-3 py-1 text-xs font-semibold ${styles[value]}`}
    >
      {value}
    </span>
  );
}

function Summary({
  label,
  value,
}: {
  label: string;
  value: ReactNode;
}) {
  return (
    <div className="rounded-2xl bg-white/80 p-4">
      <p className="text-xs uppercase tracking-[0.24em] text-slate-500">
        {label}
      </p>
      <div className="mt-2 text-sm font-semibold text-slate-900">{value}</div>
    </div>
  );
}

function formatDate(value?: string | null) {
  if (!value) {
    return "-";
  }
  return new Date(value).toLocaleString();
}

function mapApprovalError(error: unknown) {
  if (!(error instanceof ApiError)) {
    return new ApiError("Unable to complete the request.");
  }

  if (error.errorCode === "APPROVAL_BASELINE_CHANGED") {
    return new ApiError(
      "The latest schema version changed after the approval request was created. Run the analysis again.",
      error.payload,
    );
  }

  if (error.errorCode === "INVALID_APPROVAL_STATE") {
    return new ApiError(
      "This approval request has already been processed or is not available for this action.",
      error.payload,
    );
  }

  if (error.errorCode === "SCHEMA_REGISTRY_CONFLICT") {
    return new ApiError(
      "Schema Registry rejected the schema publication.",
      error.payload,
    );
  }

  if (error.status === 403) {
    return new ApiError(
      "You do not have permission to perform this action.",
      error.payload,
    );
  }

  return error;
}
