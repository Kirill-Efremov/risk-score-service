import { Navigate, Route, Routes } from "react-router-dom";
import { AppLayout } from "./components/layout/AppLayout";
import { AnalysisDetailPage } from "./pages/AnalysisDetailPage";
import { HistoryPage } from "./pages/HistoryPage";
import { ImpactGraphPage } from "./pages/ImpactGraphPage";
import { PromotionPage } from "./pages/PromotionPage";
import { RawAnalysisPage } from "./pages/RawAnalysisPage";
import { SettingsPage } from "./pages/SettingsPage";
import { UsageMapPage } from "./pages/UsageMapPage";
import { VersionedAnalysisPage } from "./pages/VersionedAnalysisPage";

export default function App() {
  return (
    <AppLayout>
      <Routes>
        <Route path="/" element={<Navigate to="/promotion" replace />} />
        <Route path="/raw-analysis" element={<RawAnalysisPage />} />
        <Route path="/versioned-analysis" element={<VersionedAnalysisPage />} />
        <Route path="/promotion" element={<PromotionPage />} />
        <Route path="/usage-map" element={<UsageMapPage />} />
        <Route path="/impact-graph" element={<ImpactGraphPage />} />
        <Route path="/history" element={<HistoryPage />} />
        <Route path="/history/:id" element={<AnalysisDetailPage />} />
        <Route path="/settings" element={<SettingsPage />} />
        <Route path="*" element={<Navigate to="/promotion" replace />} />
      </Routes>
    </AppLayout>
  );
}
