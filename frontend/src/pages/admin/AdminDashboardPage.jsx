import React, { useEffect, useState } from "react";
import { getAdminStatistics } from "../../api/adminApi";
import { Users, AlertTriangle, Cpu, MapPin, Image, TrendingUp } from "lucide-react";

export default function AdminDashboardPage() {
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchStats = async () => {
      try {
        setLoading(true);
        setError(null);
        console.log("Fetching admin statistics...");
        const data = await getAdminStatistics();
        console.log("Admin statistics loaded:", data);
        setStats(data);
      } catch (err) {
        console.error("Failed to load admin statistics:", err);
        console.error("Error details:", {
          message: err.message,
          stack: err.stack,
          name: err.name
        });
        setError(err.message || "Failed to load statistics");
      } finally {
        setLoading(false);
      }
    };

    fetchStats();
  }, []);

  if (loading) {
    return <div className="text-center py-12">Loading statistics...</div>;
  }

  if (error) {
    return (
      <div className="space-y-6">
        <div>
          <h1 className="font-black text-3xl mb-2">Admin Dashboard</h1>
          <p className="text-gray-600">System overview and statistics</p>
        </div>
        <div className="bg-red-100 border-4 border-red-500 rounded-2xl p-6 shadow-[8px_8px_0px_0px_rgba(0,0,0,1)]">
          <h2 className="font-black text-xl mb-2 text-red-700">Error Loading Statistics</h2>
          <p className="text-red-600 font-bold">{error}</p>
          <p className="text-sm text-red-500 mt-2">
            Please check your browser console for more details.
          </p>
        </div>
      </div>
    );
  }

  if (!stats) {
    return <div className="text-center py-12">No data available</div>;
  }

  const statCards = [
    {
      title: "Total Users",
      value: stats.totalUsers || 0,
      icon: Users,
      color: "bg-blue-500",
      change: `${stats.activeUsers || 0} active`,
    },
    {
      title: "Total Hazards",
      value: stats.totalHazards || 0,
      icon: AlertTriangle,
      color: "bg-orange-500",
      change: `${stats.openHazards || 0} open`,
    },
    {
      title: "Total Devices",
      value: stats.totalDevices || 0,
      icon: Cpu,
      color: "bg-purple-500",
      change: `${stats.activeDevices || 0} active`,
    },
    {
      title: "Total Trips",
      value: stats.totalTrips || 0,
      icon: MapPin,
      color: "bg-green-500",
      change: `${(stats.totalDistanceKm || 0).toFixed(1)} km`,
    },
    {
      title: "Avatars",
      value: stats.totalAvatars || 0,
      icon: Image,
      color: "bg-pink-500",
      change: `${stats.activeAvatars || 0} active`,
    },
    {
      title: "Telemetry Records",
      value: stats.totalTelemetryRecords || 0,
      icon: TrendingUp,
      color: "bg-cyan-500",
      change: `${stats.devicesWithTelemetry || 0} devices`,
    },
  ];

  return (
    <div className="space-y-6">
      <div>
        <h1 className="font-black text-3xl mb-2">Admin Dashboard</h1>
        <p className="text-gray-600">System overview and statistics</p>
      </div>

      {/* Statistics Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        {statCards.map((card) => {
          const Icon = card.icon;
          return (
            <div
              key={card.title}
              className="bg-white border-2 border-black rounded-2xl p-6 shadow-[8px_8px_0px_0px_rgba(0,0,0,1)]"
            >
              <div className="flex items-center justify-between mb-4">
                <div className={`${card.color} p-3 rounded-lg`}>
                  <Icon className="text-white" size={24} />
                </div>
                <div className="text-right">
                  <div className="text-3xl font-black">{card.value}</div>
                  <div className="text-sm text-gray-600">{card.change}</div>
                </div>
              </div>
              <h3 className="font-bold text-lg">{card.title}</h3>
            </div>
          );
        })}
      </div>

      {/* Detailed Statistics */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Hazard Breakdown */}
        <div className="bg-white border-2 border-black rounded-2xl p-6 shadow-[8px_8px_0px_0px_rgba(0,0,0,1)]">
          <h2 className="font-black text-xl mb-4">Hazard Status</h2>
          <div className="space-y-3">
            <div className="flex justify-between items-center">
              <span className="font-bold">Open</span>
              <span className="text-2xl font-black">{stats.openHazards || 0}</span>
            </div>
            <div className="flex justify-between items-center">
              <span className="font-bold">Verified</span>
              <span className="text-2xl font-black">{stats.verifiedHazards || 0}</span>
            </div>
            <div className="flex justify-between items-center">
              <span className="font-bold">Resolved</span>
              <span className="text-2xl font-black">{stats.resolvedHazards || 0}</span>
            </div>
          </div>
        </div>

        {/* Device Status */}
        <div className="bg-white border-2 border-black rounded-2xl p-6 shadow-[8px_8px_0px_0px_rgba(0,0,0,1)]">
          <h2 className="font-black text-xl mb-4">Device Status</h2>
          <div className="space-y-3">
            <div className="flex justify-between items-center">
              <span className="font-bold">Active Devices</span>
              <span className="text-2xl font-black text-green-600">{stats.activeDevices || 0}</span>
            </div>
            <div className="flex justify-between items-center">
              <span className="font-bold">Inactive Devices</span>
              <span className="text-2xl font-black text-red-600">{stats.inactiveDevices || 0}</span>
            </div>
            <div className="flex justify-between items-center">
              <span className="font-bold">With Telemetry</span>
              <span className="text-2xl font-black">{stats.devicesWithTelemetry || 0}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
