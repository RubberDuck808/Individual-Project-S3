import React from "react";
import HomeNavbar from "../components/home/HomeNavbar";
import HomeHero from "../components/home/HomeHero";
import NavigationSection from "../components/home/NavigationSection";
import SocialSection from "../components/home/SocialSection";
import TelemetrySection from "../components/home/TelemetrySection";
import Footer from "../components/home/Footer";

export default function HomePage() {
  return (
    <div className="min-h-screen bg-[#FFFDF5] text-[#1D1D1F] selection:bg-[#FFD600] overflow-x-hidden">
      {/* Playful Ambient Blobs */}
      <div className="fixed inset-0 z-0 pointer-events-none overflow-hidden">
        <div 
          className="absolute top-[-10%] left-[-5%] w-[600px] h-[600px] bg-[#FF6AC1]/10 blur-[120px] rounded-full animate-pulse" 
          style={{ animationDuration: '10s' }} 
        />
        <div 
          className="absolute bottom-[10%] right-[-5%] w-[700px] h-[700px] bg-[#00D1FF]/10 blur-[150px] rounded-full animate-pulse" 
          style={{ animationDuration: '15s' }} 
        />
      </div>

      <div className="relative z-10">
        <HomeNavbar />
        
        <main className="pt-24">
          <HomeHero />
          
          {/* Main Content Sections */}
          <div className="space-y-12">
            <NavigationSection />
            <SocialSection />
            <TelemetrySection />
          </div>

          <Footer />
        </main>
      </div>
    </div>
  );
}