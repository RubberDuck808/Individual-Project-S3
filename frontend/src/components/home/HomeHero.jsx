import React from "react";

export default function HomeHero() {
  return (
    <section className="max-w-7xl mx-auto px-6 pt-20 pb-10 text-center">
      <h1 className="text-5xl md:text-7xl font-bold tracking-tight leading-tight">
        Driving, upgraded by{" "}
        <span className="bg-gradient-to-r from-blue-600 via-cyan-500 to-blue-600 bg-clip-text text-transparent">
          community + data
        </span>
        .
      </h1>

      <p className="mt-6 text-xl text-gray-600 max-w-3xl mx-auto leading-relaxed">
        Tripwire combines real-time navigation + hazard reporting, a social
        layer for friendly competition, and live telemetry from your car — all
        in one experience.
      </p>

      <div className="mt-10 flex flex-wrap justify-center gap-4">
        <a
          href="/signup"
          className="group px-8 py-4 rounded-2xl bg-gradient-to-r from-blue-600 to-cyan-500 hover:from-blue-500 hover:to-cyan-400 transition-all font-medium shadow-xl shadow-blue-500/25 hover:shadow-blue-500/40 hover:scale-105 transform"
        >
          Get Early Access
          <span className="inline-block ml-2 group-hover:translate-x-1 transition-transform">
            →
          </span>
        </a>

        <a
          href="#nav-hazards"
          className="px-8 py-4 rounded-2xl border border-black/10 hover:border-black/20 backdrop-blur-sm bg-white/60 hover:bg-white/80 transition-all hover:scale-105 transform"
        >
          See the 3 pillars
        </a>
      </div>

      <div className="mt-12 grid grid-cols-1 sm:grid-cols-3 gap-4 max-w-4xl mx-auto">
        {[
          { label: "Hazards reported", value: "Real-time" },
          { label: "Friends & ranks", value: "Social layer" },
          { label: "Car telemetry", value: "Device-powered" },
        ].map((s) => (
          <div
            key={s.label}
            className="rounded-3xl border border-black/10 bg-white/70 backdrop-blur-xl p-5"
          >
            <div className="text-sm text-gray-600">{s.label}</div>
            <div className="mt-1 text-xl font-semibold">{s.value}</div>
          </div>
        ))}
      </div>
    </section>
  );
}
