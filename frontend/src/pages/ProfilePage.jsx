import React, { useEffect, useMemo, useState } from "react";
import { fetchCurrentUser, fetchUserByUsername } from "../api/users";
import { useTheme } from "../context/ThemeContext";
import { useNavigate, useParams } from "react-router-dom";
import {
  Settings,
  Users,
  RadioTower,
} from "lucide-react";

import ProfileTabs from "../components/profile/ProfileTabs";
import FriendsTab from "../components/profile/FriendsTab";
// import SummaryTab from "../components/profile/SummaryTab";

import StatsGrid from "../components/profile/stats/StatsGrid";
import { useUserStats } from "../components/profile/stats/useUserStats";

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

        if (username === current.username) {
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

  // If username changed, old /profile/:oldUsername should redirect to /profile/:newUsername
  useEffect(() => {
    if (!me?.id || !profileUser?.id) return;

    const viewingMeButUrlStale =
      me.id === profileUser.id && username !== me.username;

    if (viewingMeButUrlStale) {
      navigate(`/profile/${me.username}`, { replace: true });
    }
  }, [me?.id, me?.username, profileUser?.id, username, navigate]);

  const isMe = !!me && !!profileUser && me.id === profileUser.id;

const { stats, loading: statsLoading, raw: statsDto } =
  useUserStats(profileUser?.username);

  const tabs = useMemo(() => {
    return [
      { key: "summary", label: "Summary", icon: RadioTower },
      { key: "friends", label: "Friends", icon: Users },
    ];
  }, []);

  if (loading) {
    return (
      <div className={`min-h-screen ${darkMode ? "bg-gray-950 text-white" : "bg-gray-50 text-gray-900"}`}>
        <div className="max-w-5xl mx-auto px-4 py-10">Loading profile…</div>
      </div>
    );
  }

  if (!profileUser) {
    return (
      <div className={`min-h-screen ${darkMode ? "bg-gray-950 text-white" : "bg-gray-50 text-gray-900"}`}>
        <div className="max-w-5xl mx-auto px-4 py-10">Profile not found.</div>
      </div>
    );
  }

  return (
    <div className={`min-h-screen ${darkMode ? "bg-gray-950 text-white" : "bg-gray-50 text-gray-900"}`}>
      <div className="max-w-5xl mx-auto px-4 py-10">
        <div className="rounded-3xl shadow-xl bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 p-8">
          <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-6">
            <div className="flex items-center gap-5">
              <div className="w-16 h-16 rounded-2xl bg-gradient-to-br from-blue-600 to-indigo-600 flex items-center justify-center text-white font-extrabold text-2xl">
                {(profileUser?.name?.[0] || profileUser?.username?.[0] || "U").toUpperCase()}
              </div>
              <div>
                <h1 className="text-2xl md:text-3xl font-extrabold">
                  {profileUser.name || profileUser.username}
                </h1>
                <p className="opacity-70 font-semibold">@{profileUser.username}</p>
                {isMe && profileUser.email && <p className="opacity-60 text-sm">{profileUser.email}</p>}
              </div>
            </div>

            <div className="flex gap-3">
              {isMe && (
                <button
                  onClick={() => navigate("/settings")}
                  className="px-4 py-3 rounded-xl border border-gray-200 dark:border-gray-800 bg-gray-50 dark:bg-gray-800/40 font-bold flex items-center gap-2 hover:bg-gray-100 dark:hover:bg-gray-800 transition-colors"
                >
                  <Settings size={18} />
                  Settings
                </button>
              )}
            </div>
          </div>

          <StatsGrid stats={stats} />

          <div className="mt-8">
            <ProfileTabs tabs={tabs} activeKey={activeTab} onChange={setActiveTab} />

            {/* {activeTab === "summary" && (
              <SummaryTab
                stats={stats}
                recentActivity={recentActivity}
                achievements={achievements}
                liveConvoys={liveConvoys}
                convoyInvites={convoyInvites}
              />
            )} */}

            {activeTab === "friends" && <FriendsTab user={me} profileUser={profileUser} isMe={isMe} />}
          </div>
        </div>

        {/* <div className="mt-6 grid grid-cols-1 md:grid-cols-2 gap-4">
          <div className="rounded-3xl border border-gray-200 dark:border-gray-800 bg-white dark:bg-gray-900 p-6">
            <h3 className="font-extrabold text-lg flex items-center gap-2">
              <Clock size={18} className="opacity-70" /> Quick actions
            </h3>
            <div className="mt-4 space-y-2">
              <button
                className="w-full flex items-center justify-between p-4 rounded-2xl bg-gray-50 dark:bg-gray-800/40 border border-gray-200 dark:border-gray-800 hover:bg-gray-100 dark:hover:bg-gray-800 transition-colors font-bold"
                onClick={() => navigate("/hazards")}
              >
                View hazards <ChevronRight size={18} />
              </button>
              <button
                className="w-full flex items-center justify-between p-4 rounded-2xl bg-gray-50 dark:bg-gray-800/40 border border-gray-200 dark:border-gray-800 hover:bg-gray-100 dark:hover:bg-gray-800 transition-colors font-bold"
                onClick={() => navigate("/convoys")}
              >
                View convoys <ChevronRight size={18} />
              </button>
            </div>
          </div> */}

          {/* <div className="rounded-3xl border border-gray-200 dark:border-gray-800 bg-white dark:bg-gray-900 p-6">
            <h3 className="font-extrabold text-lg flex items-center gap-2">
              <RadioTower size={18} className="opacity-70" /> Status
            </h3>
            <p className="mt-4 text-sm opacity-70">
              Viewing: {isMe ? "your profile" : `@${profileUser.username}`}
            </p>
          </div> */}
        {/* </div> */}
      </div>
    </div>
  );
}
