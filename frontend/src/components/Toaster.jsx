import React from "react";
import { useToastState } from "../context/ToastContext";

const STYLES = {
  info:    "bg-blue-600 text-white",
  success: "bg-green-600 text-white",
  warn:    "bg-yellow-500 text-white",
  error:   "bg-red-600 text-white",
};

export default function Toaster() {
  const { toasts, remove } = useToastState();

  if (toasts.length === 0) return null;

  return (
    <div className="fixed bottom-6 right-6 z-[9999] flex flex-col gap-2 max-w-sm">
      {toasts.map((t) => (
        <div
          key={t.id}
          className={`flex items-start gap-3 px-4 py-3 rounded-xl shadow-lg text-sm font-medium ${STYLES[t.type] ?? STYLES.info}`}
        >
          <span className="flex-1">{t.message}</span>
          <button
            onClick={() => remove(t.id)}
            className="opacity-70 hover:opacity-100 ml-2 leading-none"
            aria-label="Dismiss"
          >
            ✕
          </button>
        </div>
      ))}
    </div>
  );
}
