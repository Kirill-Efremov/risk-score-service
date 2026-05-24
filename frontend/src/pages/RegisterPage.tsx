import { useState } from "react";
import { Link, Navigate, useNavigate } from "react-router-dom";
import { ApiError } from "../api/client";
import { ErrorAlert } from "../components/common/ErrorAlert";
import { useAuth } from "../auth/AuthContext";

export function RegisterPage() {
  const navigate = useNavigate();
  const { register, isAuthenticated, loading } = useAuth();
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<ApiError | null>(null);
  const [validationMessage, setValidationMessage] = useState<string | null>(null);

  if (!loading && isAuthenticated) {
    return <Navigate to="/promotion" replace />;
  }

  const submit = async () => {
    const normalizedUsername = username.trim();

    if (!normalizedUsername) {
      setValidationMessage("Укажите имя пользователя.");
      setError(null);
      return;
    }

    if (password.length < 6) {
      setValidationMessage("Пароль должен содержать не менее 6 символов.");
      setError(null);
      return;
    }

    if (password !== confirmPassword) {
      setValidationMessage("Пароли не совпадают.");
      setError(null);
      return;
    }

    setValidationMessage(null);
    setSubmitting(true);
    setError(null);

    try {
      await register(normalizedUsername, password);
      navigate("/promotion", { replace: true });
    } catch (err) {
      if (err instanceof ApiError) {
        setError(err);
      } else {
        setError(new ApiError("Не удалось выполнить регистрацию"));
      }
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="panel mx-auto w-full max-w-md p-6 md:p-8">
      <p className="text-sm uppercase tracking-[0.24em] text-slate-500">
        Регистрация
      </p>
      <h1 className="mt-3 text-3xl font-semibold text-slate-900">
        Создание пользователя
      </h1>
      <p className="mt-2 text-sm text-slate-600">
        Первый зарегистрированный пользователь получает роль администратора.
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
          autoComplete="new-password"
        />
        <input
          className="field"
          type="password"
          value={confirmPassword}
          onChange={(event) => setConfirmPassword(event.target.value)}
          placeholder="Подтверждение пароля"
          autoComplete="new-password"
        />
      </div>

      <div className="mt-5 space-y-3">
        <ErrorAlert title="Не удалось выполнить регистрацию" error={error} />
        <ErrorAlert message={validationMessage} />
      </div>

      <div className="mt-6 flex flex-col gap-3">
        <button
          className="btn-primary"
          onClick={() => void submit()}
          disabled={submitting}
        >
          {submitting ? "Регистрация..." : "Зарегистрироваться"}
        </button>
        <Link className="btn-secondary" to="/login">
          Перейти ко входу
        </Link>
      </div>
    </div>
  );
}
