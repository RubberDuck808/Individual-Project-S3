import React from "react";
import PropTypes from "prop-types";

export default function SettingsSidebar({ items, activeKey, onChange, onLogout }) {
  return (
    <div className="flex flex-col gap-4">
      <div className="bg-white border-[3px] border-black rounded-[2.5rem] overflow-hidden shadow-[8px_8px_0px_0px_rgba(0,0,0,1)]">
        <div className="p-4 space-y-3">
          {items.map((it) => {
            const Icon = it.icon;
            const active = it.key === activeKey;

            return (
              <button
                key={it.key}
                onClick={() => onChange(it.key)}
                className={`w-full flex items-center gap-4 px-5 py-4 rounded-2xl font-[1000] uppercase text-xs tracking-widest transition-all border-[3px] ${
                  active
                    ? "bg-black text-white border-black"
                    : "bg-white text-black border-transparent hover:border-black hover:translate-x-1"
                }`}
                type="button"
              >
                {Icon && <Icon size={20} strokeWidth={3} />}
                <span>{it.label}</span>
              </button>
            );
          })}
        </div>
      </div>

      <button
        onClick={onLogout}
        className="w-full bg-[#FF4545] text-white py-5 rounded-[2rem] font-[1000] uppercase tracking-widest text-xs border-[3px] border-black shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] hover:translate-y-1 hover:shadow-none transition-all"
        type="button"
      >
        Danger: Log out
      </button>
    </div>
  );
}

SettingsSidebar.propTypes = {
  items: PropTypes.arrayOf(PropTypes.shape({
    key: PropTypes.string.isRequired,
    label: PropTypes.string.isRequired,
    icon: PropTypes.elementType,
  })).isRequired,
  activeKey: PropTypes.string.isRequired,
  onChange: PropTypes.func.isRequired,
  onLogout: PropTypes.func.isRequired,
};