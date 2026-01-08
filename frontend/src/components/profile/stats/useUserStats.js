import { useEffect, useMemo, useState } from "react";
import { Route, AlertTriangle, ThumbsUp, Navigation } from "lucide-react";
import { getUserStats } from "../../../api/statisticsApi";

export function useUserStats(username) {
  const [dto, setDto] = useState(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    let cancelled = false;

    setDto(null);

    (async () => {
      if (!username) return;

      setLoading(true);
      try {
        const res = await getUserStats(username);
        if (!cancelled) setDto(res);
      } catch (e) {
        console.error("Failed to load user stats", e);
        if (!cancelled) setDto(null);
      } finally {
        if (!cancelled) setLoading(false);
      }
    })();

    return () => {
      cancelled = true;
    };
  }, [username]);

  return useMemo(() => {
    const safe = dto ?? {};

    const totalTrips = Number(safe.totalTrips ?? 0);
    const totalDistanceKm = Number(safe.totalDistanceKm ?? 0);
    const totalHazardsReported = Number(safe.totalHazardsReported ?? 0);
    const totalVotes = Number(safe.totalVotes ?? 0);

    return {
      loading,
      stats: [
        {
          key: "trips",
          label: "Trips",
          value: totalTrips,
          icon: Route,
          loading,
        },
        {
          key: "distance",
          label: "Distance (km)",
          value: Math.round(totalDistanceKm),
          icon: Navigation,
          loading,
        },
        {
          key: "hazards",
          label: "Hazards Reported",
          value: totalHazardsReported,
          icon: AlertTriangle,
          loading,
        },
        {
          key: "votes",
          label: "Votes Cast",
          value: totalVotes,
          icon: ThumbsUp,
          loading,
        },
      ],
      raw: dto,
    };
  }, [dto, loading]);
}
