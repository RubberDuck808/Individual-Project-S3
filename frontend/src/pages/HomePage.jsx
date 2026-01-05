import React, { useEffect } from "react";
import Footer from "../components/home/Footer";

import HomeNavbar from "../components/home/HomeNavbar";
import HomeHero from "../components/home/HomeHero";

import NavHazardSection from "../components/home/NavHazardSection";
import SocialSection from "../components/home/SocialSection";
import TelemetrySection from "../components/home/TelemetrySection";

export default function HomePage() {
  useEffect(() => {
    const orbs = document.querySelectorAll(".gradient-orb");
    let t = 0;
    const id = window.setInterval(() => {
      t += 1;
      const s = 1 + Math.sin(t / 12) * 0.05;
      orbs.forEach((orb) => {
        orb.style.transform = `scale(${s})`;
        orb.style.opacity = `${0.18 + Math.sin(t / 10) * 0.03}`;
      });
    }, 60);

    return () => window.clearInterval(id);
  }, []);

  return (
    <div className="min-h-screen bg-gradient-to-br from-gray-50 via-gray-100 to-gray-50 text-gray-900 overflow-hidden relative">
      {/* Subtle ambient orbs */}
      <div className="gradient-orb fixed top-24 left-10 w-96 h-96 bg-blue-400/20 rounded-full blur-3xl pointer-events-none" />
      <div className="gradient-orb fixed bottom-24 right-10 w-96 h-96 bg-purple-400/15 rounded-full blur-3xl pointer-events-none" />

      <HomeNavbar />

      <main className="pt-20">
        <HomeHero />

        <NavHazardSection />
        <SocialSection />
        <TelemetrySection />

        <Footer />
      </main>
    </div>
  );
}
