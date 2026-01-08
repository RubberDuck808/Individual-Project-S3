import React, { useEffect, useMemo, useState, useCallback } from "react";
import PropTypes from "prop-types";
import { submitVote, getVoteCounts } from "../../../api/voteApi";
import { haversineMeters } from "../../../utils/geo";

function cx(...classes) {
  return classes.filter(Boolean).join(" ");
}

function TimerRing({ progress = 0 }) {
  const size = 34;
  const stroke = 3;
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
        className="fill-none stroke-black/10 dark:stroke-white/15"
      />
      <circle
        cx={size / 2}
        cy={size / 2}
        r={r}
        strokeWidth={stroke}
        strokeDasharray={c}
        strokeDashoffset={dash}
        strokeLinecap="round"
        className="fill-none stroke-[#2F88FF] transition-[stroke-dashoffset] duration-200"
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
        setToast({ type: "info", msg: "Voting window expired." });
        setTimeout(() => closePanel(), 500);
      }
    }, 250);

    return () => clearInterval(interval);
  }, [expiresAt, closePanel]);

  const withinAllowedBoundary =
    distanceMeters == null ? false : distanceMeters <= allowedDistanceMeters;

  const canVote = secondsLeft > 0 && withinAllowedBoundary && !!hazardId && !loading;

  const progress = useMemo(() => {
    const p = secondsLeft / Math.max(1, totalSeconds);
    return Math.min(1, Math.max(0, p));
  }, [secondsLeft, totalSeconds]);

  const loadVotes = useCallback(async () => {
    if (!hazardId) return;
    const data = await getVoteCounts(hazardId);
    setVotes(data);
  }, [hazardId]);

  useEffect(() => {
    if (hazardId) loadVotes();
  }, [hazardId, loadVotes]);

  const showToast = (t) => {
    setToast(t);
    window.clearTimeout(showToast._t);
    showToast._t = window.setTimeout(() => setToast(null), 2400);
  };

  const handleVote = async (type) => {
    const token = localStorage.getItem("token");
    if (!token) return showToast({ type: "error", msg: "Log in to vote." });
    if (secondsLeft <= 0) return showToast({ type: "error", msg: "Voting window expired." });
    if (!withinAllowedBoundary)
      return showToast({
        type: "error",
        msg: `Too far to vote (must be ≤ ${allowedDistanceMeters}m).`,
      });

    setLoading(true);
    try {
      await submitVote(hazardId, type);
      await loadVotes();
      showToast({ type: "success", msg: "Vote submitted." });
      setTimeout(() => closePanel(), 450);
    } catch (err) {
      showToast({ type: "error", msg: err?.message || "Vote failed." });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed bottom-40 right-7 z-50 w-[290px]">
      <div className="relative overflow-hidden rounded-2xl border border-black/10 bg-white/80 p-4 shadow-2xl backdrop-blur-xl dark:border-white/10 dark:bg-gray-900/70">
        {/* subtle gradient glow */}
        <div className="pointer-events-none absolute -top-16 -right-16 h-40 w-40 rounded-full bg-[#2F88FF]/20 blur-3xl" />
        <div className="pointer-events-none absolute -bottom-16 -left-16 h-40 w-40 rounded-full bg-emerald-500/15 blur-3xl" />

        {/* Header */}
        <div className="flex items-start justify-between gap-3">
          <div className="min-w-0">
            <div className="flex items-center gap-2">
              <div className="inline-flex items-center gap-2 rounded-full bg-black/5 px-2.5 py-1 text-xs font-semibold text-black/70 dark:bg-white/10 dark:text-white/80">
                <TimerRing progress={progress} />
                <span className="tabular-nums">{secondsLeft}s</span>
              </div>
              <span className="text-xs text-black/50 dark:text-white/50">
                {withinAllowedBoundary ? "Within range" : "Out of range"}
              </span>
            </div>

            <h3 className="mt-2 text-lg font-semibold tracking-tight text-gray-900 dark:text-white">
              Confirm report?
            </h3>

            <p className="mt-1 text-sm text-gray-600 dark:text-gray-300">
              Upvote if it’s real, downvote if it’s gone.
            </p>
          </div>

          <button
            onClick={closePanel}
            className="rounded-full p-2 text-gray-500 transition hover:bg-black/5 hover:text-gray-900 dark:text-gray-300 dark:hover:bg-white/10 dark:hover:text-white"
            aria-label="Close"
          >
            ✕
          </button>
        </div>

        {/* Distance + progress bar */}
        <div className="mt-3 space-y-2">
          <div className="flex items-center justify-between text-xs">
            <span className="text-gray-500 dark:text-gray-400">
              {distanceMeters == null ? "Distance unknown" : "Distance"}
            </span>
            <span className="font-semibold tabular-nums text-gray-900 dark:text-white">
              {distanceMeters == null ? "—" : `${Math.round(distanceMeters)} m`}
            </span>
          </div>

          <div className="h-2 w-full overflow-hidden rounded-full bg-black/5 dark:bg-white/10">
            <div
              className="h-full rounded-full bg-[#2F88FF] transition-[width] duration-200"
              style={{ width: `${Math.round(progress * 100)}%` }}
            />
          </div>

          {!withinAllowedBoundary && (
            <div className="rounded-xl border border-red-500/20 bg-red-500/10 px-3 py-2 text-xs text-red-700 dark:text-red-300">
              Too far to vote. Move within <b>{allowedDistanceMeters}m</b>.
            </div>
          )}
        </div>

        {/* Toast */}
        {toast && (
          <div
            className={cx(
              "mt-3 rounded-xl px-3 py-2 text-sm",
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

        {/* Actions */}
        <div className="mt-4 grid grid-cols-2 gap-3">
          <button
            disabled={!canVote}
            onClick={() => handleVote("UPVOTE")}
            className={cx(
              "group relative flex items-center justify-center gap-2 rounded-xl px-4 py-2.5 text-sm font-semibold transition",
              canVote
                ? "bg-emerald-600 text-white hover:bg-emerald-600/90 active:scale-[0.99]"
                : "bg-gray-200 text-gray-500 dark:bg-white/10 dark:text-white/40"
            )}
          >
            <span className="text-base">▲</span>
            <span>Up</span>
            <span className="ml-auto rounded-full bg-white/15 px-2 py-0.5 text-xs tabular-nums">
              {votes.upvotes}
            </span>
          </button>

          <button
            disabled={!canVote}
            onClick={() => handleVote("DOWNVOTE")}
            className={cx(
              "group relative flex items-center justify-center gap-2 rounded-xl px-4 py-2.5 text-sm font-semibold transition",
              canVote
                ? "bg-rose-600 text-white hover:bg-rose-600/90 active:scale-[0.99]"
                : "bg-gray-200 text-gray-500 dark:bg-white/10 dark:text-white/40"
            )}
          >
            <span className="text-base">▼</span>
            <span>Down</span>
            <span className="ml-auto rounded-full bg-white/15 px-2 py-0.5 text-xs tabular-nums">
              {votes.downvotes}
            </span>
          </button>
        </div>

        {/* Footer hint */}
        <div className="mt-3 text-center text-[11px] text-gray-500 dark:text-gray-400">
          Votes close automatically when time runs out.
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

function TimerRingPropTypes() {}
TimerRingPropTypes.propTypes = {
  progress: PropTypes.number,
};
