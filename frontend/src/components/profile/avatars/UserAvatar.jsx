import React from "react";

export default function UserAvatar({ user, size = 64, className = "" }) {
  const avatarUrl = user?.avatarUrl;
  const initials = (user?.name?.[0] || user?.username?.[0] || "U").toUpperCase();

  return (
    <div
      className={`rounded-2xl overflow-hidden flex items-center justify-center font-extrabold ${className}`}
      style={{ width: size, height: size }}
    >
      {avatarUrl ? (
        <img src={avatarUrl} alt="avatar" className="w-full h-full object-cover" />
      ) : (
        <div className="w-full h-full bg-gradient-to-br from-blue-600 to-indigo-600 text-white flex items-center justify-center">
          <span style={{ fontSize: Math.max(18, Math.floor(size / 2.5)) }}>{initials}</span>
        </div>
      )}
    </div>
  );
}
