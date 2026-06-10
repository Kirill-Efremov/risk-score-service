import {
  createContext,
  useContext,
  useEffect,
  useMemo,
  useState,
  type PropsWithChildren,
} from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { authApi } from "../api/authApi";
import {
  ACCESS_TOKEN_STORAGE_KEY,
  AUTH_UNAUTHORIZED_EVENT,
  ApiError,
  clearStoredAccessToken,
  getStoredAccessToken,
  setStoredAccessToken,
} from "../api/client";
import type { CurrentUserResponse } from "../types/auth";

interface AuthContextValue {
  user: CurrentUserResponse | null;
  accessToken: string | null;
  loading: boolean;
  isAuthenticated: boolean;
  isAdmin: boolean;
  login: (username: string, password: string) => Promise<void>;
  register: (username: string, password: string) => Promise<void>;
  logout: () => void;
  refreshCurrentUser: () => Promise<void>;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

function isAuthRoute(pathname: string) {
  return pathname === "/login" || pathname === "/register";
}

export function AuthProvider({ children }: PropsWithChildren) {
  const navigate = useNavigate();
  const location = useLocation();
  const [user, setUser] = useState<CurrentUserResponse | null>(null);
  const [accessToken, setAccessToken] = useState<string | null>(() =>
    getStoredAccessToken(),
  );
  const [loading, setLoading] = useState(true);

  const handleUnauthorized = () => {
    clearStoredAccessToken();
    setAccessToken(null);
    setUser(null);

    if (!isAuthRoute(window.location.pathname)) {
      navigate("/login", {
        replace: true,
        state: { from: window.location.pathname + window.location.search },
      });
    }
  };

  const refreshCurrentUser = async () => {
    const token = getStoredAccessToken();
    if (!token) {
      setAccessToken(null);
      setUser(null);
      return;
    }

    setAccessToken(token);
    const currentUser = await authApi.me();
    setUser((existingUser) => ({
      ...currentUser,
      createdAt: existingUser?.createdAt ?? null,
      updatedAt: existingUser?.updatedAt ?? null,
    }));
  };

  useEffect(() => {
    const restoreSession = async () => {
      const token = getStoredAccessToken();
      if (!token) {
        setLoading(false);
        return;
      }

      try {
        await refreshCurrentUser();
      } catch (error) {
        if (
          error instanceof ApiError &&
          (error.status === 401 || error.status === 403)
        ) {
          clearStoredAccessToken();
          setAccessToken(null);
          setUser(null);
        }
      } finally {
        setLoading(false);
      }
    };

    void restoreSession();
  }, []);

  useEffect(() => {
    const onUnauthorized = () => {
      handleUnauthorized();
    };

    window.addEventListener(AUTH_UNAUTHORIZED_EVENT, onUnauthorized);
    return () => {
      window.removeEventListener(AUTH_UNAUTHORIZED_EVENT, onUnauthorized);
    };
  }, [navigate]);

  useEffect(() => {
    const token = window.localStorage.getItem(ACCESS_TOKEN_STORAGE_KEY);
    setAccessToken(token);
  }, [location.pathname]);

  const value = useMemo<AuthContextValue>(
    () => ({
      user,
      accessToken,
      loading,
      isAuthenticated: Boolean(user && accessToken),
      isAdmin: user?.role === "ADMIN",
      login: async (username: string, password: string) => {
        const response = await authApi.login({ username, password });
        setStoredAccessToken(response.accessToken);
        setAccessToken(response.accessToken);
        setUser({
          id: response.user.id,
          username: response.user.username,
          role: response.user.role,
          active: response.user.active,
          createdAt: response.user.createdAt ?? null,
          updatedAt: response.user.updatedAt ?? null,
        });
      },
      register: async (username: string, password: string) => {
        await authApi.register({ username, password });
        const response = await authApi.login({ username, password });
        setStoredAccessToken(response.accessToken);
        setAccessToken(response.accessToken);
        setUser({
          id: response.user.id,
          username: response.user.username,
          role: response.user.role,
          active: response.user.active,
          createdAt: response.user.createdAt ?? null,
          updatedAt: response.user.updatedAt ?? null,
        });
      },
      logout: () => {
        clearStoredAccessToken();
        setAccessToken(null);
        setUser(null);
        navigate("/login", { replace: true });
      },
      refreshCurrentUser: async () => {
        await refreshCurrentUser();
      },
    }),
    [accessToken, loading, navigate, user],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used within AuthProvider");
  }
  return context;
}
