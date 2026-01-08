import React from "react";

export default function SettingsSidebar({ items, activeKey, onChange, onLogout }) {
  return (
    <div className="rounded-2xl border border-gray-200 dark:border-gray-800 bg-white dark:bg-gray-900 overflow-hidden flex flex-col h-[calc(100vh-170px)]">
      {/* Menu area (scrolls if needed) */}
      <div className="p-4 space-y-2 overflow-auto">
        {items.map((it) => {
          const Icon = it.icon;
          const active = it.key === activeKey;

          return (
            <button
              key={it.key}
              onClick={() => onChange(it.key)}
              className={`w-full flex items-center gap-3 px-4 py-4 rounded-xl font-bold transition ${
                active
                  ? "bg-blue-600 text-white"
                  : "bg-gray-50 dark:bg-gray-800/40 border border-gray-200 dark:border-gray-800 hover:bg-gray-100 dark:hover:bg-gray-800"
              }`}
              type="button"
            >
              {Icon && <Icon size={18} className={active ? "" : "opacity-70"} />}
              <span className="text-left">{it.label}</span>
            </button>
          );
        })}
      </div>

      {/* Logout pinned to bottom */}
      <div className="mt-auto border-t border-gray-200 dark:border-gray-800 p-4">
        <button
          onClick={onLogout}
          className="w-full bg-red-600 text-white py-3 rounded-xl font-bold hover:bg-red-700 transition"
          type="button"
        >
          Log out
        </button>
      </div>
    </div>
  );
}
