import { useEffect, useState } from "react";
import { Settings, ArrowRight } from "lucide-react";
import { useNavigate } from "react-router-dom";
import { useTheme } from "../context/ThemeContext";

export default function ProfilePage() {
  const navigate = useNavigate();
  const { darkMode } = useTheme();
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  // Placeholder trip data — later can be fetched from backend
  const recentTrips = [
    { id: 1, from: "Eindhoven", to: "Amsterdam", distance: "124 km", date: "Nov 6" },
    { id: 2, from: "Rotterdam", to: "The Hague", distance: "27 km", date: "Nov 2" },
  ];

  useEffect(() => {
    const storedUser = JSON.parse(localStorage.getItem("user"));

    if (!storedUser || !storedUser.id) {
      navigate("/login");
      return;
    }

    fetch(`http://localhost:8080/api/users/${storedUser.id}`)
      .then((res) => {
        if (!res.ok) throw new Error("Failed to fetch user info");
        return res.json();
      })
      .then((data) => setUser(data))
      .catch((err) => {
        console.error(err);
        alert("Error fetching user info");
      })
      .finally(() => setLoading(false));
  }, [navigate]);

  if (loading) {
    return (
      <div className="flex justify-center items-center h-full">
        <p className={darkMode ? "text-gray-400" : "text-gray-500"}>
          Loading profile...
        </p>
      </div>
    );
  }

  if (!user) {
    return (
      <div className="flex justify-center items-center h-full">
        <p className="text-red-500">No user data available.</p>
      </div>
    );
  }

  return (
    <div
      className={`relative h-full w-full p-6 flex flex-col items-center transition-colors duration-300 ${
        darkMode ? "bg-gray-900 text-white" : "bg-gray-100 text-gray-900"
      }`}
    >
      {/* Settings button */}
      <button
        onClick={() => navigate("/settings")}
        className={`absolute top-4 right-4 transition ${
          darkMode
            ? "text-gray-300 hover:text-white"
            : "text-gray-600 hover:text-gray-900"
        }`}
      >
        <Settings size={24} />
      </button>

      {/* Profile info */}
      <img
        src="https://upload.wikimedia.org/wikipedia/commons/8/89/Portrait_Placeholder.png"
        alt="Profile"
        className="w-24 h-24 rounded-full object-cover border-4 border-white shadow-md mt-8"
      />
      <h1 className="text-2xl font-bold mt-4">{user.name || "No name set"}</h1>
      <p className="text-gray-500 dark:text-gray-400">@{user.username}</p>
      <p className="text-gray-400 text-sm mt-1">{user.email}</p>

      {/* Divider */}
      <div
        className={`w-full h-px my-6 ${
          darkMode ? "bg-gray-700" : "bg-gray-300"
        }`}
      />

      {/* Stats */}
      <div
        className={`w-full rounded-xl shadow p-4 transition-colors duration-300 ${
          darkMode ? "bg-gray-800" : "bg-white"
        }`}
      >
        <h2 className="text-lg font-semibold mb-3">Your Stats</h2>
        <div
          className={`flex justify-between ${
            darkMode ? "text-gray-300" : "text-gray-600"
          }`}
        >
          <div>
            <p className="font-semibold text-xl text-blue-500">12</p>
            <p className="text-sm">Hazards Reported</p>
          </div>
          <div>
            <p className="font-semibold text-xl text-green-500">4.8</p>
            <p className="text-sm">Safety Score</p>
          </div>
          <div>
            <p className="font-semibold text-xl text-yellow-400">7</p>
            <p className="text-sm">Achievements</p>
          </div>
        </div>
      </div>

      {/* Friends / Leaderboard */}
      <div
        className={`w-full rounded-xl shadow p-4 mt-6 transition-colors duration-300 ${
          darkMode ? "bg-gray-800" : "bg-white"
        }`}
      >
        <h2 className="text-lg font-semibold mb-3">Friends & Leaderboard</h2>
        <p
          className={`text-sm ${
            darkMode ? "text-gray-400" : "text-gray-500"
          }`}
        >
          Coming soon: see how you rank among your friends.
        </p>
      </div>

      {/* My Trips */}
      <div
        className={`mt-8 rounded-xl shadow p-4 transition-colors w-full ${
          darkMode ? "bg-gray-800" : "bg-white"
        }`}
      >
        <div className="flex justify-between items-center mb-3">
          <h2 className="text-lg font-semibold">My Trips</h2>
          <button
            onClick={() => navigate("/trips")}
            className="flex items-center text-blue-600 hover:underline"
          >
            View All <ArrowRight size={16} className="ml-1" />
          </button>
        </div>

        {recentTrips.map((trip) => (
          <div
            key={trip.id}
            className="flex justify-between border-b border-gray-200 dark:border-gray-700 py-2"
          >
            <div>
              <p className="font-medium">
                {trip.from} → {trip.to}
              </p>
              <p className="text-sm text-gray-500">{trip.date}</p>
            </div>
            <span className="text-sm text-gray-600 dark:text-gray-300">
              {trip.distance}
            </span>
          </div>
        ))}
      </div>
    </div>
  );
}
