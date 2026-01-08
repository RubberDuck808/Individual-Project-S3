import React from "react";

export default function StatCard({ label, value, Icon, loading = false }) {
  return (
    <div className="rounded-[2rem] border-[3px] border-black bg-white p-5 shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] flex flex-col justify-between min-h-[110px]">
      <div className="flex items-center justify-between">
        <p className="text-[10px] font-[1000] uppercase tracking-widest text-slate-400">{label}</p>
        {Icon ? <Icon size={20} className="text-black" strokeWidth={3} /> : null}
      </div>

      <p className="text-3xl font-[1000] mt-2 italic tracking-tighter text-black">
        {loading ? "..." : value}
      </p>
    </div>
  );
}