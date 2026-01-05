import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { ArrowLeft } from "lucide-react";
import { useTheme } from "../context/ThemeContext";
import { getStoredUser, updateCurrentUser } from "../api/users";

export default function SettingsPage() {
  const navigate = useNavigate();
  const { darkMode, setDarkMode } = useTheme();
  const [notificationsEnabled, setNotificationsEnabled] = useState(true);

  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  const [form, setForm] = useState({
    name: "",
    username: "",
    email: "",
    currentPassword: "",
    newPassword: "",
  });

  const [myUsername, setMyUsername] = useState("");

  useEffect(() => {
    const user = getStoredUser();
    if (user) {
      setMyUsername(user.username || "");
      setForm((f) => ({
        ...f,
        name: user.name || "",
        username: user.username || "",
        email: user.email || "",
      }));
    }
  }, []);

  const onChange = (key) => (e) => {
    setForm((prev) => ({ ...prev, [key]: e.target.value }));
  };

  const handleBack = () => {
    const u = getStoredUser();
    const username = u?.username || myUsername;
    if (username) navigate(`/profile/${username}`);
    else navigate(-1);
  };

  const handleSave = async () => {
    setError("");
    setSuccess("");
    setSaving(true);

    try {
      const payload = {
        name: form.name,
        username: form.username,
        email: form.email,
        currentPassword: form.currentPassword || undefined,
        newPassword: form.newPassword || undefined,
      };

      const updatedUser = await updateCurrentUser(payload);
      localStorage.setItem("user", JSON.stringify(updatedUser));

      setMyUsername(updatedUser.username || "");
      setSuccess("Account updated successfully.");
      setForm((f) => ({ ...f, currentPassword: "", newPassword: "" }));

      navigate(`/profile/${updatedUser.username}`, { replace: true });
    } catch (e) {
      setError(e?.message || "Failed to update account");
    } finally {
      setSaving(false);
    }
  };

  const handleLogout = () => {
    localStorage.removeItem("user");
    localStorage.removeItem("token");
    navigate("/");
  };

  return (
    <div className={`h-full p-6 relative ${darkMode ? "bg-gray-900 text-white" : "bg-gray-100 text-gray-900"}`}>
      <button
        onClick={handleBack}
        className={`absolute top-4 left-4 flex items-center ${
          darkMode ? "text-gray-300 hover:text-white" : "text-gray-700 hover:text-gray-900"
        }`}
      >
        <ArrowLeft className="mr-1" /> Back
      </button>

      <h1 className="text-2xl font-bold text-center mb-8">Settings</h1>

      <div className="space-y-6">
        {/* Appearance */}
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
              />
            </button>
          </div>
        </div>

        {/* Notifications */}
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
              />
            </button>
          </div>
        </div>

        {/* Account credentials */}
        <div className={`${darkMode ? "bg-gray-800" : "bg-white"} rounded-xl shadow p-4`}>
          <h2 className="text-lg font-semibold mb-3">Account credentials</h2>

          {error && <p className="text-sm text-red-400 mb-2">{error}</p>}
          {success && <p className="text-sm text-green-400 mb-2">{success}</p>}

          <div className="space-y-3">
            <input
              placeholder="Name"
              value={form.name}
              onChange={onChange("name")}
              className="w-full p-2 rounded-lg border bg-transparent"
            />
            <input
              placeholder="Username"
              value={form.username}
              onChange={onChange("username")}
              className="w-full p-2 rounded-lg border bg-transparent"
            />
            <input
              placeholder="Email"
              value={form.email}
              onChange={onChange("email")}
              className="w-full p-2 rounded-lg border bg-transparent"
            />
            <input
              type="password"
              placeholder="Current password (required for email/password change)"
              value={form.currentPassword}
              onChange={onChange("currentPassword")}
              className="w-full p-2 rounded-lg border bg-transparent"
            />
            <input
              type="password"
              placeholder="New password"
              value={form.newPassword}
              onChange={onChange("newPassword")}
              className="w-full p-2 rounded-lg border bg-transparent"
            />

            <button
              onClick={handleSave}
              disabled={saving}
              className={`w-full py-2 rounded-lg font-semibold transition ${
                saving ? "opacity-60 cursor-not-allowed" : "hover:opacity-95"
              } bg-blue-600 text-white`}
            >
              {saving ? "Saving..." : "Save changes"}
            </button>
          </div>
        </div>

        {/* Logout */}
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
