import React, { useState } from "react";
import { Outlet, Link, useLocation, useNavigate } from "react-router-dom";
import { 
  LayoutDashboard, 
  Users, 
  Cpu, 
  Image, 
  BarChart3,
  LogOut,
  Menu,
  X
} from "lucide-react";
import { logout } from "../api/auth";

const adminMenuItems = [
  { key: "dashboard", label: "Dashboard", icon: LayoutDashboard, path: "/admin" },
  { key: "users", label: "Users", icon: Users, path: "/admin/users" },
  { key: "devices", label: "Devices", icon: Cpu, path: "/admin/devices" },
  { key: "assets", label: "Assets", icon: Image, path: "/admin/assets" },
  { key: "statistics", label: "Statistics", icon: BarChart3, path: "/admin/statistics" },
];

export default function AdminLayout() {
  const location = useLocation();
  const navigate = useNavigate();
  const [sidebarOpen, setSidebarOpen] = useState(false);

  const handleLogout = () => {
    logout().then(() => {
      navigate("/login");
    }).catch(() => {
      // Even if logout fails, navigate to login
      navigate("/login");
    });
  };

  const activeKey = adminMenuItems.find(item => location.pathname.startsWith(item.path))?.key || "dashboard";

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Mobile sidebar toggle */}
      <div className="lg:hidden fixed top-0 left-0 right-0 z-50 bg-white border-b-2 border-black p-4 flex items-center justify-between">
        <h1 className="font-black text-xl">Admin Panel</h1>
        <button
          onClick={() => setSidebarOpen(!sidebarOpen)}
          className="p-2 hover:bg-gray-100 rounded-lg"
        >
          {sidebarOpen ? <X size={24} /> : <Menu size={24} />}
        </button>
      </div>

      <div className="flex">
        {/* Sidebar */}
        <aside
          className={`
            fixed lg:static inset-y-0 left-0 z-40
            w-64 bg-white border-r-2 border-black
            transform ${sidebarOpen ? "translate-x-0" : "-translate-x-full lg:translate-x-0"}
            transition-transform duration-300 ease-in-out
            pt-16 lg:pt-0
          `}
        >
          <div className="h-full flex flex-col">
            {/* Logo/Header */}
            <div className="p-6 border-b-2 border-black hidden lg:block">
              <h1 className="font-black text-2xl">Admin Panel</h1>
            </div>

            {/* Navigation */}
            <nav className="flex-1 p-4 space-y-2">
              {adminMenuItems.map((item) => {
                const Icon = item.icon;
                const isActive = activeKey === item.key;
                return (
                  <Link
                    key={item.key}
                    to={item.path}
                    onClick={() => setSidebarOpen(false)}
                    className={`
                      flex items-center gap-3 px-4 py-3 rounded-lg
                      font-bold transition-colors
                      ${isActive
                        ? "bg-[#00D1FF] text-black shadow-[4px_4px_0px_0px_rgba(0,0,0,1)]"
                        : "bg-white text-black hover:bg-gray-100 border-2 border-black"
                      }
                    `}
                  >
                    <Icon size={20} />
                    <span>{item.label}</span>
                  </Link>
                );
              })}
            </nav>

            {/* Logout */}
            <div className="p-4 border-t-2 border-black">
              <button
                onClick={handleLogout}
                className="w-full flex items-center gap-3 px-4 py-3 rounded-lg bg-red-500 text-white font-bold hover:bg-red-600 transition-colors border-2 border-black shadow-[4px_4px_0px_0px_rgba(0,0,0,1)]"
              >
                <LogOut size={20} />
                <span>Logout</span>
              </button>
            </div>
          </div>
        </aside>

        {/* Overlay for mobile */}
        {sidebarOpen && (
          <button
            type="button"
            className="lg:hidden fixed inset-0 bg-black bg-opacity-50 z-30 border-none p-0 cursor-pointer"
            onClick={() => setSidebarOpen(false)}
            onKeyDown={(e) => {
              if (e.key === 'Escape') {
                setSidebarOpen(false);
              }
            }}
            aria-label="Close sidebar"
          />
        )}

        {/* Main content */}
        <main className="flex-1 lg:ml-0 pt-16 lg:pt-0">
          <div className="p-6">
            <Outlet />
          </div>
        </main>
      </div>
    </div>
  );
}
