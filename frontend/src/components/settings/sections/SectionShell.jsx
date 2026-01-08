export default function SectionShell({ children }) {
  return (
    <div className="w-full rounded-[2rem] border-[3px] border-black bg-white p-6 shadow-[8px_8px_0px_0px_rgba(0,0,0,1)]">
      {children}
    </div>
  );
}