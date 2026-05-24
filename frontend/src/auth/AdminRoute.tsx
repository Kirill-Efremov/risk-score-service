import type { PropsWithChildren } from "react";
import { Navigate, useLocation } from "react-router-dom";
import { LoadingState } from "../components/common/LoadingState";
import { useAuth } from "./AuthContext";

export function AdminRoute({ children }: PropsWithChildren) {
  const { loading, isAuthenticated, isAdmin } = useAuth();
  const location = useLocation();

  if (loading) {
    return <LoadingState label="Загрузка профиля..." />;
  }

  if (!isAuthenticated) {
    return (
      <Navigate
        to="/login"
        replace
        state={{ from: location.pathname + location.search }}
      />
    );
  }

  if (!isAdmin) {
    return (
      <div className="panel p-6">
        <h2 className="text-xl font-semibold text-slate-900">Нет доступа</h2>
        <p className="mt-2 text-sm text-slate-600">
          Для открытия этого раздела требуется роль администратора.
        </p>
      </div>
    );
  }

  return <>{children}</>;
}
