import React from "react";
import PropTypes from "prop-types";
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

StatsGrid.propTypes = {
  stats: PropTypes.arrayOf(PropTypes.shape({
    key: PropTypes.string,
    label: PropTypes.string.isRequired,
    value: PropTypes.oneOfType([PropTypes.string, PropTypes.number]).isRequired,
    icon: PropTypes.elementType,
    loading: PropTypes.bool,
  })).isRequired,
};
