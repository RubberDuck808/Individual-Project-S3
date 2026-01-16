import React from 'react';
import PropTypes from 'prop-types';

export default function ReportButton({ onClick, children }) {
  return (
    <button
      onClick={onClick}
      className="fixed bottom-8 right-8 z-50 group transition-all active:scale-90"
      aria-label="Report Hazard"
    >
      {/* THE MAIN BUTTON BODY */}
      <div className="relative bg-[#FF6AC1] border-[4px] border-black rounded-[2rem] p-4 shadow-[8px_8px_0px_0px_rgba(0,0,0,1)] group-hover:shadow-none group-hover:translate-x-[4px] group-hover:translate-y-[4px] transition-all overflow-hidden">
        
        {/* FLASH EFFECT ON HOVER */}
        <div className="absolute inset-0 bg-white opacity-0 group-hover:opacity-20 transition-opacity" />
        
        {/* THE ICON (CONE) */}
        <div className="relative z-10 transform group-hover:rotate-12 transition-transform duration-300">
          {children}
        </div>
      </div>

      {/* FLOATING TAG */}
      <div className="absolute -top-4 -left-4 bg-[#FFD600] border-[3px] border-black px-3 py-1 rounded-lg shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] -rotate-12 group-hover:rotate-0 transition-transform">
        <span className="text-[10px] font-[1000] uppercase tracking-tighter">Report</span>
      </div>
    </button>
  );
}

ReportButton.propTypes = {
  onClick: PropTypes.func.isRequired,
  children: PropTypes.node.isRequired,
};