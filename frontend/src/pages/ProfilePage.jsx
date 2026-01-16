
import React, { useEffect, useMemo, useState } from "react";
import { fetchCurrentUser, fetchUserByUsername } from "../api/userApi";
import { useTheme } from "../context/ThemeContext";
import { useNavigate, useParams } from "react-router-dom";
import { Users, RadioTower, Settings } from "lucide-react";
import ProfileTabs from "../components/profile/tabs/ProfileTabs";
import FriendsTab from "../components/profile/tabs/FriendsTab";
import StatsGrid from "../components/profile/stats/StatsGrid";
import { useUserStats } from "../components/profile/stats/useUserStats";
import UserAvatar from "../components/profile/avatars/UserAvatar";

export default function ProfilePage() {
  const { darkMode } = useTheme();
  const navigate = useNavigate();
  const { username } = useParams();

  const [me, setMe] = useState(null);
  const [profileUser, setProfileUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState("summary");

  // Reset active tab and clear profileUser when username changes
  useEffect(() => {
    setActiveTab("summary");
    setProfileUser(null); // Clear previous user data to prevent stale redirects
  }, [username]);

  useEffect(() => {
    let cancelled = false;

    (async () => {
      setLoading(true);
      try {
        const current = await fetchCurrentUser();
        if (cancelled) return;

        setMe(current);

        // Use case-insensitive comparison to handle username casing differences
        const usernameLower = username?.toLowerCase();
        const currentUsernameLower = current.username?.toLowerCase();
        
        if (!username || usernameLower === currentUsernameLower) {
          setProfileUser(current);
        } else {
          const other = await fetchUserByUsername(username);
          if (!cancelled) setProfileUser(other);
        }
      } catch (e) {
        console.error("Failed to load profile:", e);
        if (!cancelled) {
          setProfileUser(null);
        }
      } finally {
        if (!cancelled) setLoading(false);
      }
    })();

    return () => {
      cancelled = true;
    };
  }, [username]);

  // Only redirect if we're viewing ourselves but URL doesn't match (and not loading)
  // This should only fire when the profileUser is actually loaded and matches 'me'
  useEffect(() => {
    // Don't run redirect check during loading or if data isn't ready
    if (loading || !me?.username || !profileUser?.username) return;

    // Only redirect if:
    // 1. The profileUser loaded is actually 'me' (we're viewing ourselves)
    // 2. The URL parameter doesn't match our username
    // This prevents redirects when navigating to other users' profiles
    const meUsernameLower = me.username?.toLowerCase();
    const profileUsernameLower = profileUser.username?.toLowerCase();
    const urlUsernameLower = username?.toLowerCase();
    
    // Only redirect if profileUser IS me, but URL shows a different username
    // This means we loaded our own profile but URL is wrong
    const viewingMeButUrlStale =
      meUsernameLower === profileUsernameLower && 
      urlUsernameLower && 
      urlUsernameLower !== meUsernameLower;

    if (viewingMeButUrlStale) {
      // Only redirect if we're actually viewing ourselves (not a friend)
      navigate(`/profile/${me.username}`, { replace: true });
    }
  }, [me?.username, profileUser?.username, username, navigate, loading]);

  const isMe = !!me && !!profileUser && me.username === profileUser.username;
  
  const tabs = useMemo(() => {
    return [
      { key: "summary", label: "Summary", icon: RadioTower },
      { key: "friends", label: "Friends", icon: Users },
    ];
  }, []);

  const { stats } = useUserStats(profileUser?.username);

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
      alt="Profile background"
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
