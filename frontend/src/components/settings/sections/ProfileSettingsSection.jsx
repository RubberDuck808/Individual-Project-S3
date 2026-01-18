import React from "react";
import PropTypes from "prop-types";
import AvatarPicker from "../../profile/avatars/AvatarPicker";
import UserAvatar from "../../profile/avatars/UserAvatar";
import BackgroundPicker from "../../profile/backgrounds/BackgroundPicker";

export default function ProfileSettingsSection({ me, setMe }) {
  return (
    <div className="w-full space-y-8">
      {/* AVATAR */}
      <div>
        <div className="flex items-center gap-4 mb-4">
          <UserAvatar user={me} size={56} />
          <div>
            <div className="font-extrabold">Profile picture</div>
            <div className="text-sm opacity-70">Choose a preset avatar</div>
          </div>
        </div>

        <AvatarPicker
          currentAvatarName={me?.avatarName || ""}
          onUpdated={(updatedUser) => {
            setMe(updatedUser);
            localStorage.setItem("user", JSON.stringify(updatedUser));
          }}
        />
      </div>

      {/* Background */}
      <div>
        <div className="mb-3">
          <div className="font-extrabold">Profile background</div>
          <div className="text-sm opacity-70">Choose a preset background</div>
        </div>

        <BackgroundPicker
          currentBackgroundName={me?.backgroundName || ""}
          onUpdated={(updatedUser) => {
            setMe(updatedUser);
            localStorage.setItem("user", JSON.stringify(updatedUser));
          }}
        />
      </div>
    </div>
  );
}

ProfileSettingsSection.propTypes = {
  me: PropTypes.shape({
    avatarName: PropTypes.string,
    backgroundName: PropTypes.string,
  }),
  setMe: PropTypes.func.isRequired,
};
