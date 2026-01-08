import React from "react";

export default function Footer() {
  return (
    <footer className="mt-32 pb-12 px-6">
      <div className="max-w-7xl mx-auto rounded-[4rem] bg-[#1D1D1F] border-[6px] border-black text-white p-12 md:p-20 relative overflow-hidden shadow-[12px_12px_0px_0px_#0066FF]">
        
        <div className="relative z-10 grid grid-cols-1 lg:grid-cols-12 gap-16">
          <div className="lg:col-span-6">
            <h2 className="text-5xl md:text-7xl font-[1000] leading-none mb-10 uppercase italic">
              Don't Drive <br />
              <span className="text-[#FFD600]">Alone.</span>
            </h2>
            
            <p className="text-xl font-bold text-slate-400 mb-10 max-w-sm">
              The road is more fun when you've got a crew watching your back.
            </p>

            <div className="flex flex-wrap gap-4">
              {['Discord', 'Instagram', 'Twitter'].map(social => (
                <button key={social} className="px-8 py-4 rounded-2xl bg-white text-black border-4 border-black font-black uppercase tracking-widest shadow-[4px_4px_0px_0px_#FF6AC1] hover:shadow-none hover:translate-x-[4px] hover:translate-y-[4px] transition-all">
                  {social}
                </button>
              ))}
            </div>
          </div>

          <div className="lg:col-span-6 grid grid-cols-2 gap-8">
            {/* Link Lists with Bubbly Bullets */}
            {['Product', 'Legal'].map((cat) => (
              <div key={cat}>
                <h4 className="font-black uppercase text-[#00D1FF] text-sm tracking-[0.2em] mb-8">{cat}</h4>
                <ul className="space-y-6">
                  {['Dashboard', 'Leaderboard', 'Telemetry', 'Privacy'].slice(cat === 'Legal' ? 2 : 0).map(link => (
                    <li key={link}>
                      <a href="#" className="font-black text-xl hover:text-[#FF6AC1] transition-colors flex items-center gap-2 group">
                        <span className="w-3 h-3 bg-white rounded-full group-hover:scale-150 transition-transform" />
                        {link}
                      </a>
                    </li>
                  ))}
                </ul>
              </div>
            ))}
          </div>
        </div>

<div className="mt-24 pt-10 border-t-4 border-black flex flex-col md:flex-row justify-between items-center gap-8">
  <div className="flex items-center gap-6">
    {/* DIRECT LOGO - NO CIRCLE */}
    <a href="/" className="transition-transform hover:scale-110 active:scale-95">
      <img 
        src="https://storage.googleapis.com/tripwire_bucket/icons/tripwire_logo.svg" 
        alt="Tripwire Logo"
        className="w-12 h-12 object-contain" 
      />
    </a>
    
    <p className="font-black uppercase tracking-widest text-sm text-slate-500">
      © 2026 Tripwire Platforms Inc.
    </p>
  </div>

  <div className="px-6 py-2 bg-black rounded-full border-2 border-slate-700 font-mono text-[10px] text-emerald-400 uppercase tracking-[0.3em]">
    System Status: 100% Groovy
  </div>
</div>
      </div>
    </footer>
  );
}