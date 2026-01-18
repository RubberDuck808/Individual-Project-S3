import React, { lazy, Suspense } from "react";
import { Routes, Route } from "react-router-dom";

import MainLayout from "./layouts/MainLayout";
import MapPage from "./pages/MapPage";
import ProfilePage from "./pages/ProfilePage";
import CarHealthPage from "./pages/CarHealthPage";
import SettingsPage from "./pages/SettingsPage";
import LoginPage from "./pages/LoginPage";
import SignupPage from "./pages/SignupPage";
import HomePage from "./pages/HomePage";
import ProtectedRoute from "./components/ProtectedRoute";

import RealLocationProvider from "./providers/RealLocationProvider";
import SimulatedRouteProvider from "./providers/SimulatedRouteProvider";
import { AssetsCacheProvider } from "./context/AssetsCacheContext";

// Lazy load admin pages (code splitting)
const AdminLayout = lazy(() => import("./layouts/AdminLayout"));
const AdminDashboardPage = lazy(() => import("./pages/admin/AdminDashboardPage"));
const AdminUsersPage = lazy(() => import("./pages/admin/AdminUsersPage"));
const AdminDevicesPage = lazy(() => import("./pages/admin/AdminDevicesPage"));
const AdminAssetsPage = lazy(() => import("./pages/admin/AdminAssetsPage"));

const LoadingSpinner = () => (
  <div className="min-h-screen flex items-center justify-center">
    <div className="text-center">
      <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-black mx-auto mb-4"></div>
      <p className="font-bold">Loading...</p>
    </div>
  </div>
);

export default function App() {
  const USE_SPOOF = false;
  const Provider = USE_SPOOF ? SimulatedRouteProvider : RealLocationProvider;

  return (
    <Provider>
      <AssetsCacheProvider>
        <Routes>
          {/* Public */}
          <Route path="/" element={<HomePage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/signup" element={<SignupPage />} />

          {/* Protected User Routes */}
          <Route element={<ProtectedRoute />}>
            <Route element={<MainLayout />}>
              <Route path="/map" element={<MapPage />} />
              <Route path="/profile/:username" element={<ProfilePage />} />
              <Route path="/car" element={<CarHealthPage />} />
              <Route path="/settings" element={<SettingsPage />} />
            </Route>
          </Route>

          {/* Protected Admin Routes - Lazy Loaded */}
          <Route element={<ProtectedRoute requireRole="ADMIN" />}>
            <Route
              element={
                <Suspense fallback={<LoadingSpinner />}>
                  <AdminLayout />
                </Suspense>
              }
            >
              <Route
                path="/admin"
                element={
                  <Suspense fallback={<LoadingSpinner />}>
                    <AdminDashboardPage />
                  </Suspense>
                }
              />
              <Route
                path="/admin/users"
                element={
                  <Suspense fallback={<LoadingSpinner />}>
                    <AdminUsersPage />
                  </Suspense>
                }
              />
              <Route
                path="/admin/devices"
                element={
                  <Suspense fallback={<LoadingSpinner />}>
                    <AdminDevicesPage />
                  </Suspense>
                }
              />
              <Route
                path="/admin/assets"
                element={
                  <Suspense fallback={<LoadingSpinner />}>
                    <AdminAssetsPage />
                  </Suspense>
                }
              />
            </Route>
          </Route>
        </Routes>
      </AssetsCacheProvider>
    </Provider>
  );
}
