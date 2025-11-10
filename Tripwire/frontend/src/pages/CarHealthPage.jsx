import { useTheme } from "../context/ThemeContext";

export default function CarHealthPage() {
  const { darkMode } = useTheme();

  return (
    <div
      className={`h-full w-full flex flex-col items-center justify-center transition-colors duration-300 ${
        darkMode ? "bg-gray-900 text-white" : "bg-gray-100 text-gray-900"
      }`}
    >
      <div
        className={`p-6 rounded-2xl shadow-lg max-w-md text-center transition-colors duration-300 ${
          darkMode ? "bg-gray-800" : "bg-white"
        }`}
      >
        <h1 className="text-2xl font-bold mb-3">Car Health</h1>
        <p
          className={`leading-relaxed ${
            darkMode ? "text-gray-300" : "text-gray-600"
          }`}
        >
          This page will connect to your Arduino and display car sensor data such as engine temperature,
          battery voltage, and more.
        </p>
      </div>
    </div>
  );
}
