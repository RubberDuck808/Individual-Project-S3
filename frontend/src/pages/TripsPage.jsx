import React from "react";
import { ArrowLeft } from "lucide-react";
import { useNavigate } from "react-router-dom";
import { useTheme } from "../context/ThemeContext";

export default function TripsPage() {
  const navigate = useNavigate();
  const { darkMode } = useTheme();

  const trips = [
    { id: 1, from: "Eindhoven", to: "Amsterdam", distance: "124 km", date: "Nov 6", duration: "1h 20m" },
    { id: 2, from: "Rotterdam", to: "The Hague", distance: "27 km", date: "Nov 2", duration: "25m" },
    { id: 3, from: "Utrecht", to: "Arnhem", distance: "67 km", date: "Oct 28", duration: "50m" },
    { id: 4, from: "Breda", to: "Tilburg", distance: "22 km", date: "Oct 21", duration: "18m" },
  ];

  return (
    <div
      className={`h-full w-full p-6 transition-colors duration-300 ${
        darkMode ? "bg-gray-900 text-white" : "bg-gray-100 text-gray-900"
      }`}
    >
      <button
        onClick={() => navigate(-1)}
        className="flex items-center text-gray-500 hover:text-gray-800 mb-4"
      >
        <ArrowLeft className="mr-1" /> Back
      </button>

      <h1 className="text-2xl font-bold mb-4">My Trips</h1>

      <div
        className={`rounded-xl shadow overflow-y-auto max-h-[70vh] ${
          darkMode ? "bg-gray-800" : "bg-white"
        }`}
      >
        <table className="w-full text-sm">
          <thead className={darkMode ? "bg-gray-700 text-gray-200" : "bg-gray-100 text-gray-700"}>
            <tr>
              <th className="p-3 text-left">From</th>
              <th className="p-3 text-left">To</th>
              <th className="p-3 text-left">Distance</th>
              <th className="p-3 text-left">Date</th>
              <th className="p-3 text-left">Duration</th>
            </tr>
          </thead>
          <tbody>
            {trips.map((trip) => (
              <tr
                key={trip.id}
                className={`border-t ${darkMode ? "border-gray-700 hover:bg-gray-700/50" : "border-gray-200 hover:bg-gray-50"}`}
              >
                <td className="p-3">{trip.from}</td>
                <td className="p-3">{trip.to}</td>
                <td className="p-3">{trip.distance}</td>
                <td className="p-3">{trip.date}</td>
                <td className="p-3">{trip.duration}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
