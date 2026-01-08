import React, { useEffect, useMemo, useState } from "react";
import { fetchCurrentUser, fetchUserByUsername } from "../api/userApi";
import { useTheme } from "../context/ThemeContext";
import { useNavigate, useParams } from "react-router-dom";
import { Users, RadioTower } from "lucide-react";

import ProfileTabs from "../components/profile/tabs/ProfileTabs";
import FriendsTab from "../components/profile/tabs/FriendsTab";
import StatsGrid from "../components/profile/stats/StatsGrid";
import { useUserStats } from "../components/profile/stats/useUserStats";
import ProfileHeader from "../components/profile/ProfileHeader";

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

  return (
    <div className={`min-h-screen ${darkMode ? "text-white" : "text-gray-900"}`}>
      {/* FULL PAGE BACKGROUND */}
      <div className="relative min-h-screen">
        {bgUrl ? (
          <div className="absolute inset-0">
            <img
              src={bgUrl}
              alt="profile background"
              className="w-full h-full object-cover"
            />
          </div>
        ) : (
          <div
            className={`absolute inset-0 ${
              darkMode ? "bg-gray-950" : "bg-gray-50"
            }`}
          />
        )}

        {/* CONTENT */}
        <div className="relative z-10">
          <div className="max-w-6xl mx-auto px-4 py-12">
            {/* ONLY 10px side gap so background barely shows */}
            <div className="px-[10px]">
              <div className="rounded-3xl shadow-xl bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 p-10">
                <ProfileHeader profileUser={profileUser} isMe={isMe} />

                <StatsGrid stats={stats} />

                <div className="mt-10">
                  <ProfileTabs
                    tabs={tabs}
                    activeKey={activeTab}
                    onChange={setActiveTab}
                  />

                  {activeTab === "friends" && (
                    <FriendsTab
                      user={me}
                      profileUser={profileUser}
                      isMe={isMe}
                    />
                  )}
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
