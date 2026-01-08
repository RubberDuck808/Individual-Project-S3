import React, { useEffect, useMemo, useState, useCallback } from "react";
import { useTheme } from "../../../context/ThemeContext";
import { getCategoriesCached, createHazard } from "../../../api/hazardApi";

function cx(...classes) {
  return classes.filter(Boolean).join(" ");
}

export default function HazardFormPanel({ coords, onClose }) {
  const [categories, setCategories] = useState([]);
  const [submitting, setSubmitting] = useState(false);
  const [toast, setToast] = useState(null);
  const { darkMode } = useTheme();

  const locating =
    !coords || coords.loading || coords.lat == null || coords.lng == null;

  // Load categories
  useEffect(() => {
    getCategoriesCached()
      .then(setCategories)
      .catch((err) => {
        console.error("Failed to load hazard categories:", err);
        setToast({ type: "error", msg: "Failed to load hazard types." });
      });
  }, []);

  const showToast = useCallback((t) => {
    setToast(t);
    window.clearTimeout(showToast._t);
    showToast._t = window.setTimeout(() => setToast(null), 2400);
  }, []);

  const disabled = submitting || locating;

  const headerStatus = useMemo(() => {
    if (submitting) return { label: "Submitting…", kind: "info" };
    if (locating) return { label: "Getting location…", kind: "info" };
    return { label: "Choose a hazard type", kind: "neutral" };
  }, [submitting, locating]);

  const handleSelect = async (categoryId) => {
    if (locating) {
      showToast({ type: "info", msg: "Still determining your location…" });
      return;
    }

    const token = localStorage.getItem("token");
    if (!token) {
      showToast({ type: "error", msg: "You must be logged in to report." });
      return;
    }

    setSubmitting(true);
    try {
      await createHazard({
        latitude: coords.lat,
        longitude: coords.lng,
        categoryId,
      });

      showToast({ type: "success", msg: "Hazard reported." });
      setTimeout(() => onClose?.(), 450);
    } catch (error) {
      showToast({ type: "error", msg: error?.message || "Report failed." });
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50">
      {/* Backdrop */}
      <button
        aria-label="Close"
        onClick={onClose}
        className="absolute inset-0 bg-black/60 backdrop-blur-sm"
      />

      {/* Bottom sheet */}
      <div className="absolute inset-x-0 bottom-0">
        <div
          className={cx(
            "relative overflow-hidden rounded-t-3xl border border-black/10 shadow-2xl backdrop-blur-xl",
            darkMode ? "bg-gray-900/80 text-white" : "bg-white/80 text-gray-900"
          )}
        >
          {/* subtle gradient glow like VotePanel */}
          <div className="pointer-events-none absolute -top-20 -right-20 h-56 w-56 rounded-full bg-[#2F88FF]/20 blur-3xl" />
          <div className="pointer-events-none absolute -bottom-24 -left-20 h-56 w-56 rounded-full bg-emerald-500/15 blur-3xl" />

          {/* Header */}
          <div className="relative px-5 pt-5 pb-3">
            <div className="flex items-start justify-between gap-3">
              <div className="min-w-0">
                <div className="flex items-center gap-2">
                  <span
                    className={cx(
                      "inline-flex items-center rounded-full px-2.5 py-1 text-xs font-semibold",
                      headerStatus.kind === "neutral" &&
                        "bg-black/5 text-black/70 dark:bg-white/10 dark:text-white/80",
                      headerStatus.kind === "info" &&
                        "bg-[#2F88FF]/10 text-[#2F88FF] dark:text-[#7fb6ff]"
                    )}
                  >
                    {headerStatus.label}
                  </span>

                  {coords?.lat != null && coords?.lng != null && (
                    <span className="text-xs text-black/50 dark:text-white/50 tabular-nums">
                      {coords.lat.toFixed(5)}, {coords.lng.toFixed(5)}
                    </span>
                  )}
                </div>

                <h2 className="mt-3 text-xl font-semibold tracking-tight">
                  Report hazard
                </h2>

                <p className="mt-1 text-sm text-gray-600 dark:text-gray-300">
                  Tap a category to drop a report at your current location.
                </p>
              </div>

              <button
                onClick={onClose}
                className="rounded-full p-2 text-gray-500 transition hover:bg-black/5 hover:text-gray-900 dark:text-gray-300 dark:hover:bg-white/10 dark:hover:text-white"
                aria-label="Close"
              >
                ✕
              </button>
            </div>

            {/* Toast */}
            {toast && (
              <div
                className={cx(
                  "mt-4 rounded-xl px-3 py-2 text-sm",
                  toast.type === "success" &&
                    "border border-emerald-500/20 bg-emerald-500/10 text-emerald-800 dark:text-emerald-200",
                  toast.type === "error" &&
                    "border border-red-500/20 bg-red-500/10 text-red-800 dark:text-red-200",
                  toast.type === "info" &&
                    "border border-[#2F88FF]/20 bg-[#2F88FF]/10 text-[#2F88FF] dark:text-[#7fb6ff]"
                )}
              >
                {toast.msg}
              </div>
            )}
          </div>

          {/* Grid */}
          <div className="relative px-5 pb-6">
            <div className="grid grid-cols-2 gap-4">
              {categories.map((c) => (
                <button
                  key={c.id}
                  disabled={disabled}
                  onClick={() => handleSelect(c.id)}
                  className={cx(
                    "group rounded-2xl border p-4 text-left transition",
                    "active:scale-[0.99]",
                    disabled
                      ? "opacity-60 cursor-not-allowed"
                      : "hover:bg-black/5 dark:hover:bg-white/10",
                    "border-black/10 dark:border-white/10"
                  )}
                >
                  <div className="flex items-center gap-3">
                    <div
                      className={cx(
                        "h-12 w-12 rounded-2xl flex items-center justify-center",
                        "border border-black/10 dark:border-white/10",
                        "bg-black/5 dark:bg-white/10"
                      )}
                    >
                      {c.iconUrl ? (
                        <img
                          src={c.iconUrl}
                          alt={c.name}
                          className="h-8 w-8 object-contain"
                        />
                      ) : (
                        <span className="font-bold">{c.name?.[0] ?? "?"}</span>
                      )}
                    </div>

                    <div className="min-w-0">
                      <div className="font-semibold truncate">{c.name}</div>
                      <div className="text-xs opacity-70">
                        Report this hazard type
                      </div>
                    </div>
                  </div>
                </button>
              ))}

              {categories.length === 0 && (
                <div className="col-span-2 rounded-2xl border border-black/10 dark:border-white/10 bg-black/5 dark:bg-white/10 p-4 text-sm opacity-80">
                  Loading hazard types…
                </div>
              )}
            </div>

            {/* Footer hint */}
            <div className="mt-4 text-center text-[11px] text-gray-500 dark:text-gray-400">
              Reports are public and may be verified by nearby users.
            </div>

            <div className="h-2" />
          </div>
        </div>
      </div>
    </div>
  );
}
