import { useLocation } from "react-router-dom";

const titles: Record<string, string> = {
  "/promotion": "Controlled Promotion",
  "/raw-analysis": "Raw Analysis",
  "/versioned-analysis": "Versioned Analysis",
  "/usage-map": "Usage Map",
  "/history": "Analysis History",
  "/impact-graph": "Impact Graph",
  "/settings": "Settings",
};

export function Header() {
  const location = useLocation();
  const title = titles[location.pathname] || "Risk Score Service";

  return (
    <header className="panel px-6 py-5">
      <div className="flex flex-col gap-3 md:flex-row md:items-center md:justify-between">
        <div>
          <p className="text-sm uppercase tracking-[0.24em] text-slate-500">Operator console</p>
          <h2 className="mt-2 text-3xl font-semibold text-slate-900">{title}</h2>
        </div>
      </div>
    </header>
  );
}
