import React from "react";

export default function HomeNavbar() {
  return (
    <header className="fixed top-0 left-0 w-full z-50 backdrop-blur-xl bg-white/70 border-b border-black/5">
      <div className="max-w-7xl mx-auto px-6 h-20 grid grid-cols-3 items-center">
        
        {/* LEFT: Logo */}
        <div className="text-2xl font-bold tracking-tight justify-self-start">
          Trip
          <span className="bg-gradient-to-r from-blue-600 to-cyan-500 bg-clip-text text-transparent">
            wire
          </span>
        </div>

        {/* CENTER: Navigation */}
        <nav className="hidden md:flex items-center gap-8 text-sm justify-self-center">
          <a
            href="#nav-hazards"
            className="text-gray-600 hover:text-gray-900 transition-colors"
          >
            Navigation
          </a>
          <a
            href="#social"
            className="text-gray-600 hover:text-gray-900 transition-colors"
          >
            Social
          </a>
          <a
            href="#telemetry"
            className="text-gray-600 hover:text-gray-900 transition-colors"
          >
            Telemetry
          </a>
        </nav>

        {/* RIGHT: Auth */}
        <div className="flex items-center gap-3 justify-self-end">
          <a
            href="/login"
            className="px-4 py-2 text-gray-700 hover:text-gray-900 transition-colors"
          >
            Login
          </a>
          <a
            href="/signup"
            className="px-6 py-2.5 rounded-2xl bg-gradient-to-r from-blue-600 to-cyan-500 hover:from-blue-500 hover:to-cyan-400 transition-all font-medium shadow-lg shadow-blue-500/20 hover:shadow-blue-500/35"
          >
            Sign Up
          </a>
        </div>

      </div>
    </header>
  );
}
