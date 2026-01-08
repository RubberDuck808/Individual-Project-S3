import { BrowserRouter, Routes, Route } from "react-router-dom";
import React from "react";
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

export default function App() {
  const USE_SPOOF = false;
  const Provider = USE_SPOOF ? SimulatedRouteProvider : RealLocationProvider;

  return (
    <Provider>
      <BrowserRouter>
        <Routes>
          {/* Public */}
          <Route path="/" element={<HomePage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/signup" element={<SignupPage />} />

          {/* Protected */}
          <Route element={<ProtectedRoute />}>
            <Route element={<MainLayout />}>
              <Route path="/map" element={<MapPage />} />
              <Route path="/profile/:username" element={<ProfilePage />} />
              <Route path="/car" element={<CarHealthPage />} />
              <Route path="/settings" element={<SettingsPage />} />
            </Route>
          </Route>
        </Routes>
      </BrowserRouter>
    </Provider>
  );
}
