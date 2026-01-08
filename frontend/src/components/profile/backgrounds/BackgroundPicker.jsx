import React, { useEffect, useState } from "react";
import { fetchBackgrounds, changeMyBackground } from "../../../api/backgroundApi";

function BackgroundTile({ bg, selected, onClick }) {
  return (
    <button
      onClick={onClick}
      className={`w-full rounded-2xl border p-2 flex flex-col items-center gap-2 hover:opacity-90 transition ${
        selected ? "border-blue-600" : "border-gray-200 dark:border-gray-800"
      }`}
      type="button"
    >
      <div className="w-full aspect-video rounded-xl overflow-hidden bg-gray-200 dark:bg-gray-800">
        {bg.url ? (
          <img src={bg.url} alt={bg.name} className="w-full h-full object-cover" />
        ) : (
          <div className="w-full h-full bg-gradient-to-br from-blue-600 to-indigo-600" />
        )}
      </div>
    </button>
  );
}

export default function BackgroundPicker({ currentBackgroundName, onUpdated }) {
  const [backgrounds, setBackgrounds] = useState([]);
  const [loading, setLoading] = useState(true);

  const [selected, setSelected] = useState(currentBackgroundName || "");
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    setSelected(currentBackgroundName || "");
  }, [currentBackgroundName]);

  useEffect(() => {
    let cancelled = false;

    (async () => {
      setLoading(true);
      setError("");
      try {
        const list = await fetchBackgrounds();
        if (!cancelled) setBackgrounds(list);
      } catch (e) {
        if (!cancelled) setError(e?.message || "Failed to load backgrounds");
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
      const updatedUser = await changeMyBackground(selected);
      localStorage.setItem("user", JSON.stringify(updatedUser));
      onUpdated?.(updatedUser);
    } catch (e) {
      setError(e?.message || "Failed to change background");
    } finally {
      setSaving(false);
    }
  };

  if (loading) return <div className="opacity-70">Loading backgrounds…</div>;

  return (
    <div className="w-full space-y-3">
      <div className="flex items-center justify-between">
        <h2 className="text-lg font-semibold">Profile background</h2>

        <button
          onClick={handleSave}
          disabled={saving || !selected || selected === currentBackgroundName}
          className={`px-4 py-2 rounded-xl font-bold ${
            saving || !selected || selected === currentBackgroundName
              ? "opacity-60 cursor-not-allowed"
              : "hover:opacity-95"
          } bg-blue-600 text-white`}
          type="button"
        >
          {saving ? "Saving…" : "Save"}
        </button>
      </div>

      {error && <p className="text-sm text-red-400">{error}</p>}

      <div className="w-full grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-3">
        {backgrounds
          .filter((b) => b.active)
          .map((b) => (
            <BackgroundTile
              key={b.name}
              bg={b}
              selected={selected === b.name}
              onClick={() => setSelected(b.name)}
            />
          ))}
      </div>
    </div>
  );
}
