import { useEffect, useState } from "react";
import { useAuth } from "../auth/AuthContext";
import { adminUsersApi } from "../api/adminUsersApi";
import { ApiError } from "../api/client";
import { ActiveBadge } from "../components/common/ActiveBadge";
import { ErrorAlert } from "../components/common/ErrorAlert";
import type { UserResponse, UserRole } from "../types/auth";

type RoleFilter = "all" | UserRole;
type ActiveFilter = "all" | "active" | "inactive";

export function AdminUsersPage() {
  const { user: currentUser } = useAuth();
  const [users, setUsers] = useState<UserResponse[]>([]);
  const [roleFilter, setRoleFilter] = useState<RoleFilter>("all");
  const [activeFilter, setActiveFilter] = useState<ActiveFilter>("all");
  const [error, setError] = useState<ApiError | null>(null);
  const [loading, setLoading] = useState(false);
  const [savingUserId, setSavingUserId] = useState<number | null>(null);

  useEffect(() => {
    void loadUsers();
  }, [roleFilter, activeFilter]);

  const loadUsers = async () => {
    setLoading(true);
    setError(null);
    try {
      setUsers(
        await adminUsersApi.getUsers({
          role: roleFilter === "all" ? undefined : roleFilter,
          active:
            activeFilter === "all"
              ? undefined
              : activeFilter === "active",
        }),
      );
    } catch (err) {
      setError(toApiError(err));
    } finally {
      setLoading(false);
    }
  };

  const updateRole = async (userId: number, role: UserRole) => {
    setSavingUserId(userId);
    setError(null);
    try {
      patchUser(await adminUsersApi.updateUser(userId, { role }));
      await loadUsers();
    } catch (err) {
      setError(toApiError(err));
    } finally {
      setSavingUserId(null);
    }
  };

  const updateActive = async (userId: number, active: boolean) => {
    setSavingUserId(userId);
    setError(null);
    try {
      patchUser(await adminUsersApi.updateUser(userId, { active }));
      await loadUsers();
    } catch (err) {
      setError(toApiError(err));
    } finally {
      setSavingUserId(null);
    }
  };

  const deactivateUser = async (userId: number) => {
    setSavingUserId(userId);
    setError(null);
    try {
      patchUser(await adminUsersApi.deactivateUser(userId));
      await loadUsers();
    } catch (err) {
      setError(toApiError(err));
    } finally {
      setSavingUserId(null);
    }
  };

  const patchUser = (updated: UserResponse) => {
    setUsers((current) =>
      current.map((user) => (user.id === updated.id ? updated : user)),
    );
  };

  return (
    <div className="space-y-6">
      <section className="panel p-6">
        <div className="flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
          <div>
            <h2 className="text-2xl font-semibold text-slate-900">
              Пользователи
            </h2>
            <p className="mt-1 text-sm text-slate-500">
              Управление ролями и активностью пользователей.
            </p>
          </div>
          <button className="btn-secondary" onClick={() => void loadUsers()}>
            Обновить
          </button>
        </div>
      </section>

      <ErrorAlert title="Не удалось выполнить операцию" error={error} />

      <section className="panel p-6">
        <div className="grid gap-3 md:grid-cols-2">
          <select
            className="field"
            value={roleFilter}
            onChange={(event) => setRoleFilter(event.target.value as RoleFilter)}
          >
            <option value="all">Все роли</option>
            <option value="ADMIN">ADMIN</option>
            <option value="USER">USER</option>
          </select>
          <select
            className="field"
            value={activeFilter}
            onChange={(event) =>
              setActiveFilter(event.target.value as ActiveFilter)
            }
          >
            <option value="all">Все статусы</option>
            <option value="active">Только активные</option>
            <option value="inactive">Только неактивные</option>
          </select>
        </div>

        <div className="mt-5 table-wrap">
          <table className="table-base">
            <thead>
              <tr>
                <th>ID</th>
                <th>Username</th>
                <th>Role</th>
                <th>Active</th>
                <th>Created at</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {users.map((user) => {
                const isSelf = currentUser?.id === user.id;
                const saving = savingUserId === user.id;

                return (
                  <tr key={user.id}>
                    <td>{user.id}</td>
                    <td className="font-medium text-slate-900">
                      {user.username}
                      {isSelf ? (
                        <span className="ml-2 text-xs text-slate-500">(вы)</span>
                      ) : null}
                    </td>
                    <td>
                      <RolePill role={user.role} />
                    </td>
                    <td>
                      <ActiveBadge active={user.active} />
                    </td>
                    <td>{formatDate(user.createdAt)}</td>
                    <td>
                      <div className="flex flex-wrap gap-2">
                        {user.role === "USER" ? (
                          <button
                            className="btn-secondary"
                            disabled={saving}
                            onClick={() => void updateRole(user.id, "ADMIN")}
                          >
                            Сделать ADMIN
                          </button>
                        ) : (
                          <button
                            className="btn-secondary"
                            disabled={saving || isSelf}
                            onClick={() => void updateRole(user.id, "USER")}
                          >
                            Сделать USER
                          </button>
                        )}

                        {user.active ? (
                          <button
                            className="btn-secondary"
                            disabled={saving || isSelf}
                            onClick={() => void deactivateUser(user.id)}
                          >
                            Деактивировать
                          </button>
                        ) : (
                          <button
                            className="btn-secondary"
                            disabled={saving}
                            onClick={() => void updateActive(user.id, true)}
                          >
                            Активировать
                          </button>
                        )}
                      </div>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>

        {!loading && users.length === 0 ? (
          <p className="mt-4 text-sm text-slate-500">
            Пользователи по текущим фильтрам не найдены.
          </p>
        ) : null}
      </section>
    </div>
  );
}

function RolePill({ role }: { role: UserRole }) {
  return (
    <span
      className={`inline-flex rounded-full px-3 py-1 text-xs font-semibold ${
        role === "ADMIN"
          ? "bg-violet-100 text-violet-700"
          : "bg-sky-100 text-sky-700"
      }`}
    >
      {role}
    </span>
  );
}

function formatDate(value?: string | null) {
  if (!value) {
    return "-";
  }
  return new Date(value).toLocaleString();
}

function toApiError(error: unknown) {
  if (error instanceof ApiError) {
    return error;
  }
  return new ApiError("Не удалось выполнить запрос");
}
