import { useCallback, useMemo, useState } from "react";
import { completeTrip } from "../../api/tripApi";

// Haversine distance in KM
function haversineKm([lng1, lat1], [lng2, lat2]) {
  const R = 6371;
  const dLat = ((lat2 - lat1) * Math.PI) / 180;
  const dLng = ((lng2 - lng1) * Math.PI) / 180;

  const a =
    Math.sin(dLat / 2) ** 2 +
    Math.cos((lat1 * Math.PI) / 180) *
      Math.cos((lat2 * Math.PI) / 180) *
      Math.sin(dLng / 2) ** 2;

  return 2 * R * Math.asin(Math.sqrt(a));
}

export function useTrip() {
  const [activeTrip, setActiveTrip] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  const isActive = !!activeTrip;

  const startTrip = useCallback(({ startLat, startLng, endLat, endLng }) => {
    setActiveTrip({
      startLat,
      startLng,
      endLat,
      endLng,
      startedAt: new Date().toISOString(),
      distanceKm: 0,
      lastCoord: [startLng, startLat],
    });
  }, []);

  // Call this on every GPS update
  const onUserMove = useCallback((loc) => {
    setActiveTrip((t) => {
      if (!t) return t;

      const prev = t.lastCoord;
      const current = [loc.lng, loc.lat];

      const deltaKm = haversineKm(prev, current);

      // Ignore tiny jitter (< ~3m)
      if (deltaKm < 0.003) {
        return { ...t, lastCoord: current };
      }

      return {
        ...t,
        distanceKm: t.distanceKm + deltaKm,
        lastCoord: current,
      };
    });
  }, []);

  const endTrip = useCallback(
    async ({ currentLat, currentLng }) => {
      if (!activeTrip) return null;

      const payload = {
        startLat: activeTrip.startLat,
        startLng: activeTrip.startLng,
        endLat: currentLat,
        endLng: currentLng,
        distanceKm: Number(activeTrip.distanceKm.toFixed(3)),
        startedAt: activeTrip.startedAt,
        endedAt: new Date().toISOString(),
      };

      setSubmitting(true);
      try {
        const result = await completeTrip(payload);
        setActiveTrip(null);
        return result;
      } finally {
        setSubmitting(false);
      }
    },
    [activeTrip]
  );

  const cancelTrip = useCallback(() => {
    setActiveTrip(null);
  }, []);

  const display = useMemo(() => {
    if (!activeTrip) return null;
    return {
      distanceKm: activeTrip.distanceKm,
      startedAt: activeTrip.startedAt,
      endLat: activeTrip.endLat,
      endLng: activeTrip.endLng,
    };
  }, [activeTrip]);

  return {
    isActive,
    activeTrip,
    display,
    submitting,
    startTrip,
    onUserMove,
    endTrip,
    cancelTrip,
  };
}
