import React, { useState } from "react";
import { updateCurrentUser } from "../../../api/userApi";

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

  return (
    <div className="w-full space-y-4">
      <h2 className="text-lg font-semibold mb-2">Account details</h2>

      {error && <p className="text-sm text-red-400">{error}</p>}
      {success && <p className="text-sm text-green-400">{success}</p>}

      <input
        placeholder="Name"
        value={form.name}
        onChange={onChange("name")}
        className="w-full p-2 rounded-lg border bg-transparent"
      />
      <input
        placeholder="Username"
        value={form.username}
        onChange={onChange("username")}
        className="w-full p-2 rounded-lg border bg-transparent"
      />
      <input
        placeholder="Email"
        value={form.email}
        onChange={onChange("email")}
        className="w-full p-2 rounded-lg border bg-transparent"
      />
      <input
        type="password"
        placeholder="Current password"
        value={form.currentPassword}
        onChange={onChange("currentPassword")}
        className="w-full p-2 rounded-lg border bg-transparent"
      />
      <input
        type="password"
        placeholder="New password"
        value={form.newPassword}
        onChange={onChange("newPassword")}
        className="w-full p-2 rounded-lg border bg-transparent"
      />

      <button
        onClick={handleSave}
        disabled={saving}
        className={`w-full py-2 rounded-lg font-semibold transition ${
          saving ? "opacity-60 cursor-not-allowed" : "hover:opacity-95"
        } bg-blue-600 text-white`}
      >
        {saving ? "Saving..." : "Save changes"}
      </button>
    </div>
  );
}
