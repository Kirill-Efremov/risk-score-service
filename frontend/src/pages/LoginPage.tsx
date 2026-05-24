import { useState } from "react";
import { Link, Navigate, useLocation, useNavigate } from "react-router-dom";
import { ApiError } from "../api/client";
import { ErrorAlert } from "../components/common/ErrorAlert";
import { useAuth } from "../auth/AuthContext";

interface RedirectState {
  from?: string;
}

export function LoginPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const { login, isAuthenticated, loading } = useAuth();
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<ApiError | null>(null);
  const redirectState = location.state as RedirectState | null;

  if (!loading && isAuthenticated) {
    const target =
      typeof redirectState?.from === "string" ? redirectState.from : "/promotion";
    return <Navigate to={target} replace />;
  }

  const submit = async () => {
    setSubmitting(true);
    setError(null);
    try {
      await login(username.trim(), password);
      const target =
        typeof redirectState?.from === "string"
          ? redirectState.from
          : "/promotion";
      navigate(target, { replace: true });
    } catch (err) {
      if (err instanceof ApiError) {
        setError(err);
      } else {
        setError(new ApiError("Не удалось выполнить вход"));
      }
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="panel mx-auto w-full max-w-md p-6 md:p-8">
      <p className="text-sm uppercase tracking-[0.24em] text-slate-500">
        Вход в систему
      </p>
      <h1 className="mt-3 text-3xl font-semibold text-slate-900">
        Авторизация
      </h1>
      <p className="mt-2 text-sm text-slate-600">
        Используйте учетную запись backend-аутентификации.
      </p>

      <div className="mt-6 grid gap-4">
        <input
          className="field"
          value={username}
          onChange={(event) => setUsername(event.target.value)}
          placeholder="Имя пользователя"
          autoComplete="username"
        />
        <input
          className="field"
          type="password"
          value={password}
          onChange={(event) => setPassword(event.target.value)}
          placeholder="Пароль"
          autoComplete="current-password"
        />
      </div>

      <div className="mt-5">
        <ErrorAlert
          title="Не удалось выполнить вход"
          error={error}
          message={
            error?.errorCode === "INVALID_CREDENTIALS"
              ? "Проверьте имя пользователя и пароль"
              : undefined
          }
        />
      </div>

      <div className="mt-6 flex flex-col gap-3">
        <button
          className="btn-primary"
          onClick={() => void submit()}
          disabled={submitting}
        >
          {submitting ? "Выполняется вход..." : "Войти"}
        </button>
        <Link className="btn-secondary" to="/register">
          Перейти к регистрации
        </Link>
      </div>
    </div>
  );
}
