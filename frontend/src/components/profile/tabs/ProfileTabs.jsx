import React from "react";
import PropTypes from "prop-types";

const cx = (...classes) => classes.filter(Boolean).join(" ");

export default function ProfileTabs({ tabs, activeKey, onChange }) {
  return (
    <div className="flex gap-3 flex-wrap p-2 bg-slate-100 border-[3px] border-black rounded-[2rem] w-fit shadow-[4px_4px_0px_0px_rgba(0,0,0,1)]">
      {tabs.map((t) => {
        const active = t.key === activeKey;
        return (
          <button
            key={t.key}
            onClick={() => onChange(t.key)}
            className={cx(
              "px-6 py-2 rounded-[1.5rem] text-xs font-[1000] uppercase tracking-widest transition-all border-[3px]",
              active
                ? "bg-black text-white border-black shadow-none translate-y-0.5" 
                : "bg-white text-black border-transparent hover:border-black hover:-translate-y-0.5"
            )}
            type="button"
          >
            <span className="inline-flex items-center gap-2">
              {t.icon && <t.icon size={14} strokeWidth={3} />}
              {t.label}
            </span>
          </button>
        );
      })}
    </div>
  );
}

ProfileTabs.propTypes = {
  tabs: PropTypes.arrayOf(PropTypes.shape({
    key: PropTypes.string.isRequired,
    label: PropTypes.string.isRequired,
    icon: PropTypes.elementType,
  })).isRequired,
  activeKey: PropTypes.string.isRequired,
  onChange: PropTypes.func.isRequired,
};