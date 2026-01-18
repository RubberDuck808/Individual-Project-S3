import React, { useEffect, useState, useCallback, useRef } from "react";
import PropTypes from "prop-types";
import { getCategoriesCached, createHazard } from "../../../api/hazardApi";

function cx(...classes) {
  return classes.filter(Boolean).join(" ");
}

export default function HazardFormPanel({ coords, onClose }) {
  const [categories, setCategories] = useState([]);
  const [submitting, setSubmitting] = useState(false);
  const [toast, setToast] = useState(null);
  const toastTimeoutRef = useRef(null);

  const locating = !coords || coords.loading || coords.lat == null || coords.lng == null;

  const showToast = useCallback((t) => {
    setToast(t);
    if (toastTimeoutRef.current) {
      clearTimeout(toastTimeoutRef.current);
    }
    toastTimeoutRef.current = globalThis.setTimeout(() => {
      setToast(null);
      toastTimeoutRef.current = null;
    }, 2400);
  }, []);

  useEffect(() => {
    getCategoriesCached()
      .then(setCategories)
      .catch((err) => {
        showToast({ type: "error", msg: "Failed to load hazard types." });
      });
    
    return () => {
      if (toastTimeoutRef.current) {
        clearTimeout(toastTimeoutRef.current);
      }
    };
  }, [showToast]);

  const handleSelect = async (categoryId) => {
    if (locating) return showToast({ type: "info", msg: "Scanning for location..." });
    const token = localStorage.getItem("token");
    if (!token) return showToast({ type: "error", msg: "Log in to report, driver!" });

    setSubmitting(true);
    try {
      await createHazard({ latitude: coords.lat, longitude: coords.lng, categoryId });
      showToast({ type: "success", msg: "Hazard logged in the system!" });
      setTimeout(() => onClose?.(), 800);
    } catch (error) {
      showToast({ type: "error", msg: error?.message || "Transmission failed." });
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="fixed inset-0 z-[100] flex items-end sm:items-center justify-center p-4">
      {/* Backdrop */}
      <button
        type="button"
        onClick={onClose}
        className="absolute inset-0 bg-black/40 backdrop-blur-[2px]"
        aria-label="Close form"
      />

      <div className="relative w-full max-w-lg bg-[#FFFDF5] border-[4px] border-black rounded-[2.5rem] shadow-[12px_12px_0px_0px_rgba(0,0,0,1)] overflow-hidden animate-in slide-in-from-bottom-10 duration-300">
        
        {/* Header Section */}
        <div className="p-6 border-b-[4px] border-black bg-white">
          <div className="flex justify-between items-start">
            <div>
              <h2 className="text-3xl font-[1000] italic uppercase tracking-tighter text-black">
                Drop a <span className="text-[#FF6AC1]">Marker</span>
              </h2>
              <div className="mt-2 flex items-center gap-2">
                <span className={cx(
                  "px-3 py-1 rounded-full border-2 border-black text-[10px] font-black uppercase shadow-[2px_2px_0px_0px_rgba(0,0,0,1)]",
                  locating ? "bg-[#FFD600] animate-pulse" : "bg-[#00D1FF]"
                )}>
                  {locating ? "🛰️ Pinpointing..." : "📍 Ready to report"}
                </span>
              </div>
            </div>
            <button onClick={onClose} className="w-10 h-10 border-4 border-black rounded-2xl flex items-center justify-center font-black hover:bg-black hover:text-white transition-colors shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] active:shadow-none active:translate-x-1 active:translate-y-1">
              ✕
            </button>
          </div>
        </div>

        {/* Categories Grid */}
        <div className="p-6 max-h-[60vh] overflow-y-auto">
          {toast && (
            <div className={cx(
              "mb-6 p-4 rounded-2xl border-4 border-black font-black text-center shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] animate-bounce",
              toast.type === "success" ? "bg-emerald-400 text-black" : "bg-[#FF6AC1] text-white"
            )}>
              {toast.msg}
            </div>
          )}

          <div className="grid grid-cols-2 gap-4">
            {categories.map((c) => (
              <button
                key={c.id}
                disabled={submitting || locating}
                onClick={() => handleSelect(c.id)}
                className="group relative bg-white border-[4px] border-black rounded-3xl p-4 flex flex-col items-center gap-3 transition-all shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] hover:shadow-[6px_6px_0px_0px_#00D1FF] hover:-translate-y-1 active:shadow-none active:translate-y-1 active:translate-x-1"
              >
                <div className="h-14 w-14 bg-[#F1F5F9] border-2 border-black rounded-2xl flex items-center justify-center text-3xl group-hover:rotate-12 transition-transform shadow-[2px_2px_0px_0px_rgba(0,0,0,1)]">
                  {c.iconUrl ? <img src={c.iconUrl} alt="" className="h-10 w-10 object-contain" /> : "⚠️"}
                </div>
                <span className="font-[1000] uppercase text-xs tracking-tight text-black">{c.name}</span>
              </button>
            ))}
          </div>
        </div>

        {/* Footer */}
        <div className="p-4 bg-black text-center">
          <p className="text-[10px] font-black text-white/50 uppercase tracking-[0.2em]">
            Public Transmission // ID: {coords?.lat?.toFixed(3)}
          </p>
        </div>
      </div>
    </div>
  );
}

HazardFormPanel.propTypes = {
  coords: PropTypes.shape({
    lat: PropTypes.number,
    lng: PropTypes.number,
    loading: PropTypes.bool,
  }),
  onClose: PropTypes.func.isRequired,
};