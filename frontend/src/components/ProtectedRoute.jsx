import { Navigate, Outlet } from "react-router-dom";
import React from "react";

export default function ProtectedRoute() {
  const user = JSON.parse(localStorage.getItem("user"));

  // If no user, redirect to login
  if (!user) {
    return <Navigate to="/login" replace />;
  }

  // Otherwise, render nested routes
  return <Outlet />;
}
