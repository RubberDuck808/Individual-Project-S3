import React from "react";

export default function Footer() {
  const columns = [
    {
      title: "Company",
      items: ["Contact Us"],
    },
    {
      title: "Legal",
      items: [
        "Terms",
        "Privacy Policy",
        "Trust",
        "Customer Business Agreement",
        "Cookies Preferences Center",
      ],
    },
  ];

  return (
    <footer className="relative w-full overflow-hidden bg-gradient-to-b from-gray-900 to-gray-800 text-white">
      {/* Soft top divider */}
      <div className="h-px w-full bg-white/10" />

      {/* Main content: reduce bottom padding so SVG sits closer */}
      <div className="mx-auto max-w-7xl px-6 pt-14 pb-6">
        <div className="grid grid-cols-1 gap-8 md:grid-cols-12">
          {/* Brand */}
          <div className="md:col-span-4">
            <div className="flex items-center gap-3">
              <div className="grid h-11 w-11 place-items-center rounded-2xl bg-white/10 ring-1 ring-white/15">
                <span className="text-sm font-bold">T</span>
              </div>
              <span className="text-sm font-semibold tracking-wide">
                TRIPWIRE
              </span>
            </div>

            <h3 className="mt-6 text-3xl font-semibold leading-tight">
              Drive smarter.
              <br />
              Drive safer.
              <br />
              Together.
            </h3>

            {/* tighten this too */}
            <p className="mt-8 text-sm text-white/70">
              Builders of Tripwire Platform® apps
            </p>
          </div>

          {/* Link columns */}
          <div className="md:col-span-8 flex justify-end">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
              {columns.map((col) => (
                <div key={col.title} className="px-8 py-8">
                  <h4 className="text-sm font-semibold text-white/90">
                    {col.title}
                  </h4>
                  <ul className="mt-5 space-y-3 text-sm">
                    {col.items.map((item) => (
                      <li key={item}>
                        <a
                          href="#"
                          className="text-white/70 hover:text-white transition-colors"
                        >
                          {item}
                          {item === "Privacy Policy" && (
                            <span className="ml-1 text-white/50">↗</span>
                          )}
                        </a>
                      </li>
                    ))}
                  </ul>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>

      {/* Watermark: reduce vertical padding here */}
        <div className="relative w-full overflow-hidden">
          <img
            src="/logos/tripwire.svg"
            alt="TRIPWIRE"
            className="
              pointer-events-none select-none
              relative left-1/2 -translate-x-1/2
              w-[360vw] min-w-[3200px]
              h-auto
              scale-x-110
              brightness-0 invert opacity-[0.15]
              object-contain
            "
            style={{ maxHeight: "25rem" }}
          />

          <div className="pointer-events-none absolute inset-x-0 bottom-0 h-20 bg-gradient-to-b from-transparent to-gray-800" />
        </div>
    </footer>
  );
}
