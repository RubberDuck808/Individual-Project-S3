import React, { useMemo } from "react";
import { Outlet, NavLink, useLocation } from "react-router-dom";
import { Map, User, Car } from "lucide-react";
import { getStoredUser } from "../api/auth";

function cx(...classes) {
  return classes.filter(Boolean).join(" ");
}

export default function MainLayout() {
  const location = useLocation();

  // Navigation is hidden on specific pages
  const hideNav = location.pathname === "/" || location.pathname === "/settings";

  const profilePath = useMemo(() => {
    const me = getStoredUser();
    const username = me?.username ? String(me.username).toLowerCase() : "me";
    return `/profile/${username}`;
  }, []);

  return (
    <div className="flex flex-col h-screen bg-[#FFFDF5] text-black font-bold overflow-hidden">
      {/* Main Content Area */}
      <main className="flex-1 overflow-y-auto relative">
        <Outlet />
      </main>

      {/* Neo-Brutalist Bottom Nav Dock */}
      {!hideNav && (
        <div className="fixed bottom-6 left-1/2 -translate-x-1/2 w-[90%] max-w-md z-[200]">
          <nav
            className={cx(
              "h-20 bg-white border-[4px] border-black rounded-[2.5rem] flex justify-around items-center px-4",
              "shadow-[8px_8px_0px_0px_rgba(0,0,0,1)]"
            )}
          >
            {[
              { to: profilePath, icon: User, label: "User", color: "#FF6AC1" },
              { to: "/map", icon: Map, label: "Map", color: "#00D1FF" },
              { to: "/car", icon: Car, label: "Drive", color: "#FFD600" },
            ].map(({ to, icon: Icon, color }) => (
              <NavLink
                key={to}
                to={to}
                className={({ isActive }) =>
                  cx(
                    "relative flex flex-col items-center justify-center w-14 h-14 transition-all duration-200 rounded-2xl border-[3px]",
                    isActive
                      ? "bg-black text-white border-black translate-y-1 shadow-none"
                      : "bg-white text-black border-transparent hover:border-black hover:-translate-y-1 hover:shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] active:translate-y-0 active:shadow-none"
                  )
                }
              >
                {({ isActive }) => (
                  <>
                    <Icon 
                      size={24} 
                      strokeWidth={isActive ? 3 : 2} 
                      color={isActive ? color : "currentColor"} 
                    />
                    {/* Active Indicator Dot */}
                    {isActive && (
                      <div 
                        className="absolute -bottom-1.5 w-1.5 h-1.5 rounded-full border border-black" 
                        style={{ backgroundColor: color }}
                      />
                    )}
                  </>
                )}
              </NavLink>
            ))}
          </nav>
        </div>
      )}
    </div>
  );
}