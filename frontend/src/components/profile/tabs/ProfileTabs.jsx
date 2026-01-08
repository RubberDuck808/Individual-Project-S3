import React from "react";

export default function ProfileTabs({ tabs, activeKey, onChange }) {
  return (
    <div className="flex gap-2 flex-wrap border-b border-gray-200 dark:border-gray-800">
      {tabs.map((t) => {
        const active = t.key === activeKey;
        return (
          <button
            key={t.key}
            onClick={() => onChange(t.key)}
            className={[
              "px-4 py-2 rounded-t-xl text-sm font-bold transition-colors",
              active
                ? "bg-gray-100 dark:bg-gray-800 text-gray-900 dark:text-white"
                : "text-gray-500 dark:text-gray-400 hover:bg-gray-50 dark:hover:bg-gray-900",
            ].join(" ")}
          >
            <span className="inline-flex items-center gap-2">
              {t.icon && <t.icon size={16} />}
              {t.label}
            </span>
          </button>
        );
      })}
    </div>
  );
}
