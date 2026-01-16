import React from "react";

export default function HomeHero() {
  return (
    <section className="max-w-7xl mx-auto px-6 pt-16 pb-12 text-center relative">
      {/* Floating Badge - Added a hard black border/shadow */}
      <div className="inline-flex items-center gap-2 px-5 py-2.5 rounded-2xl bg-[#FFD600] text-black text-sm font-[900] uppercase tracking-wider border-4 border-black shadow-[6px_6px_0px_0px_rgba(0,0,0,1)] mb-10 transform -rotate-3 hover:rotate-0 transition-transform cursor-default">
        ✨ 100% Community Powered
      </div>

      <h1 className="text-6xl md:text-[100px] font-[1000] tracking-tighter leading-[0.85] text-[#1D1D1F] uppercase italic">
        Drive <span className="text-white drop-shadow-[4px_4px_0px_#000]" style={{ WebkitTextStroke: '3px black' }}>Smarter.</span><br />
        <span className="text-[#0066FF] relative inline-block mt-4">
          Together.
          <svg className="absolute -bottom-4 left-0 w-full" viewBox="0 0 300 20" fill="none">
            <path d="M5 15C80 5 220 5 295 15" stroke="#FF6AC1" strokeWidth="8" strokeLinecap="round"/>
          </svg>
        </span>
      </h1>

      <p className="mt-16 text-xl md:text-2xl text-slate-600 max-w-2xl mx-auto font-bold leading-relaxed">
        The ultimate co-pilot for your crew. Real-time hazard alerts, car telemetry, and a leaderboard that actually matters.
      </p>

      {/* Hero Actions */}
      <div className="mt-12 flex flex-col items-center gap-8">
        <a href="/signup" className="group relative px-12 py-6 bg-[#FF6AC1] text-white rounded-[2.5rem] font-black text-2xl border-4 border-black shadow-[8px_8px_0px_0px_rgba(0,0,0,1)] hover:shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] hover:translate-x-[4px] hover:translate-y-[4px] transition-all">
          GET INITIALIZED →
        </a>
        
        {/* Animated Avatar Group */}
        <div className="flex flex-col items-center gap-3">
          <div className="flex -space-x-4">
            {[
              { id: 'avatar-1', bg: 'bg-[#FF6AC1]', delay: '0s', img: '🐱' },
              { id: 'avatar-2', bg: 'bg-[#00D1FF]', delay: '0.2s', img: '🐸' },
              { id: 'avatar-3', bg: 'bg-[#FFD600]', delay: '0.4s', img: '🐶' }
            ].map((user) => (
              <div 
                key={user.id}
                className={`w-16 h-16 rounded-full border-4 border-black ${user.bg} flex items-center justify-center text-3xl shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] animate-bounce`}
                style={{ animationDuration: '3s', animationDelay: user.delay }}
              >
                {user.img}
              </div>
            ))}
            <div className="w-16 h-16 rounded-full border-4 border-black bg-white flex items-center justify-center font-black text-sm shadow-[4px_4px_0px_0px_rgba(0,0,0,1)]">
              +2k
            </div>
          </div>
          <span className="text-xs font-black uppercase tracking-[0.2em] text-slate-400">Join 2,400+ Scouts</span>
        </div>
      </div>
    </section>
  );
}