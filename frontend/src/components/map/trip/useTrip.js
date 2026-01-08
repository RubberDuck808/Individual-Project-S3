import { useCallback, useMemo, useState } from "react";
import { completeTrip } from "../../../api/tripApi";
import { haversineMeters } from "../../../utils/geo";

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

  const onUserMove = useCallback((loc) => {
    setActiveTrip((t) => {
      if (!t) return t;
      if (loc?.lat == null || loc?.lng == null) return t;

      const [prevLng, prevLat] = t.lastCoord;
      const currentLng = loc.lng;
      const currentLat = loc.lat;

      const deltaM = haversineMeters(prevLat, prevLng, currentLat, currentLng);
      const deltaKm = deltaM / 1000;

      if (deltaM < 3) {
        return { ...t, lastCoord: [currentLng, currentLat] };
      }

      return {
        ...t,
        distanceKm: t.distanceKm + deltaKm,
        lastCoord: [currentLng, currentLat],
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
