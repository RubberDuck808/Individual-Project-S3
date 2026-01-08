import React from "react";

export default function HomeNavbar() {
  return (
    <header className="fixed top-8 left-1/2 -translate-x-1/2 w-[95%] max-w-6xl z-50">
      <div className="bg-white border-[4px] border-black rounded-[3rem] px-8 h-20 flex items-center justify-between shadow-[8px_8px_0px_0px_rgba(0,0,0,1)]">
        
        <a href="/" className="flex items-center gap-3 group">
          <div className="w-12 h-12 bg-[#0066FF] border-4 border-black rounded-2xl flex items-center justify-center rotate-[-8deg] group-hover:rotate-0 transition-all shadow-[4px_4px_0px_0px_rgba(0,0,0,1)]">
             <span className="text-white font-[1000] text-2xl">T</span>
          </div>
          <span className="font-[1000] text-3xl tracking-tighter uppercase italic group-hover:text-[#0066FF] transition-colors">tripwire</span>
        </a>

        <nav className="hidden lg:flex gap-10">
          {['Navigation', 'Social', 'Telemetry'].map(item => (
            <a key={item} href={`#${item.toLowerCase()}`} className="font-black text-sm uppercase tracking-widest text-slate-900 hover:text-[#FF6AC1] transition-colors">
              {item}
            </a>
          ))}
        </nav>

        <div className="flex items-center gap-6">
          <a href="/login" className="hidden sm:block font-black uppercase tracking-widest text-sm hover:underline decoration-4 decoration-[#FFD600]">Login</a>
          <a href="/signup" className="px-8 py-3 bg-[#00D1FF] border-4 border-black text-black rounded-full font-[1000] uppercase tracking-widest text-xs shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] hover:shadow-none hover:translate-x-[4px] hover:translate-y-[4px] transition-all">
            Join Now
          </a>
        </div>
      </div>
    </header>
  );
}