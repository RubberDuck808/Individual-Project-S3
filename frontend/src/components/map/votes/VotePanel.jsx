import React, { useEffect, useMemo, useState, useCallback } from "react";
import PropTypes from "prop-types";
import { submitVote, getVoteCounts } from "../../../api/voteApi";
import { haversineMeters } from "../../../utils/geo";

// Utility for joining classes
function cx(...classes) {
  return classes.filter(Boolean).join(" ");
}

// Updated TimerRing to match the chunky style
function TimerRing({ progress = 0 }) {
  const size = 34;
  const stroke = 4; // Chunkier stroke
  const r = (size - stroke) / 2;
  const c = 2 * Math.PI * r;
  const dash = c * (1 - Math.min(1, Math.max(0, progress)));

  return (
    <svg width={size} height={size} className="shrink-0">
      <circle
        cx={size / 2}
        cy={size / 2}
        r={r}
        strokeWidth={stroke}
        className="fill-none stroke-black/10"
      />
      <circle
        cx={size / 2}
        cy={size / 2}
        r={r}
        strokeWidth={stroke}
        strokeDasharray={c}
        strokeDashoffset={dash}
        strokeLinecap="round"
        className="fill-none stroke-black transition-[stroke-dashoffset] duration-200"
        transform={`rotate(-90 ${size / 2} ${size / 2})`}
      />
    </svg>
  );
}

