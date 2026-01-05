import React from "react";
import StatCard from "./StatCard";

export default function StatsGrid({ stats }) {
  return (
    <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mt-8">
      {stats.map((s) => (
        <StatCard
          key={s.key || s.label}
          label={s.label}
          value={s.value}
          Icon={s.icon}
          loading={s.loading}
        />
      ))}
    </div>
  );
}
