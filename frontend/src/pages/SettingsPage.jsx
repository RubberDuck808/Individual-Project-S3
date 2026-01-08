import React, { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { ArrowLeft, UserRound, UserCog, Accessibility, Bell } from "lucide-react";
import { useTheme } from "../context/ThemeContext";
import { getStoredUser } from "../api/userApi";

import SettingsSidebar from "../components/settings/SettingsSidebar";
import SectionShell from "../components/settings/sections/SectionShell";

import ProfileSettingsSection from "../components/settings/sections/ProfileSettingsSection";
import AccountSettingsSection from "../components/settings/sections/AccountSettingsSection";
import AccessibilitySettingsSection from "../components/settings/sections/AccessibilitySettingsSection";
import NotificationsSettingsSection from "../components/settings/sections/NotificationsSettingsSection";

export default function SettingsPage() {
  const navigate = useNavigate();
  const { darkMode, setDarkMode } = useTheme();

  const [me, setMe] = useState(null);
  const [activeKey, setActiveKey] = useState("profile");
  const [notificationsEnabled, setNotificationsEnabled] = useState(true);

  useEffect(() => {
    const user = getStoredUser();
    setMe(user);
  }, []);

  const items = useMemo(
    () => [
      { key: "profile", label: "Profile", icon: UserRound },
      { key: "account", label: "Account", icon: UserCog },
      { key: "accessibility", label: "Accessibility", icon: Accessibility },
      { key: "notifications", label: "Notifications", icon: Bell },
    ],
    []
  );

  const handleBack = () => {
    const u = getStoredUser();
    const username = u?.username || me?.username;
    if (username) navigate(`/profile/${username}`);
    else navigate(-1);
  };

  const handleLogout = () => {
    localStorage.removeItem("user");
    localStorage.removeItem("token");
    navigate("/");
  };

  return (
    <div
      className={`min-h-screen px-6 pt-2 pb-6 ${
        darkMode ? "bg-gray-900 text-white" : "bg-gray-100 text-gray-900"
      }`}
    >
      <button
        onClick={handleBack}
        className={`flex items-center gap-1 mb-2 ${
          darkMode ? "text-gray-300 hover:text-white" : "text-gray-700 hover:text-gray-900"
        }`}
        type="button"
      >
        <ArrowLeft /> Back
      </button>

      <div className="w-full max-w-7xl mx-auto">
        <h1 className="text-2xl font-bold mb-4">Settings</h1>

        <div className="grid grid-cols-1 md:grid-cols-[360px,minmax(0,1fr)] gap-8 items-start">
          <SettingsSidebar
            items={items}
            activeKey={activeKey}
            onChange={setActiveKey}
            onLogout={handleLogout}
          />

          {/* RIGHT COLUMN */}
          <div className="w-full min-w-0">
            <SectionShell>
              {activeKey === "profile" && (
                <ProfileSettingsSection me={me} setMe={setMe} />
              )}

              {activeKey === "account" && (
                <AccountSettingsSection
                  me={me}
                  setMe={setMe}
                  onUsernameChanged={(newUsername) => {
                    navigate(`/profile/${newUsername}`, { replace: true });
                  }}
                />
              )}

              {activeKey === "accessibility" && (
                <AccessibilitySettingsSection darkMode={darkMode} setDarkMode={setDarkMode} />
              )}

              {activeKey === "notifications" && (
                <NotificationsSettingsSection
                  enabled={notificationsEnabled}
                  setEnabled={setNotificationsEnabled}
                />
              )}
            </SectionShell>
          </div>
        </div>
      </div>
    </div>
  );
}
