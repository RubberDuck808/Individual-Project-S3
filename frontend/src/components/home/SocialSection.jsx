import React from "react";

export default function SocialSection() {
  const leaderboard = [
    { name: "Amina", points: 1420, color: "bg-[#FF6AC1]", img: "🐱" },
    { name: "Leo", points: 1310, color: "bg-[#00D1FF]", img: "🐸" },
    { name: "Maya", points: 1215, color: "bg-[#FFD600]", img: "🐶" },
  ];

  return (
    <section id="social" className="max-w-7xl mx-auto px-6 py-24">
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-20 items-center">
        {/* Mock UI Leaderboard */}
        <div className="lg:col-span-7 lg:order-1 order-2">
          <div className="rounded-[3.5rem] border-[6px] border-black bg-white p-10 shadow-[16px_16px_0px_0px_#0066FF]">
            <div className="flex items-center justify-between mb-10">
              <h3 className="text-4xl font-[1000] italic uppercase tracking-tighter text-black">Hall of Fame</h3>
              <div className="px-6 py-2 bg-[#FFD600] border-[3px] border-black rounded-full font-black text-xs uppercase shadow-[4px_4px_0px_0px_rgba(0,0,0,1)]">Week 12</div>
            </div>

            <div className="space-y-6">
              {leaderboard.map((p, idx) => (
                <div key={p.name} className="flex items-center justify-between p-6 rounded-[2rem] bg-white border-[4px] border-black shadow-[6px_6px_0px_0px_rgba(0,0,0,1)] hover:translate-x-2 transition-transform cursor-pointer group">
                  <div className="flex items-center gap-6">
                    <div className="text-2xl font-[1000] text-slate-300 group-hover:text-[#FF6AC1]">0{idx + 1}</div>
                    <div className={`w-16 h-16 ${p.color} rounded-full border-[4px] border-black shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] flex items-center justify-center text-3xl group-hover:rotate-12 transition-transform`}>
                      {p.img}
                    </div>
                    <div className="font-[1000] text-2xl text-black uppercase italic tracking-tighter">{p.name}</div>
                  </div>
                  <div className="text-right">
                    <div className="font-[1000] text-[#0066FF] text-3xl leading-none">{p.points}</div>
                    <div className="text-[10px] font-black text-slate-400 uppercase tracking-widest">Points</div>
                  </div>
                </div>
              ))}
            </div>

            <button className="mt-10 w-full py-6 bg-black text-[#00D1FF] rounded-3xl border-[4px] border-black font-[1000] uppercase tracking-widest shadow-[8px_8px_0px_0px_#00D1FF] hover:shadow-none hover:translate-x-2 hover:translate-y-2 transition-all">
              Join the Rankings →
            </button>
          </div>
        </div>

        {/* Copy */}
        <div className="lg:col-span-5 lg:order-2 order-1">
          <div className="inline-flex items-center gap-2 rounded-2xl bg-[#FF6AC1] border-[3px] border-black px-5 py-2 text-xs font-[1000] uppercase tracking-widest text-white shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] mb-8">
            ✨ Friendly Competition
          </div>
          <h2 className="text-5xl md:text-6xl font-[1000] leading-[0.95] text-slate-900 uppercase italic">
            Climb the <br/><span className="text-[#FFD600] drop-shadow-[2px_2px_0px_#000]">Leaderboard.</span>
          </h2>
          <p className="mt-8 text-xl text-slate-600 font-bold leading-relaxed">
            Turn your commute into a quest. Earn badges, climb the ranks, and prove you're the safest driver on the road.
          </p>
        </div>
      </div>
    </section>
  );
}