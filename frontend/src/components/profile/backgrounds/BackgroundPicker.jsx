import React, { useEffect, useState } from "react";
import { fetchBackgrounds, changeMyBackground } from "../../../api/backgroundApi";

function cx(...classes) {
  return classes.filter(Boolean).join(" ");
}

function SelectionTile({ url, name, selected, onClick }) {
  return (
    <button
      onClick={onClick}
      className={cx(
        "w-full rounded-[1.5rem] border-[3px] border-black p-1.5 transition-all overflow-hidden",
        selected
          ? "bg-black translate-y-1 shadow-none"
          : "bg-white shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] hover:-translate-y-0.5 active:translate-y-1 active:shadow-none"
      )}
      type="button"
    >
      <div className="w-full aspect-video rounded-xl overflow-hidden bg-slate-100 flex items-center justify-center border-2 border-black/5">
        {url ? (
          <img src={url} alt={name} className="w-full h-full object-cover" />
        ) : (
          <div className="w-full h-full bg-[#FF6AC1]" />
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
    return () => { cancelled = true; };
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

  if (loading) return (
    <div className="font-black animate-pulse uppercase tracking-widest text-slate-400">
      Fetching Scenes...
    </div>
  );

  const hasChanged = selected !== currentBackgroundName;

  return (
    <div className="w-full space-y-6">
      <div className="flex items-center justify-between">
        <h2 className="text-xl font-[1000] uppercase tracking-tighter italic">Scene Selection</h2>

        <button
          onClick={handleSave}
          disabled={saving || !selected || !hasChanged}
          className={cx(
            "px-6 py-2 rounded-xl font-[1000] uppercase text-xs tracking-widest border-[3px] border-black transition-all",
            saving || !selected || !hasChanged
              ? "bg-slate-100 text-slate-400 border-slate-300 cursor-not-allowed"
              : "bg-[#FF6AC1] text-white shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] hover:translate-y-0.5 hover:shadow-none"
          )}
          type="button"
        >
          {saving ? "Updating..." : "Apply Scene"}
        </button>
      </div>

      {error && (
        <div className="p-3 bg-red-100 border-2 border-red-500 rounded-xl text-red-600 text-xs font-black uppercase">
          {error}
        </div>
      )}

      <div className="w-full grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-4">
        {backgrounds
          .filter((b) => b.active)
          .map((b) => (
            <SelectionTile
              key={b.name}
              url={b.url}
              name={b.name}
              selected={selected === b.name}
              onClick={() => setSelected(b.name)}
            />
          ))}
      </div>
    </div>
  );
}