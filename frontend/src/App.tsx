import { Navigate, Route, Routes } from "react-router-dom";
import { AdminRoute } from "./auth/AdminRoute";
import { ProtectedRoute } from "./auth/ProtectedRoute";
import { AppLayout } from "./components/layout/AppLayout";
import { AdminSchemaApprovalsPage } from "./pages/AdminSchemaApprovalsPage";
import { AdminUsersPage } from "./pages/AdminUsersPage";
import { AnalysisDetailPage } from "./pages/AnalysisDetailPage";
import { HistoryPage } from "./pages/HistoryPage";
import { ImpactGraphPage } from "./pages/ImpactGraphPage";
import { LoginPage } from "./pages/LoginPage";
import { PromotionPage } from "./pages/PromotionPage";
import { RawAnalysisPage } from "./pages/RawAnalysisPage";
import { RegisterPage } from "./pages/RegisterPage";
import { ServiceDetailPage } from "./pages/ServiceDetailPage";
import { ServicesPage } from "./pages/ServicesPage";
import { SettingsPage } from "./pages/SettingsPage";
import { UsageMapPage } from "./pages/UsageMapPage";
import { VersionedAnalysisPage } from "./pages/VersionedAnalysisPage";

export default function App() {
  return (
    <AppLayout>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/" element={<ProtectedRoute><Navigate to="/promotion" replace /></ProtectedRoute>} />
        <Route path="/raw-analysis" element={<ProtectedRoute><RawAnalysisPage /></ProtectedRoute>} />
        <Route path="/versioned-analysis" element={<ProtectedRoute><VersionedAnalysisPage /></ProtectedRoute>} />
        <Route path="/promotion" element={<ProtectedRoute><PromotionPage /></ProtectedRoute>} />
        <Route path="/services" element={<ProtectedRoute><ServicesPage /></ProtectedRoute>} />
        <Route path="/services/:serviceId" element={<ProtectedRoute><ServiceDetailPage /></ProtectedRoute>} />
        <Route path="/usage-map" element={<ProtectedRoute><UsageMapPage /></ProtectedRoute>} />
        <Route path="/impact-graph" element={<ProtectedRoute><ImpactGraphPage /></ProtectedRoute>} />
        <Route path="/history" element={<ProtectedRoute><HistoryPage /></ProtectedRoute>} />
        <Route path="/history/:id" element={<ProtectedRoute><AnalysisDetailPage /></ProtectedRoute>} />
        <Route path="/settings" element={<ProtectedRoute><SettingsPage /></ProtectedRoute>} />
        <Route path="/admin/users" element={<AdminRoute><AdminUsersPage /></AdminRoute>} />
        <Route path="/admin/schema-approvals" element={<AdminRoute><AdminSchemaApprovalsPage /></AdminRoute>} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </AppLayout>
  );
}
