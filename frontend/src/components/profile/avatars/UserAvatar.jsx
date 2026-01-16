import React from "react";
import PropTypes from "prop-types";

export default function UserAvatar({ user, size = 64, className = "" }) {
  const avatarUrl = user?.avatarUrl;
  const initials = (user?.name?.[0] || user?.username?.[0] || "U").toUpperCase();

  return (
    <div
      className={`relative overflow-hidden border-[3px] border-black shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] bg-white ${className}`}
      style={{ 
        width: size, 
        height: size,
        borderRadius: size > 40 ? '1.5rem' : '0.75rem' // Adjust roundness based on size
      }}
    >
      {avatarUrl ? (
        <img src={avatarUrl} alt="avatar" className="w-full h-full object-cover" />
      ) : (
        <div className="w-full h-full bg-[#00D1FF] text-black flex items-center justify-center font-[1000]">
          <span style={{ fontSize: Math.max(14, Math.floor(size / 2.5)) }}>
            {initials}
          </span>
        </div>
      )}
    </div>
  );
}

UserAvatar.propTypes = {
  user: PropTypes.shape({
    avatarUrl: PropTypes.string,
    name: PropTypes.string,
    username: PropTypes.string,
  }),
  size: PropTypes.number,
  className: PropTypes.string,
};