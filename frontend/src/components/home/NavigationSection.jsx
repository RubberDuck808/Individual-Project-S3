import React from "react";

export default function NavigationSection() {
  const hazards = [
    { label: "Pothole", icon: "🕳️", eta: "0.8 mi", color: "bg-[#FF6AC1]" },
    { label: "Accident", icon: "🚧", eta: "1.6 mi", color: "bg-[#FFD600]" },
    { label: "Debris", icon: "🪵", eta: "2.1 mi", color: "bg-[#00D1FF]" },
  ];

  return (
    <section id="nav-hazards" className="max-w-7xl mx-auto px-6 py-24 relative">
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-16 items-center">
        {/* Copy */}
        <div className="lg:col-span-5">
          <div className="inline-flex items-center gap-2 rounded-2xl bg-emerald-400 border-[3px] border-black px-4 py-2 text-xs font-[1000] uppercase tracking-widest text-black shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] mb-8">
            <span className="h-2 w-2 rounded-full bg-black animate-pulse" />
            Live Navigation
          </div>

          <h2 className="text-5xl md:text-6xl font-[1000] leading-[0.95] text-slate-900 uppercase italic">
            Routes that <br /><span className="text-[#0066FF] drop-shadow-[2px_2px_0px_#000]">talk back.</span>
          </h2>

          <p className="mt-8 text-xl text-slate-600 font-bold leading-relaxed">
            See what’s coming before you get there. Tap to report and keep your drive smooth as butter.
          </p>

          <div className="mt-10 grid grid-cols-1 sm:grid-cols-2 gap-4">
            {["Smart reroutes", "One-tap reports"].map((title) => (
              <div key={title} className="rounded-2xl border-[4px] border-black bg-white p-5 shadow-[6px_6px_0px_0px_#00D1FF]">
                <div className="font-[1000] text-black uppercase text-xs tracking-widest">{title}</div>
                <div className="text-[10px] font-black text-[#0066FF] uppercase mt-1">System Active</div>
              </div>
            ))}
          </div>
        </div>

        {/* Mock UI Device */}
        <div className="lg:col-span-7">
          <div className="rounded-[3.5rem] border-[6px] border-black bg-white shadow-[16px_16px_0px_0px_#FFD600] overflow-hidden">
            <div className="p-6 bg-black text-white flex items-center justify-between">
              <div className="font-black uppercase tracking-tighter italic flex items-center gap-2">
                <span className="w-3 h-3 bg-red-500 rounded-full animate-ping" />
                Live Drive
              </div>
              <div className="text-[10px] font-black bg-[#FF6AC1] px-4 py-1 rounded-full uppercase border-2 border-white">Downtown → Home</div>
            </div>

            <div className="p-8 space-y-6">
              <div className="rounded-[2rem] bg-[#FFFDF5] border-[4px] border-black p-6 flex items-center justify-between shadow-[6px_6px_0px_0px_rgba(0,0,0,1)]">
                <div>
                  <div className="text-[10px] font-[1000] uppercase text-slate-400 mb-1">Next Turn</div>
                  <div className="text-3xl font-[1000] text-black tracking-tighter italic">Right on Elm St</div>
                </div>
                <div className="w-16 h-16 bg-[#FFD600] border-4 border-black rounded-2xl flex items-center justify-center text-3xl shadow-[4px_4px_0px_0px_rgba(0,0,0,1)]">➡️</div>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                {hazards.map((h) => (
                  <div key={h.label} className="rounded-3xl border-[4px] border-black bg-white p-5 hover:-translate-y-1 transition-transform cursor-pointer">
                    <div className="flex items-center justify-between mb-4">
                      <div className={`w-12 h-12 ${h.color} border-[3px] border-black rounded-xl flex items-center justify-center text-2xl shadow-[4px_4px_0px_0px_rgba(0,0,0,1)]`}>{h.icon}</div>
                      <div className="text-[10px] font-black text-slate-400">{h.eta}</div>
                    </div>
                    <div className="font-[1000] text-black uppercase text-xs tracking-widest">{h.label}</div>
                  </div>
                ))}
              </div>

              <button className="w-full py-6 bg-[#FF6AC1] text-white rounded-3xl border-[4px] border-black font-[1000] uppercase tracking-widest text-xl shadow-[8px_8px_0px_0px_rgba(0,0,0,1)] hover:shadow-none hover:translate-x-2 hover:translate-y-2 transition-all">
                Report Hazard!
              </button>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}