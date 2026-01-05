import React from "react";

export default function TelemetrySection() {
  const metrics = [
    { label: "Speed", value: "62", unit: "mph" },
    { label: "RPM", value: "2.4k", unit: "" },
    { label: "Coolant", value: "196", unit: "°F" },
    { label: "Battery", value: "13.9", unit: "V" },
    { label: "Fuel", value: "58", unit: "%" },
    { label: "Trip", value: "14.2", unit: "mi" },
  ];

  return (
    <section id="telemetry" className="max-w-7xl mx-auto px-6 py-20">
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-10 items-center">
        {/* Copy */}
        <div className="lg:col-span-5">
          <div className="inline-flex items-center gap-2 rounded-full border border-black/10 bg-white/70 px-4 py-2 text-sm">
            <span className="h-2 w-2 rounded-full bg-cyan-500" />
            Telemetry (device → car data)
          </div>

          <h2 className="mt-5 text-4xl font-bold leading-tight">
            Turn your car into a live data stream.
          </h2>

          <p className="mt-4 text-lg text-gray-600 leading-relaxed">
            Plug in a device and pull real-time telemetry: speed, RPM, battery
            voltage, engine temperature, trip stats, and more. Use it for
            insights, alerts, and safer driving habits.
          </p>

          <div className="mt-8 grid grid-cols-1 sm:grid-cols-2 gap-4">
            {[
              { title: "Live metrics", desc: "Clean dashboard while you drive." },
              {
                title: "Smart alerts",
                desc: "Get notified for anomalies and thresholds.",
              },
              {
                title: "Trip summaries",
                desc: "See patterns and improvement over time.",
              },
              {
                title: "Privacy controls",
                desc: "You control what gets shared, always.",
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
              <div className="font-semibold">Telemetry Dashboard</div>
              <div className="text-sm text-gray-600">OBD / device connected</div>
            </div>

            <div className="p-6">
              <div className="grid grid-cols-2 md:grid-cols-3 gap-4">
                {metrics.map((m) => (
                  <div
                    key={m.label}
                    className="rounded-2xl border border-black/10 bg-white p-5"
                  >
                    <div className="text-xs text-gray-500">{m.label}</div>
                    <div className="mt-2 flex items-end gap-2">
                      <div className="text-2xl font-bold">{m.value}</div>
                      <div className="text-sm text-gray-500">{m.unit}</div>
                    </div>
                    <div className="mt-3 h-1.5 w-full rounded-full bg-gray-100 overflow-hidden">
                      <div className="h-full w-2/3 bg-gradient-to-r from-blue-600 to-cyan-500" />
                    </div>
                  </div>
                ))}
              </div>

              <div className="mt-6 rounded-2xl border border-black/10 bg-white p-5">
                <div className="flex items-center justify-between">
                  <div className="font-semibold">Alerts</div>
                  <div className="text-xs text-gray-500">last 5 min</div>
                </div>
                <ul className="mt-3 space-y-2 text-sm text-gray-700">
                  <li className="flex items-center gap-2">
                    <span className="h-2 w-2 rounded-full bg-emerald-500" />
                    All systems normal.
                  </li>
                  <li className="flex items-center gap-2">
                    <span className="h-2 w-2 rounded-full bg-amber-500" />
                    Hard braking detected (1) — drive smoother for a higher
                    safety score.
                  </li>
                </ul>
              </div>

              <div className="mt-4 text-sm text-gray-600">
                Note: this UI is a preview — exact metrics depend on the device
                + vehicle compatibility.
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}
