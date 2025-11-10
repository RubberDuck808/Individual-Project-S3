export default function TailwindTest() {
  return (
    <div className="min-h-screen flex flex-col items-center justify-center bg-gray-100">
      <div className="p-8 rounded-2xl shadow-lg bg-white border border-gray-300">
        <h1 className="text-3xl font-bold text-blue-600 mb-4">
          Tailwind is working!
        </h1>

        <p className="text-gray-700 mb-6">
          If you can see styled text, rounded corners, and spacing,
          Tailwind is correctly configured.
        </p>

        <button className="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition">
          Test Button
        </button>
      </div>
    </div>
  );
}
