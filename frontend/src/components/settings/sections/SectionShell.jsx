export default function SectionShell({ children }) {
  return (
    <div className="w-full rounded-2xl border border-gray-200 dark:border-gray-800 bg-white dark:bg-gray-900 p-4">
      {children}
    </div>
  );
}
