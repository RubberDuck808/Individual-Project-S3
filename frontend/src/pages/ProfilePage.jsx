
import React, { useEffect, useMemo, useState } from "react";
import { fetchCurrentUser, fetchUserByUsername } from "../api/userApi";
import { useTheme } from "../context/ThemeContext";
import { useNavigate, useParams } from "react-router-dom";
import { Users, RadioTower } from "lucide-react";


import { Settings, ArrowLeft, Edit3 } from "lucide-react";
import ProfileTabs from "../components/profile/tabs/ProfileTabs";
import FriendsTab from "../components/profile/tabs/FriendsTab";
import StatsGrid from "../components/profile/stats/StatsGrid";
import { useUserStats } from "../components/profile/stats/useUserStats";
import ProfileHeader from "../components/profile/ProfileHeader";
import UserAvatar from "../components/profile/avatars/UserAvatar";

export default function ProfilePage() {
  const { darkMode } = useTheme();
  const navigate = useNavigate();
  const { username } = useParams();

  const [me, setMe] = useState(null);
  const [profileUser, setProfileUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState("summary");

  useEffect(() => {
    let cancelled = false;

    (async () => {
      setLoading(true);
      try {
        const current = await fetchCurrentUser();
        if (cancelled) return;

        setMe(current);

        if (!username || username === current.username) {
          setProfileUser(current);
        } else {
          const other = await fetchUserByUsername(username);
          if (!cancelled) setProfileUser(other);
        }
      } catch (e) {
        console.error(e);
        if (!cancelled) setProfileUser(null);
      } finally {
        if (!cancelled) setLoading(false);
      }
    })();

    return () => {
      cancelled = true;
    };
  }, [username]);

  useEffect(() => {
    if (!me?.username || !profileUser?.username) return;

    const viewingMeButUrlStale =
      me.username === profileUser.username && username !== me.username;

    if (viewingMeButUrlStale) {
      navigate(`/profile/${me.username}`, { replace: true });
    }
  }, [me?.username, profileUser?.username, username, navigate]);

  const isMe = !!me && !!profileUser && me.username === profileUser.username;
  const { stats } = useUserStats(profileUser?.username);

  const tabs = useMemo(() => {
    return [
      { key: "summary", label: "Summary", icon: RadioTower },
      { key: "friends", label: "Friends", icon: Users },
    ];
  }, []);

  if (loading) {
    return (
      <div
        className={`min-h-screen ${
          darkMode ? "bg-gray-950 text-white" : "bg-gray-50 text-gray-900"
        }`}
      >
        <div className="max-w-5xl mx-auto px-4 py-10">Loading profile…</div>
      </div>
    );
  }

  if (!profileUser) {
    return (
      <div
        className={`min-h-screen ${
          darkMode ? "bg-gray-950 text-white" : "bg-gray-50 text-gray-900"
        }`}
      >
        <div className="max-w-5xl mx-auto px-4 py-10">Profile not found.</div>
      </div>
    );
  }

  const bgUrl = profileUser?.backgroundUrl;

  // ... logic remains same, update the return statement
return (
  <div className="min-h-screen bg-[#FFFDF5]">
    {/* Background Image / Solid Wall */}
    {/* Background Image / Solid Wall */}
<div className="fixed inset-0 z-0">
  {bgUrl ? (
    <img 
      src={bgUrl} 
      className="w-full h-full object-cover" // Removed opacity and blur
    />
  ) : (
    <div className="w-full h-full bg-[#FFFDF5]" />
  )}
  {/* OPTIONAL: Add a very light dark tint ONLY if your white text becomes unreadable */}
  {/* <div className="absolute inset-0 bg-black/10" /> */}
</div>

    <div className="relative z-10 max-w-5xl mx-auto px-4 py-12 pb-32">
      {/* THE MAIN CARD */}
      <div className="bg-white border-[4px] border-black rounded-[3rem] p-8 md:p-12 shadow-[12px_12px_0px_0px_rgba(0,0,0,1)]">
        
        {/* Header Section */}
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-8">
          <div className="flex items-center gap-6">
            <div className="p-1 border-[4px] border-black rounded-[2rem] bg-white shadow-[6px_6px_0px_0px_#00D1FF]">
               <UserAvatar user={profileUser} size={84} className="rounded-[1.5rem]" />
            </div>
            <div>
              <h1 className="text-4xl font-[1000] italic uppercase tracking-tighter leading-none">
                {profileUser.name || profileUser.username}
              </h1>
              <div className="mt-2 inline-block px-3 py-1 bg-black text-[#FFD600] text-sm font-black rounded-lg">
                @{profileUser.username}
              </div>
            </div>
          </div>

          {isMe && (
            <button
              onClick={() => navigate("/settings")}
              className="px-6 py-4 rounded-2xl border-[3px] border-black bg-[#FFD600] font-[1000] uppercase text-xs tracking-widest shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] hover:translate-y-1 hover:shadow-none transition-all flex items-center gap-2"
            >
              <Settings size={18} strokeWidth={3} />
              Account Settings
            </button>
          )}
        </div>

        {/* The Grid */}
        <div className="mt-12">
          <StatsGrid stats={stats} />
        </div>

        {/* Tab Selection */}
        <div className="mt-12 space-y-8">
          <div className="flex justify-center md:justify-start">
            <ProfileTabs tabs={tabs} activeKey={activeTab} onChange={setActiveTab} />
          </div>

          <div className="animate-in fade-in slide-in-from-bottom-4 duration-500">
            {activeTab === "friends" && (
              <FriendsTab user={me} profileUser={profileUser} isMe={isMe} />
            )}
            
            {activeTab === "summary" && (
               <div className="p-8 border-[3px] border-black rounded-[2rem] bg-slate-50 border-dashed">
                  <p className="text-center font-black text-slate-400 uppercase tracking-widest text-sm">
                    Recent Activity Feed Coming Soon
                  </p>
               </div>
            )}
          </div>
        </div>
      </div>
    </div>
  </div>
);
}
