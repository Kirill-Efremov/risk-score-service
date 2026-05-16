import {
  Activity,
  Clock3,
  GitCompareArrows,
  Send,
  Settings,
  ShieldCheck,
  Waypoints,
} from "lucide-react";
import { NavLink } from "react-router-dom";

const links = [
  { to: "/promotion", label: "Promotion", icon: Send },
  { to: "/raw-analysis", label: "Raw Analysis", icon: GitCompareArrows },
  { to: "/versioned-analysis", label: "Versioned Analysis", icon: ShieldCheck },
  { to: "/usage-map", label: "Usage Map", icon: Activity },
  { to: "/impact-graph", label: "Impact Graph", icon: Waypoints },
  { to: "/history", label: "History", icon: Clock3 },
  { to: "/settings", label: "Settings", icon: Settings },
];

export function Sidebar() {
  return (
    <aside className="flex w-full lg:w-[260px] shrink-0 self-start rounded-[28px] border border-white/70 bg-white/80 p-5 shadow-panel backdrop-blur lg:sticky lg:top-6">
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
              <span className="leading-7">
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
                ) : label === "Usage Map" ? (
                  <>
                    Usage
                    <br />
                    Map
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
      </div>
    </aside>
  );
}
