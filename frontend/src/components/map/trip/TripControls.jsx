import React from "react";

// Standard helper for joining CSS classes
function cx(...classes) {
  return classes.filter(Boolean).join(" ");
}

export default function TripControls({ 
  isActive, 
  distanceKm, 
  submitting, 
  onStart, 
  onEnd, 
  onCancel, 
  canStart 
}) {
  if (!isActive) {
    return (
      <button
        disabled={!canStart}
        onClick={onStart}
        className={cx(
          "w-full mt-4 py-5 rounded-[2rem] border-[4px] border-black font-[1000] uppercase tracking-widest transition-all",
          canStart 
            ? "bg-[#00D1FF] text-black shadow-[6px_6px_0px_0px_#0044AA] hover:translate-y-1 hover:shadow-[2px_2px_0px_0px_#0044AA] active:translate-y-2 active:shadow-none" 
            : "bg-slate-200 text-slate-400 cursor-not-allowed opacity-50"
        )}
      >
        {canStart ? "Start Mission →" : "Engine Locked"}
      </button>
    );
  }

  return (
    <div className="mt-4 space-y-4">
      <div className="bg-white border-[3px] border-black rounded-2xl p-4 flex items-center justify-between shadow-[4px_4px_0px_0px_rgba(0,0,0,1)]">
        <div className="text-[10px] font-black uppercase text-slate-400">Tracked Distance</div>
        <div className="text-xl font-[1000] text-black">
          {Number.isFinite(distanceKm) ? distanceKm.toFixed(2) : "0.00"}{" "}
          <span className="text-xs italic uppercase">KM</span>
        </div>
      </div>

      <div className="grid grid-cols-2 gap-4">
        <button
          disabled={submitting}
          onClick={onEnd}
          className="py-4 bg-[#FF6AC1] text-white border-[4px] border-black rounded-2xl font-black uppercase shadow-[4px_4px_0px_0px_#A32E6F] active:shadow-none active:translate-y-1 transition-all"
        >
          {submitting ? "..." : "Finish"}
        </button>
        <button
          onClick={onCancel}
          className="py-4 bg-white text-black border-[4px] border-black rounded-2xl font-black uppercase shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] active:shadow-none active:translate-y-1 transition-all"
        >
          Abort
        </button>
      </div>
    </div>
  );
}