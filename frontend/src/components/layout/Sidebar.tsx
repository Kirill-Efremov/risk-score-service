import {
  Activity,
  Clock3,
  GitCompareArrows,
  Send,
  Settings,
  ShieldAlert,
  ShieldCheck,
  UserRound,
  Users,
  Waypoints,
} from "lucide-react";
import { NavLink } from "react-router-dom";
import { useAuth } from "../../auth/AuthContext";

const links = [
  { to: "/versioned-analysis", label: "Versioned Analysis", icon: ShieldCheck },
  { to: "/promotion", label: "Promotion", icon: Send },
  { to: "/raw-analysis", label: "Raw Analysis", icon: GitCompareArrows },
  { to: "/services", label: "Services", icon: Activity },
  { to: "/history", label: "History", icon: Clock3 },
  { to: "/impact-graph", label: "Impact Graph", icon: Waypoints },
  { to: "/settings", label: "Settings", icon: Settings },
  { to: "/profile", label: "Profile", icon: UserRound },
];

export function Sidebar() {
  const { isAuthenticated, isAdmin } = useAuth();

  if (!isAuthenticated) {
    return null;
  }

  return (
    <aside className="flex w-full shrink-0 self-start rounded-[28px] border border-white/70 bg-white/80 p-5 shadow-panel backdrop-blur lg:sticky lg:top-6 lg:w-[260px]">
      <div className="w-full">
        <div className="px-2 py-2">
          <h1 className="text-[28px] font-semibold leading-[1.35] text-slate-900">
            Risk
            <br />
            Score
          </h1>
        </div>

        <nav className="mt-4 flex flex-col gap-3">
          {links.map(({ to, label, icon: Icon }) => (
            <NavLink
              key={to}
              to={to}
              className={({ isActive }) =>
                `flex items-center gap-4 rounded-2xl px-4 py-3 text-[15px] font-medium transition ${
                  isActive
                    ? "bg-slate-900 text-white shadow-lg"
                    : "text-slate-600 hover:bg-slate-100 hover:text-slate-900"
                }`
              }
            >
              <Icon size={20} className="shrink-0" />
              <span className="leading-6">
                {label === "Raw Analysis" ? (
                  <>
                    Raw
                    <br />
                    Analysis
                  </>
                ) : label === "Versioned Analysis" ? (
                  <>
                    Versioned
                    <br />
                    Analysis
                  </>
                ) : label === "Impact Graph" ? (
                  <>
                    Impact
                    <br />
                    Graph
                  </>
                ) : (
                  label
                )}
              </span>
            </NavLink>
          ))}
        </nav>

        {isAdmin ? (
          <div className="mt-6">
            <p className="px-2 text-xs font-semibold uppercase tracking-[0.18em] text-violet-700">
              Administration
            </p>
            <div className="mt-3 flex flex-col gap-3">
              <NavLink
                to="/admin/users"
                className={({ isActive }) =>
                  `flex items-center gap-4 rounded-2xl px-4 py-3 text-[15px] font-medium transition ${
                    isActive
                      ? "bg-violet-700 text-white shadow-lg"
                      : "text-violet-700 hover:bg-violet-50"
                  }`
                }
              >
                <Users size={20} className="shrink-0" />
                <span>Users</span>
              </NavLink>
              <NavLink
                to="/admin/schema-approvals"
                className={({ isActive }) =>
                  `flex items-center gap-4 rounded-2xl px-4 py-3 text-[15px] font-medium transition ${
                    isActive
                      ? "bg-violet-700 text-white shadow-lg"
                      : "text-violet-700 hover:bg-violet-50"
                  }`
                }
              >
                <ShieldAlert size={20} className="shrink-0" />
                <span>Schema Approvals</span>
              </NavLink>
            </div>
          </div>
        ) : null}
      </div>
    </aside>
  );
}
