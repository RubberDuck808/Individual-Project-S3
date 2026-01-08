import React from "react";

export default function TelemetrySection() {
  const metrics = [
    { label: "Velocity", value: "62", unit: "mph", color: "bg-[#00D1FF]", shadow: "shadow-[#00D1FF]" },
    { label: "Fuel Level", value: "58", unit: "%", color: "bg-[#FF6AC1]", shadow: "shadow-[#FF6AC1]" },
    { label: "Oil Temp", value: "196", unit: "°F", color: "bg-[#FFD600]", shadow: "shadow-[#FFD600]" },
  ];

  return (
    <section id="telemetry" className="max-w-7xl mx-auto px-6 py-24">
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-16 items-center">
        <div className="lg:col-span-5">
          <div className="inline-flex items-center gap-2 rounded-2xl bg-[#00D1FF] border-[3px] border-black px-5 py-2 text-xs font-[1000] uppercase tracking-widest text-black shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] mb-8">
            🏎️ Pro Mode Active
          </div>
          <h2 className="text-5xl md:text-6xl font-[1000] leading-[0.95] text-slate-900 uppercase italic">
            Your car, <br/><span className="text-[#FF6AC1] drop-shadow-[2px_2px_0px_#000]">unlocked.</span>
          </h2>
          <p className="mt-8 text-xl text-slate-600 font-bold leading-relaxed">
            Plug in and peek under the hood. Live engine stats and trip history delivered in a style that actually looks good.
          </p>
        </div>

        <div className="lg:col-span-7">
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-8">
            {metrics.map((m) => (
              <div key={m.label} className="p-8 rounded-[3rem] bg-white border-[4px] border-black shadow-[8px_8px_0px_0px_rgba(0,0,0,1)] flex flex-col items-center hover:scale-105 transition-transform">
                <div className="text-[10px] font-[1000] uppercase tracking-widest text-slate-400 mb-4">{m.label}</div>
                <div className="text-5xl font-[1000] text-black italic tracking-tighter mb-1">{m.value}</div>
                <div className="text-xs font-black text-slate-400 uppercase mb-8">{m.unit}</div>
                
                {/* Chunky Progress Bar */}
                <div className="h-6 w-full bg-slate-100 border-[3px] border-black rounded-full overflow-hidden shadow-[inset_0_2px_4px_rgba(0,0,0,0.1)]">
                  <div className={`h-full ${m.color} border-r-[3px] border-black rounded-full`} style={{ width: '60%' }} />
                </div>
              </div>
            ))}
          </div>

          <div className="mt-10 p-10 rounded-[3rem] bg-[#0066FF] border-[5px] border-black text-white flex items-center justify-between overflow-hidden relative shadow-[12px_12px_0px_0px_rgba(0,0,0,1)]">
            <div className="relative z-10">
              <div className="text-xs font-[1000] uppercase tracking-[0.2em] mb-3 text-blue-200">System Diagnostics</div>
              <div className="text-3xl font-[1000] uppercase italic leading-none">All Systems <br/>Purring! 🐯</div>
            </div>
            <div className="text-8xl absolute -right-4 -bottom-4 opacity-30 rotate-12 select-none font-black italic">FAST</div>
          </div>
        </div>
      </div>
    </section>
  );
}