import React, { useState } from "react";
import PropTypes from "prop-types";
import { updateCurrentUser } from "../../../api/userApi";

const cx = (...classes) => classes.filter(Boolean).join(" ");

export default function AccountSettingsSection({ me, setMe, onUsernameChanged }) {
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  const [form, setForm] = useState({
    name: me?.name || "",
    username: me?.username || "",
    email: me?.email || "",
    currentPassword: "",
    newPassword: "",
  });

  const onChange = (key) => (e) => {
    setForm((prev) => ({ ...prev, [key]: e.target.value }));
  };

  const handleSave = async () => {
    setError("");
    setSuccess("");
    setSaving(true);

    try {
      const payload = {
        name: form.name,
        username: form.username,
        email: form.email,
        currentPassword: form.currentPassword || undefined,
        newPassword: form.newPassword || undefined,
      };

      const updatedUser = await updateCurrentUser(payload);
      localStorage.setItem("user", JSON.stringify(updatedUser));

      setMe(updatedUser);
      setSuccess("Account updated successfully.");
      setForm((f) => ({ ...f, currentPassword: "", newPassword: "" }));

      if (updatedUser.username && updatedUser.username !== me?.username) {
        onUsernameChanged?.(updatedUser.username);
      }
    } catch (e) {
      setError(e?.message || "Failed to update account");
    } finally {
      setSaving(false);
    }
  };

  // Reusable Input Style for Neo-Brutalism
  const inputClass = "w-full p-4 rounded-2xl border-[3px] border-black bg-slate-50 font-bold focus:bg-white focus:outline-none transition-all focus:-translate-y-0.5 shadow-[2px_2px_0px_0px_rgba(0,0,0,1)] focus:shadow-[4px_4px_0px_0px_rgba(0,0,0,1)]";
  const labelClass = "text-[10px] font-black uppercase ml-2 mb-1 text-slate-500 tracking-widest";

  return (
    <div className="w-full space-y-6">
      <h2 className="text-xl font-[1000] uppercase italic tracking-tighter mb-2">Account Credentials</h2>

      {error && (
        <div className="p-3 bg-red-100 border-2 border-red-500 rounded-xl text-red-600 text-xs font-black uppercase">
          {error}
        </div>
      )}
      
      {success && (
        <div className="p-3 bg-green-100 border-2 border-green-500 rounded-xl text-green-600 text-xs font-black uppercase">
          {success}
        </div>
      )}

      <div className="space-y-4">
        {/* Name Field */}
        <div className="flex flex-col">
          <label htmlFor="display-name" className={labelClass}>Display Name</label>
          <input
            id="display-name"
            placeholder="e.g. John Doe"
            value={form.name}
            onChange={onChange("name")}
            className={inputClass}
          />
        </div>

        {/* Username Field */}
        <div className="flex flex-col">
          <label htmlFor="username-handle" className={labelClass}>Username Handle</label>
          <input
            id="username-handle"
            placeholder="username"
            value={form.username}
            onChange={onChange("username")}
            className={inputClass}
          />
        </div>

        {/* Email Field */}
        <div className="flex flex-col">
          <label htmlFor="email-address" className={labelClass}>Email Address</label>
          <input
            id="email-address"
            placeholder="email@example.com"
            value={form.email}
            onChange={onChange("email")}
            className={inputClass}
          />
        </div>

        <hr className="border-t-[3px] border-black/5 my-2" />

        {/* Current Password Field */}
        <div className="flex flex-col">
          <label htmlFor="current-password" className={labelClass}>Current Password</label>
          <input
            id="current-password"
            type="password"
            placeholder="••••••••"
            value={form.currentPassword}
            onChange={onChange("currentPassword")}
            className={inputClass}
          />
        </div>

        {/* New Password Field */}
        <div className="flex flex-col">
          <label htmlFor="new-password" className={labelClass}>New Password (Optional)</label>
          <input
            id="new-password"
            type="password"
            placeholder="••••••••"
            value={form.newPassword}
            onChange={onChange("newPassword")}
            className={inputClass}
          />
        </div>
      </div>

      <button
        onClick={handleSave}
        disabled={saving}
        className={cx(
          "w-full py-4 rounded-2xl font-[1000] uppercase tracking-widest text-sm border-[3px] border-black transition-all",
          saving 
            ? "bg-slate-200 text-slate-400 cursor-not-allowed border-slate-300" 
            : "bg-[#00D1FF] text-black shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] hover:translate-y-0.5 hover:shadow-none"
        )}
      >
        {saving ? "Syncing..." : "Commit Changes"}
      </button>
    </div>
  );
}

AccountSettingsSection.propTypes = {
  me: PropTypes.shape({
    name: PropTypes.string,
    username: PropTypes.string,
    email: PropTypes.string,
  }),
  setMe: PropTypes.func.isRequired,
  onUsernameChanged: PropTypes.func,
};