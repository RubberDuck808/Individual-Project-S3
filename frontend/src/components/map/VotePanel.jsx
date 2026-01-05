import React from "react";
import PropTypes from "prop-types";
import { useEffect, useMemo, useState, useCallback } from "react";
import { submitVote, getVoteCounts } from "../../api/voteApi";

function haversineMeters(lat1, lng1, lat2, lng2) {
  const R = 6371000;
  const toRad = (d) => (d * Math.PI) / 180;
  const dLat = toRad(lat2 - lat1);
  const dLng = toRad(lng2 - lng1);

  const a =
    Math.sin(dLat / 2) ** 2 +
    Math.cos(toRad(lat1)) * Math.cos(toRad(lat2)) * Math.sin(dLng / 2) ** 2;

  return 2 * R * Math.asin(Math.sqrt(a));
}

export default function VotePanel({
  hazard,
  userLocation,
  // 🚗 bigger boundary for driving-away scenario
  allowedDistanceMeters = 200,
  expiresAt,
  onClose,
}) {
  const [votes, setVotes] = useState({ upvotes: 0, downvotes: 0 });
  const [loading, setLoading] = useState(false);

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

  const closePanel = useCallback(() => {
    onClose?.();
  }, [onClose]);

  useEffect(() => {
    const interval = setInterval(() => {
      const left = Math.max(0, Math.ceil((expiresAt - Date.now()) / 1000));
      setSecondsLeft(left);
      if (left <= 0) {
        clearInterval(interval);
        closePanel();
      }
    }, 250);

    return () => clearInterval(interval);
  }, [expiresAt, closePanel]);

  const withinAllowedBoundary =
    distanceMeters == null ? false : distanceMeters <= allowedDistanceMeters;

  const canVote = secondsLeft > 0 && withinAllowedBoundary;

  useEffect(() => {
    if (hazardId) loadVotes();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [hazardId]);

  const loadVotes = async () => {
    if (!hazardId) return;
    const data = await getVoteCounts(hazardId);
    setVotes(data);
  };

  const handleVote = async (type) => {
    const token = localStorage.getItem("token");
    if (!token) return alert("You must be logged in to vote");

    if (secondsLeft <= 0) return alert("Voting window expired.");
    if (!withinAllowedBoundary)
      return alert(`Too far to vote (>${allowedDistanceMeters}m).`);

    setLoading(true);
    try {
      await submitVote(hazardId, type);
      await loadVotes();

      // ✅ optional: close after vote (recommended)
      closePanel();
    } catch (err) {
      alert(err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed bottom-40 right-7 bg-white dark:bg-gray-800 p-4 rounded-xl shadow-xl w-64 z-50">
      <div className="flex justify-between items-center mb-1">
        <h3 className="font-bold text-lg">Vote</h3>
        <button onClick={closePanel} className="text-xl">✕</button>
      </div>

      <div className="text-sm mb-2 text-orange-500 font-semibold">
        Time left: {secondsLeft}s
      </div>

      <div className="text-sm mb-3 opacity-80">
        {distanceMeters == null ? (
          "Distance unknown…"
        ) : (
          <>
            Distance: <span className="font-semibold">{Math.round(distanceMeters)} m</span>
            {!withinAllowedBoundary && (
              <div className="text-red-500 mt-1">
                Too far (must be ≤ {allowedDistanceMeters}m).
              </div>
            )}
          </>
        )}
      </div>

      <div className="flex items-center justify-between">
        <button
          disabled={loading || !hazardId || !canVote}
          onClick={() => handleVote("UPVOTE")}
          className={`px-4 py-2 rounded-lg text-white ${
            !canVote ? "bg-gray-400" : "bg-green-600"
          }`}
        >
          Up {votes.upvotes}
        </button>

        <button
          disabled={loading || !hazardId || !canVote}
          onClick={() => handleVote("DOWNVOTE")}
          className={`px-4 py-2 rounded-lg text-white ${
            !canVote ? "bg-gray-400" : "bg-red-600"
          }`}
        >
          Down {votes.downvotes}
        </button>
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
