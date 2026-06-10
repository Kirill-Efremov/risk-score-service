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
      setValidationMessage("Enter a username.");
      setError(null);
      return;
    }

    if (password.length < 6) {
      setValidationMessage("Password must contain at least 6 characters.");
      setError(null);
      return;
    }

    if (password !== confirmPassword) {
      setValidationMessage("Passwords do not match.");
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
        setError(new ApiError("Unable to register."));
      }
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="panel mx-auto w-full max-w-md p-6 md:p-8">
      <p className="text-sm uppercase tracking-[0.24em] text-slate-500">
        Create account
      </p>
      <h1 className="mt-3 text-3xl font-semibold text-slate-900">Register</h1>
      <p className="mt-2 text-sm text-slate-600">
        The first registered user receives the administrator role.
      </p>

      <div className="mt-6 grid gap-4">
        <input
          className="field"
          value={username}
          onChange={(event) => setUsername(event.target.value)}
          placeholder="Username"
          autoComplete="username"
        />
        <input
          className="field"
          type="password"
          value={password}
          onChange={(event) => setPassword(event.target.value)}
          placeholder="Password"
          autoComplete="new-password"
        />
        <input
          className="field"
          type="password"
          value={confirmPassword}
          onChange={(event) => setConfirmPassword(event.target.value)}
          placeholder="Confirm password"
          autoComplete="new-password"
        />
      </div>

      <div className="mt-5 space-y-3">
        <ErrorAlert title="Unable to register" error={error} />
        <ErrorAlert message={validationMessage} />
      </div>

      <div className="mt-6 flex flex-col gap-3">
        <button
          className="btn-primary"
          onClick={() => void submit()}
          disabled={submitting}
        >
          {submitting ? "Registering..." : "Register"}
        </button>
        <Link className="btn-secondary" to="/login">
          Go to login
        </Link>
      </div>
    </div>
  );
}
