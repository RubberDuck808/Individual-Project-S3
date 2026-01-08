export default function ReportButton({ onClick, children }) {
  return (
    <button
      onClick={onClick}
      aria-label="Report hazard"
      className="
        fixed right-6
        bottom-[calc(1.75rem+4rem+env(safe-area-inset-bottom))]
        z-50
        h-[81px] w-[81px] rounded-full
        bg-black shadow-2xl shadow-black/35
        active:scale-95 transition-all duration-200
        flex items-center justify-center
      "
    >
      {children}
    </button>
  );
}
