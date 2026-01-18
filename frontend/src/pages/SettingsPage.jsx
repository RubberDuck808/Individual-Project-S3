import React, { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { ArrowLeft, UserRound, UserCog, Users, Cpu } from "lucide-react";
import { getStoredUser } from "../api/userApi";

import SettingsSidebar from "../components/settings/SettingsSidebar";
import SectionShell from "../components/settings/sections/SectionShell";

import ProfileSettingsSection from "../components/settings/sections/ProfileSettingsSection";
import AccountSettingsSection from "../components/settings/sections/AccountSettingsSection";
import FriendsSettingsSection from "../components/settings/sections/FriendsSettingsSection";
import DeviceSettingsSection from "../components/settings/sections/DeviceSettingsSection";

export default function SettingsPage() {
  const navigate = useNavigate();

  const [me, setMe] = useState(null);
  const [activeKey, setActiveKey] = useState("profile");

  useEffect(() => {
    const user = getStoredUser();
    setMe(user);
  }, []);

  const items = useMemo(
    () => [
      { key: "profile", label: "Profile", icon: UserRound },
      { key: "account", label: "Account", icon: UserCog },
      { key: "device", label: "Device", icon: Cpu },
      { key: "friends", label: "Friends", icon: Users },
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

  // SettingsPage.jsx return update:
return (
    <div className="min-h-screen bg-[#FFFDF5] text-black pb-20">
      <div className="max-w-6xl mx-auto px-6 pt-10">
        
        {/* Neo-Brutalist Back Button */}
        <button
          onClick={handleBack}
          className="mb-8 flex items-center gap-2 px-4 py-2 bg-white border-[3px] border-black rounded-xl font-black uppercase text-xs shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] hover:translate-y-0.5 hover:shadow-none transition-all"
        >
          <ArrowLeft size={16} strokeWidth={3} /> Return
        </button>

        <h1 className="text-5xl font-[1000] uppercase italic tracking-tighter mb-10">
          System <span className="text-[#FF6AC1]">Settings</span>
        </h1>

        <div className="grid grid-cols-1 md:grid-cols-[320px,1fr] gap-10">
          {/* FIXED: Passing the actual variables here instead of ... */}
          <SettingsSidebar 
            items={items}
            activeKey={activeKey}
            onChange={setActiveKey}
            onLogout={handleLogout}
          />

          <div className="min-w-0">
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

              {activeKey === "device" && (
                <DeviceSettingsSection me={me} />
              )}

              {activeKey === "friends" && (
                <FriendsSettingsSection me={me} />
              )}
            </SectionShell>
          </div>
        </div>
      </div>
    </div>
  );
}
