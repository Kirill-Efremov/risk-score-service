import { Link, useLocation } from "react-router-dom";
import { useAuth } from "../../auth/AuthContext";

const titles: Record<string, string> = {
  "/promotion": "Controlled Promotion",
  "/raw-analysis": "Raw Analysis",
  "/versioned-analysis": "Versioned Analysis",
  "/usage-map": "Usage Map",
  "/history": "Analysis History",
  "/impact-graph": "Impact Graph",
  "/settings": "Settings",
  "/login": "Вход",
  "/register": "Регистрация",
};

export function Header() {
  const location = useLocation();
  const { user, isAuthenticated, logout } = useAuth();
  const title = titles[location.pathname] || "Risk Score Service";

  return (
    <header className="panel px-6 py-5">
      <div className="flex flex-col gap-3 md:flex-row md:items-center md:justify-between">
        <div>
          <p className="text-sm uppercase tracking-[0.24em] text-slate-500">Operator console</p>
          <h2 className="mt-2 text-3xl font-semibold text-slate-900">{title}</h2>
        </div>
        <div className="flex flex-wrap items-center gap-3">
          {isAuthenticated && user ? (
            <>
              <div className="rounded-2xl border border-slate-200 bg-slate-50 px-4 py-2 text-sm text-slate-700">
                <span className="font-medium text-slate-900">{user.username}</span>
              </div>
              <span
                className={`inline-flex rounded-full px-3 py-1 text-xs font-semibold ${
                  user.role === "ADMIN"
                    ? "bg-violet-100 text-violet-700"
                    : "bg-sky-100 text-sky-700"
                }`}
              >
                {user.role}
              </span>
              <button className="btn-secondary" onClick={logout}>
                Logout
              </button>
            </>
          ) : (
            <>
              <Link className="btn-secondary" to="/login">
                Login
              </Link>
              <Link className="btn-primary" to="/register">
                Register
              </Link>
            </>
          )}
        </div>
      </div>
    </header>
  );
}
