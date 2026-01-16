import { Navigate, Outlet } from "react-router-dom";
import { useEffect, useState } from "react";
import PropTypes from "prop-types";
import { fetchCurrentUser } from "../api/userApi";

export default function ProtectedRoute({ requireRole }) {
  const [loading, setLoading] = useState(true);
  const [user, setUser] = useState(null);
  const [error, setError] = useState(null);

  useEffect(() => {
    const loadUser = async () => {
      try {
        const userData = await fetchCurrentUser();
        setUser(userData);
      } catch (err) {
        setError(err);
      } finally {
        setLoading(false);
      }
    };

    loadUser();
  }, []);

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-black mx-auto mb-4"></div>
          <p className="font-bold">Loading...</p>
        </div>
      </div>
    );
  }

  if (error || !user) {
    return <Navigate to="/login" replace />;
  }

  // Role-based check
  if (requireRole) {
    const userRole = user.roleName?.toUpperCase();
    const requiredRole = requireRole.toUpperCase();
    
    if (userRole !== requiredRole) {
      return <Navigate to="/" replace />;
    }
  }

  return <Outlet />;
}

ProtectedRoute.propTypes = {
  requireRole: PropTypes.string,
};
