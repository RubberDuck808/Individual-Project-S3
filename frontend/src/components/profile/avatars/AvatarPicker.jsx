import React, { useEffect, useState } from "react";
import { fetchAvatars, changeMyAvatar } from "../../../api/avatarApi";

function AvatarTile({ avatar, selected, onClick }) {
  return (
    <button
      onClick={onClick}
      className={`w-full rounded-2xl border p-2 flex flex-col items-center gap-2 hover:opacity-90 transition ${
        selected ? "border-blue-600" : "border-gray-200 dark:border-gray-800"
      }`}
      type="button"
    >
      <div className="w-16 h-16 rounded-xl overflow-hidden bg-gray-200 dark:bg-gray-800 flex items-center justify-center">
        {avatar.url ? (
          <img src={avatar.url} alt={avatar.name} className="w-full h-full object-cover" />
        ) : (
          <div className="w-full h-full bg-gradient-to-br from-blue-600 to-indigo-600" />
        )}
      </div>
    </button>
  );
}

export default function AvatarPicker({ currentAvatarName, onUpdated }) {
  const [avatars, setAvatars] = useState([]);
  const [loading, setLoading] = useState(true);

  const [selected, setSelected] = useState(currentAvatarName || "");
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");

  // keep selected in sync if parent updates
  useEffect(() => {
    setSelected(currentAvatarName || "");
  }, [currentAvatarName]);

  useEffect(() => {
    let cancelled = false;

    (async () => {
      setLoading(true);
      setError("");
      try {
        const list = await fetchAvatars();
        if (!cancelled) setAvatars(list);
      } catch (e) {
        if (!cancelled) setError(e?.message || "Failed to load avatars");
      } finally {
        if (!cancelled) setLoading(false);
      }
    })();

    return () => {
      cancelled = true;
    };
  }, []);

  const handleSave = async () => {
    if (!selected) return;

    setSaving(true);
    setError("");

    try {
      const updatedUser = await changeMyAvatar(selected);
      localStorage.setItem("user", JSON.stringify(updatedUser));
      onUpdated?.(updatedUser);
    } catch (e) {
      setError(e?.message || "Failed to change avatar");
    } finally {
      setSaving(false);
    }
  };

  if (loading) return <div className="opacity-70">Loading avatars…</div>;

  return (
    <div className="w-full space-y-3">
      <div className="flex items-center justify-between">
        <h2 className="text-lg font-semibold">Profile picture</h2>

        <button
          onClick={handleSave}
          disabled={saving || !selected || selected === currentAvatarName}
          className={`px-4 py-2 rounded-xl font-bold ${
            saving || !selected || selected === currentAvatarName
              ? "opacity-60 cursor-not-allowed"
              : "hover:opacity-95"
          } bg-blue-600 text-white`}
          type="button"
        >
          {saving ? "Saving…" : "Save"}
        </button>
      </div>

      {error && <p className="text-sm text-red-400">{error}</p>}

      <div className="w-full grid grid-cols-3 sm:grid-cols-4 gap-3">
        {avatars
          .filter((a) => a.active)
          .map((a) => (
            <AvatarTile
              key={a.name}
              avatar={a}
              selected={selected === a.name}
              onClick={() => setSelected(a.name)}
            />
          ))}
      </div>
    </div>
  );
}
