import React from "react";

export default function NavHazardSection() {
  const hazards = [
    { label: "Pothole", icon: "🕳️", eta: "0.8 mi" },
    { label: "Accident", icon: "🚧", eta: "1.6 mi" },
    { label: "Debris", icon: "🪵", eta: "2.1 mi" },
  ];

  return (
    <section id="nav-hazards" className="max-w-7xl mx-auto px-6 py-20">
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-10 items-center">
        {/* Copy */}
        <div className="lg:col-span-5">
          <div className="inline-flex items-center gap-2 rounded-full border border-black/10 bg-white/70 px-4 py-2 text-sm">
            <span className="h-2 w-2 rounded-full bg-emerald-500" />
            Navigation + Hazard Reporting
          </div>

          <h2 className="mt-5 text-4xl font-bold leading-tight">
            Routes that adapt — with hazard alerts from drivers ahead.
          </h2>

          <p className="mt-4 text-lg text-gray-600 leading-relaxed">
            Tripwire blends intelligent routing with instant hazard reporting.
            See what’s coming, report in one tap, and help everyone drive safer.
          </p>

          <div className="mt-8 grid grid-cols-1 sm:grid-cols-2 gap-4">
            {[
              {
                title: "Smart reroutes",
                desc: "Avoid hazards and slowdowns automatically.",
              },
              {
                title: "One-tap reports",
                desc: "Potholes, accidents, closures, debris.",
              },
              {
                title: "Confidence scoring",
                desc: "Reports get validated by the crowd.",
              },
              {
                title: "Live ETA impact",
                desc: "Know how hazards affect your route.",
              },
            ].map((f) => (
              <div
                key={f.title}
                className="rounded-3xl border border-black/10 bg-white/70 backdrop-blur-xl p-6"
              >
                <div className="font-semibold">{f.title}</div>
                <div className="mt-2 text-sm text-gray-600">{f.desc}</div>
              </div>
            ))}
          </div>
        </div>

        {/* Mock UI */}
        <div className="lg:col-span-7">
          <div className="rounded-3xl border border-black/10 bg-white/70 backdrop-blur-xl overflow-hidden">
            <div className="p-6 border-b border-black/10 flex items-center justify-between">
              <div className="font-semibold">Live Drive View</div>
              <div className="text-sm text-gray-600">Downtown → Home</div>
            </div>

            <div className="p-6">
              <div className="rounded-2xl border border-black/10 bg-white p-5">
                <div className="flex items-center justify-between">
                  <div>
                    <div className="text-sm text-gray-600">Next turn</div>
                    <div className="text-xl font-semibold">Right on Elm St</div>
                  </div>
                  <div className="text-right">
                    <div className="text-sm text-gray-600">ETA</div>
                    <div className="text-xl font-semibold">18 min</div>
                  </div>
                </div>

                <div className="mt-5 grid grid-cols-1 md:grid-cols-3 gap-3">
                  {hazards.map((h) => (
                    <div
                      key={h.label}
                      className="rounded-2xl border border-black/10 bg-gray-50 p-4"
                    >
                      <div className="flex items-center justify-between">
                        <div className="text-lg">{h.icon}</div>
                        <div className="text-xs text-gray-500">{h.eta}</div>
                      </div>
                      <div className="mt-2 font-semibold">{h.label}</div>
                      <div className="mt-1 text-xs text-gray-600">
                        Community confirmed
                      </div>
                    </div>
                  ))}
                </div>

                <div className="mt-5 flex flex-wrap gap-3">
                  <button className="px-4 py-2 rounded-xl bg-gray-900 text-white text-sm font-semibold hover:bg-black transition">
                    Report hazard
                  </button>
                  <button className="px-4 py-2 rounded-xl border border-black/10 bg-white text-sm font-semibold hover:bg-gray-50 transition">
                    Reroute
                  </button>
                  <button className="px-4 py-2 rounded-xl border border-black/10 bg-white text-sm font-semibold hover:bg-gray-50 transition">
                    Nearby
                  </button>
                </div>
              </div>

              <div className="mt-6 text-sm text-gray-600">
                Tip: reports gain “confidence” as more drivers confirm them.
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}
