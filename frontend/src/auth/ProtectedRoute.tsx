import type { PropsWithChildren } from "react";
import { Navigate, useLocation } from "react-router-dom";
import { LoadingState } from "../components/common/LoadingState";
import { useAuth } from "./AuthContext";

export function ProtectedRoute({ children }: PropsWithChildren) {
  const { loading, isAuthenticated } = useAuth();
  const location = useLocation();

  if (loading) {
    return <LoadingState label="Loading profile..." />;
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

  return <>{children}</>;
}
