import React, { useMemo } from "react";
import { Outlet, NavLink, useLocation } from "react-router-dom";
import { Map, User, Car } from "lucide-react";
import { useTheme } from "../context/ThemeContext";
import { getStoredUser } from "../api/auth";

export default function MainLayout() {
  const location = useLocation();
  const { darkMode } = useTheme();

  const hideNav =
    location.pathname === "/" || location.pathname === "/settings";

  const profilePath = useMemo(() => {
    const me = getStoredUser();
    const username = me?.username ? String(me.username).toLowerCase() : "me";
    return `/profile/${username}`;
  }, []);

  return (
    <div
      className={`flex flex-col h-screen transition-colors duration-300 ${
        darkMode ? "bg-gray-900 text-white" : "bg-gray-50 text-gray-900"
      }`}
    >
      {/* Allow vertical scrolling for long pages */}
      <main className="flex-1 overflow-y-auto">
        <Outlet />
      </main>

      {/* Bottom navigation (icons only) */}
      {!hideNav && (
        <nav
          className={`h-16 backdrop-blur-md flex justify-around items-center border-t transition-colors duration-300 ${
            darkMode
              ? "bg-gray-800/80 border-gray-700 text-gray-300"
              : "bg-white/80 border-gray-200 text-gray-600"
          }`}
        >
          {[
            { to: profilePath, icon: User },
            { to: "/map", icon: Map },
            { to: "/car", icon: Car },
          ].map(({ to, icon: Icon }) => (
            <NavLink
              key={to}
              to={to}
              className={({ isActive }) =>
                `flex items-center justify-center p-2 transition-all duration-200 rounded-xl ${
                  isActive
                    ? darkMode
                      ? "text-blue-400 scale-110 bg-blue-900/30"
                      : "text-blue-600 scale-110 bg-blue-50"
                    : "hover:scale-105"
                }`
              }
            >
              <Icon size={26} strokeWidth={2.2} />
            </NavLink>
          ))}
        </nav>
      )}
    </div>
  );
}
