import React from "react";
import PropTypes from "prop-types";
import { Settings } from "lucide-react";
import { useNavigate } from "react-router-dom";
import UserAvatar from "./avatars/UserAvatar";

export default function ProfileHeader({ profileUser, isMe }) {
  const navigate = useNavigate();

  return (
    <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-6">
      <div className="flex items-center gap-5">
        <UserAvatar user={profileUser} size={64} />

        <div>
          <h1 className="text-2xl md:text-3xl font-extrabold">
            {profileUser.name || profileUser.username}
          </h1>
          <p className="opacity-70 font-semibold">@{profileUser.username}</p>

          {isMe && profileUser.email && (
            <p className="opacity-60 text-sm">{profileUser.email}</p>
          )}
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
  );
}

ProfileHeader.propTypes = {
  profileUser: PropTypes.shape({
    name: PropTypes.string,
    username: PropTypes.string.isRequired,
    email: PropTypes.string,
  }).isRequired,
  isMe: PropTypes.bool.isRequired,
};
