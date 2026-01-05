import React from "react";

export default function StatCard({ label, value, Icon, loading = false }) {
  return (
    <div className="rounded-2xl border border-gray-200 dark:border-gray-800 bg-gray-50 dark:bg-gray-800/30 p-4">
      <div className="flex items-center justify-between">
        <p className="text-sm font-bold opacity-70">{label}</p>
        {Icon ? <Icon size={18} className="opacity-60" /> : null}
      </div>

      <p className="text-2xl font-extrabold mt-2">
        {loading ? "…" : value}
      </p>
    </div>
  );
}
