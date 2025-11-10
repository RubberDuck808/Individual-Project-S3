import { useNavigate } from "react-router-dom";
import { ArrowLeft } from "lucide-react";
import { useTheme } from "../context/ThemeContext";
import { useState } from "react";

export default function SettingsPage() {
  const navigate = useNavigate();
  const { darkMode, setDarkMode } = useTheme();
  const [notificationsEnabled, setNotificationsEnabled] = useState(true);

  const handleLogout = () => {
    localStorage.removeItem("user");
    navigate("/login");
  };

  return (
    <div className={`h-full p-6 relative ${darkMode ? "bg-gray-900 text-white" : "bg-gray-100 text-gray-900"}`}>
      {/* Back button */}
      <button
        onClick={() => navigate(-1)}
        className={`absolute top-4 left-4 flex items-center ${darkMode ? "text-gray-300 hover:text-white" : "text-gray-700 hover:text-gray-900"}`}
      >
        <ArrowLeft className="mr-1" /> Back
      </button>

      <h1 className="text-2xl font-bold text-center mb-8">Settings</h1>

      <div className="space-y-6">
        {/* Appearance Section */}
        <div className={`${darkMode ? "bg-gray-800" : "bg-white"} rounded-xl shadow p-4`}>
          <h2 className="text-lg font-semibold mb-3">Appearance</h2>
          <div className="flex items-center justify-between">
            <span>Dark Mode</span>
            <button
              onClick={() => setDarkMode(!darkMode)}
              className={`w-12 h-6 rounded-full relative transition ${darkMode ? "bg-blue-600" : "bg-gray-300"}`}
            >
              <div
                className={`absolute top-1 left-1 w-4 h-4 bg-white rounded-full shadow transition-transform ${
                  darkMode ? "translate-x-6" : "translate-x-0"
                }`}
              ></div>
            </button>
          </div>
        </div>

        {/* Notifications Section */}
        <div className={`${darkMode ? "bg-gray-800" : "bg-white"} rounded-xl shadow p-4`}>
          <h2 className="text-lg font-semibold mb-3">Notifications</h2>
          <div className="flex items-center justify-between">
            <span>Enable notifications</span>
            <button
              onClick={() => setNotificationsEnabled(!notificationsEnabled)}
              className={`w-12 h-6 rounded-full relative transition ${
                notificationsEnabled ? "bg-green-500" : "bg-gray-300"
              }`}
            >
              <div
                className={`absolute top-1 left-1 w-4 h-4 bg-white rounded-full shadow transition-transform ${
                  notificationsEnabled ? "translate-x-6" : "translate-x-0"
                }`}
              ></div>
            </button>
          </div>
        </div>

        {/* Account Section */}
        <div className={`${darkMode ? "bg-gray-800" : "bg-white"} rounded-xl shadow p-4`}>
          <h2 className="text-lg font-semibold mb-3">Account</h2>
          <button
            onClick={handleLogout}
            className="w-full bg-red-600 text-white py-2 rounded-lg hover:bg-red-700 transition"
          >
            Log Out
          </button>
        </div>
      </div>
    </div>
  );
}
