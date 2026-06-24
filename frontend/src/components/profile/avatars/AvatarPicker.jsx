import React, { useEffect, useState } from "react";
import PropTypes from "prop-types";
import { changeMyAvatar } from "../../../api/avatarApi";
import { useAssetsCache } from "../../../context/AssetsCacheContext";

function cx(...classes) {
  return classes.filter(Boolean).join(" ");
}

function SelectionTile({ url, name, selected, onClick }) {
  return (
    <button
      onClick={onClick}
      className={cx(
        "w-full rounded-[1.5rem] border-[3px] border-black p-1.5 transition-all",
        selected
          ? "bg-black translate-y-1 shadow-none"
          : "bg-white shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] hover:-translate-y-0.5 active:translate-y-1 active:shadow-none"
      )}
      type="button"
    >
      <div className="w-full aspect-square rounded-xl overflow-hidden bg-slate-100 flex items-center justify-center border-2 border-black/5">
        {url ? (
          <img 
            src={url} 
            alt={name} 
            className="w-full h-full object-cover" 
            loading="lazy"
            decoding="async"
          />
        ) : (
          <div className="w-full h-full bg-[#00D1FF]" />
        )}
      </div>
    </button>
  );
}

export default function AvatarPicker({ currentAvatarName, onUpdated }) {
  const { avatars, loading: cacheLoading, error: cacheError } = useAssetsCache();
  const [selected, setSelected] = useState(currentAvatarName || "");
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    setSelected(currentAvatarName || "");
  }, [currentAvatarName]);

  const handleSave = async () => {
    if (!selected) return;
    setSaving(true);
    setError("");
    try {
      const updatedUser = await changeMyAvatar(selected);
      sessionStorage.setItem("user", JSON.stringify(updatedUser));
      onUpdated?.(updatedUser);
    } catch (e) {
      setError(e?.message || "Failed to change avatar");
    } finally {
      setSaving(false);
    }
  };

  if (cacheLoading) return <div className="font-black animate-pulse uppercase tracking-widest text-slate-400">Loading characters...</div>;
  if (cacheError) return <div className="font-black uppercase tracking-widest text-red-500">Error: {cacheError}</div>;
  if (!avatars) return <div className="font-black uppercase tracking-widest text-slate-400">No avatars available</div>;

  const hasChanged = selected !== currentAvatarName;
  const activeAvatars = avatars.filter((a) => a.active);

  return (
    <div className="w-full space-y-6">
      <div className="flex items-center justify-between">
        <h2 className="text-xl font-[1000] uppercase tracking-tighter italic">Select Identity</h2>

        <button
          onClick={handleSave}
          disabled={saving || !selected || !hasChanged}
          className={cx(
            "px-6 py-2 rounded-xl font-[1000] uppercase text-xs tracking-widest border-[3px] border-black transition-all",
            saving || !selected || !hasChanged
              ? "bg-slate-100 text-slate-400 border-slate-300 cursor-not-allowed"
              : "bg-[#FFD600] text-black shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] hover:translate-y-0.5 hover:shadow-none"
          )}
          type="button"
        >
          {saving ? "Syncing..." : "Save Selection"}
        </button>
      </div>

      {error && (
        <div className="p-3 bg-red-100 border-2 border-red-500 rounded-xl text-red-600 text-xs font-black uppercase">
          {error}
        </div>
      )}

      <div className="w-full grid grid-cols-3 sm:grid-cols-4 gap-4">
        {activeAvatars.map((a) => (
          <SelectionTile
            key={a.name}
            url={a.url}
            name={a.name}
            selected={selected === a.name}
            onClick={() => setSelected(a.name)}
          />
        ))}
      </div>
    </div>
  );
}

AvatarPicker.propTypes = {
  currentAvatarName: PropTypes.string,
  onUpdated: PropTypes.func,
};

SelectionTile.propTypes = {
  url: PropTypes.string,
  name: PropTypes.string.isRequired,
  selected: PropTypes.bool.isRequired,
  onClick: PropTypes.func.isRequired,
};