export default function VotePanel({
  hazard,
  userLocation,
  allowedDistanceMeters = 200,
  expiresAt,
  onClose,
}) {
  const [votes, setVotes] = useState({ upvotes: 0, downvotes: 0 });
  const [loading, setLoading] = useState(false);
  const [toast, setToast] = useState(null);

  const hazardId = hazard?.id;

  const distanceMeters = useMemo(() => {
    if (!hazard || !userLocation) return null;
    if (hazard.latitude == null || hazard.longitude == null) return null;
    return haversineMeters(
      userLocation.lat,
      userLocation.lng,
      hazard.latitude,
      hazard.longitude
    );
  }, [hazard, userLocation]);

  const [secondsLeft, setSecondsLeft] = useState(() =>
    Math.max(0, Math.ceil((expiresAt - Date.now()) / 1000))
  );

  const totalSeconds = useMemo(() => {
    return Math.max(1, Math.ceil((expiresAt - (Date.now() - secondsLeft * 1000)) / 1000));
  }, []);

  const closePanel = useCallback(() => onClose?.(), [onClose]);

  useEffect(() => {
    const interval = setInterval(() => {
      const left = Math.max(0, Math.ceil((expiresAt - Date.now()) / 1000));
      setSecondsLeft(left);
      if (left <= 0) {
        clearInterval(interval);
        setToast({ type: "info", msg: "Window expired." });
        setTimeout(() => closePanel(), 800);
      }
    }, 250);
    return () => clearInterval(interval);
  }, [expiresAt, closePanel]);

  const withinAllowedBoundary = distanceMeters == null ? false : distanceMeters <= allowedDistanceMeters;
  const canVote = secondsLeft > 0 && withinAllowedBoundary && !!hazardId && !loading;
  const progress = useMemo(() => secondsLeft / Math.max(1, totalSeconds), [secondsLeft, totalSeconds]);

  const loadVotes = useCallback(async () => {
    if (!hazardId) return;
    try {
      const data = await getVoteCounts(hazardId);
      setVotes(data);
    } catch (err) { console.error(err); }
  }, [hazardId]);

  useEffect(() => { if (hazardId) loadVotes(); }, [hazardId, loadVotes]);

  const showToast = (t) => {
    setToast(t);
    window.clearTimeout(showToast._t);
    showToast._t = window.setTimeout(() => setToast(null), 2400);
  };

  const handleVote = async (type) => {
    const token = localStorage.getItem("token");
    if (!token) return showToast({ type: "error", msg: "Log in first!" });
    if (!canVote) return;

    setLoading(true);
    try {
      await submitVote(hazardId, type);
      await loadVotes();
      showToast({ type: "success", msg: "Vote Cast!" });
      setTimeout(() => closePanel(), 800);
    } catch (err) {
      showToast({ type: "error", msg: "Vote failed." });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed bottom-36 right-4 z-[100] w-[310px] animate-in slide-in-from-right-10">
      <div className="relative overflow-hidden rounded-[2.5rem] border-[4px] border-black bg-[#FFFDF5] p-6 shadow-[10px_10px_0px_0px_rgba(0,0,0,1)]">
        
        {/* Top Header */}
        <div className="flex items-start justify-between gap-2">
          <div className="flex flex-col gap-2">
            <div className="inline-flex items-center gap-2 rounded-xl bg-white border-[3px] border-black px-3 py-1 shadow-[3px_3px_0px_0px_rgba(0,0,0,1)]">
              <TimerRing progress={progress} />
              <span className="font-[1000] tabular-nums text-sm">{secondsLeft}s</span>
            </div>
            <h3 className="text-2xl font-[1000] italic uppercase tracking-tighter text-black leading-none mt-1">
              Still there?
            </h3>
          </div>

          <button
            onClick={closePanel}
            className="w-10 h-10 border-[3px] border-black rounded-xl flex items-center justify-center font-black hover:bg-black hover:text-white transition-colors shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] active:shadow-none active:translate-x-1 active:translate-y-1 bg-white"
          >
            ✕
          </button>
        </div>

        {/* Distance Status Card */}
        <div className="mt-6 space-y-3">
          <div className={cx(
            "p-3 rounded-2xl border-[3px] border-black font-black text-xs uppercase text-center shadow-[4px_4px_0px_0px_rgba(0,0,0,1)]",
            withinAllowedBoundary ? "bg-[#FFD600] text-black" : "bg-slate-100 text-slate-400"
          )}>
            {withinAllowedBoundary 
              ? `📍 ${Math.round(distanceMeters)}m Away (In Range)` 
              : `🛑 Too Far (${Math.round(distanceMeters)}m)`}
          </div>

          {/* Chunky Progress Bar */}
          <div className="h-4 w-full bg-white border-[3px] border-black rounded-full p-0.5 overflow-hidden">
            <div
              className="h-full rounded-full bg-[#00D1FF] transition-[width] duration-200 border-r-2 border-black"
              style={{ width: `${Math.round(progress * 100)}%` }}
            />
          </div>
        </div>

        {/* Toast / Message Area */}
        {toast && (
          <div className={cx(
            "mt-4 p-3 rounded-xl border-[3px] border-black font-black text-xs text-center shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] animate-bounce",
            toast.type === "success" ? "bg-emerald-400 text-black" : "bg-[#FF6AC1] text-white"
          )}>
            {toast.msg}
          </div>
        )}

        {/* Action Buttons */}
        <div className="mt-6 grid grid-cols-2 gap-4">
          <button
            disabled={!canVote}
            onClick={() => handleVote("UPVOTE")}
            className={cx(
              "flex flex-col items-center justify-center gap-1 rounded-2xl border-[4px] border-black py-3 px-2 font-[1000] uppercase transition-all",
              canVote
                ? "bg-[#00D1FF] text-black shadow-[4px_4px_0px_0px_#0044AA] hover:translate-y-0.5 hover:shadow-[2px_2px_0px_0px_#0044AA] active:translate-y-1 active:shadow-none"
                : "bg-slate-100 text-slate-300 opacity-60"
            )}
          >
            <span className="text-xl">▲</span>
            <span className="text-[10px] tracking-widest">Keep</span>
            <div className="mt-1 px-2 bg-white border-2 border-black rounded-lg text-[10px]">
              {votes.upvotes}
            </div>
          </button>

          <button
            disabled={!canVote}
            onClick={() => handleVote("DOWNVOTE")}
            className={cx(
              "flex flex-col items-center justify-center gap-1 rounded-2xl border-[4px] border-black py-3 px-2 font-[1000] uppercase transition-all",
              canVote
                ? "bg-[#FF6AC1] text-white shadow-[4px_4px_0px_0px_#A32E6F] hover:translate-y-0.5 hover:shadow-[2px_2px_0px_0px_#A32E6F] active:translate-y-1 active:shadow-none"
                : "bg-slate-100 text-slate-300 opacity-60"
            )}
          >
            <span className="text-xl">▼</span>
            <span className="text-[10px] tracking-widest">Gone</span>
            <div className="mt-1 px-2 bg-white border-2 border-black rounded-lg text-[10px] text-black">
              {votes.downvotes}
            </div>
          </button>
        </div>

        <div className="mt-4 text-center">
          <span className="text-[9px] font-black text-slate-400 uppercase tracking-widest">
            Community Verification Mode
          </span>
        </div>
      </div>
    </div>
  );
}

VotePanel.propTypes = {
  hazard: PropTypes.object.isRequired,
  userLocation: PropTypes.shape({
    lat: PropTypes.number,
    lng: PropTypes.number,
  }),
  allowedDistanceMeters: PropTypes.number,
  expiresAt: PropTypes.number.isRequired,
  onClose: PropTypes.func.isRequired,
};