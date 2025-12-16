import { useEffect, useState } from "react";
import { useTheme } from "../context/ThemeContext";
import { getCategoriesCached, createHazard } from "../api/hazardApi";
import { getIconUrl } from "../utils/getIconUrl";


export default function HazardFormPanel({ coords, onClose }) {
  const [categories, setCategories] = useState([]);
  const [submitting, setSubmitting] = useState(false);
  const { darkMode } = useTheme();

  const locating =
    !coords || coords.loading || coords.lat == null || coords.lng == null;

  // Load categories
  useEffect(() => {
    getCategoriesCached()
      .then(setCategories)
      .catch((err) =>
        console.error("Failed to load hazard categories:", err)
      );
  }, []);

  const handleSelect = async (categoryId) => {
    if (locating) {
      alert("Still determining your location. Please wait a moment.");
      return;
    }

    const user = JSON.parse(localStorage.getItem("user"));
    if (!user) {
      alert("You must be logged in to report a hazard.");
      return;
    }

    setSubmitting(true);

    try {
      await createHazard({
        latitude: coords.lat,
        longitude: coords.lng,
        categoryId,
        userId: user.id,
      });

      alert("Hazard reported successfully!");
      onClose();
    } catch (error) {
      alert(error.message);
    } finally {
      setSubmitting(false);
    }
  };

  const disabled = submitting || locating;

  return (
    <div className="fixed inset-0 bg-black/60 backdrop-blur-sm z-50 flex items-end">
      <div
        className={`w-full rounded-t-3xl p-6 transition-colors duration-300 relative animate-slideUp ${
          darkMode ? "bg-gray-900 text-white" : "bg-white text-gray-900"
        }`}
      >
        <button
          onClick={onClose}
          className={`absolute top-4 left-4 text-2xl transition ${
            darkMode
              ? "text-gray-300 hover:text-white"
              : "text-gray-600 hover:text-black"
          }`}
        >
          ✕
        </button>

        <h2 className="text-center text-2xl font-bold mb-2">Report Hazard</h2>

        {locating && (
          <p className="text-center text-sm mb-2 text-gray-400">
            Getting your location...
          </p>
        )}

        {submitting && (
          <p className="text-center text-blue-400 font-semibold mb-4">
            Submitting...
          </p>
        )}

        <div className="grid grid-cols-2 gap-y-8 gap-x-6 justify-items-center mt-4">
          {categories.map((c) => (
            <button
              key={c.id}
              onClick={() => !disabled && handleSelect(c.id)}
              className={`flex flex-col items-center gap-2 transition-transform hover:scale-105 ${
                disabled ? "opacity-50 pointer-events-none" : ""
              }`}
            >
              {/* ICON CIRCLE */}
              <div
                className={`w-24 h-24 rounded-full flex items-center justify-center shadow-md transition-colors duration-200 ${
                  darkMode
                    ? "bg-gray-800 text-white border border-gray-700"
                    : "bg-gray-200 text-gray-900 border border-gray-300"
                }`}
              >
                {c.icon ? (
                  <img
                    src={getIconUrl(c.icon)}
                    alt={c.name}
                    className="w-12 h-12 object-contain"
                  />

                ) : (
                  <span className="font-bold text-lg">{c.name[0]}</span>
                )}
              </div>

              {/* LABEL */}
              <span
                className={`text-sm font-semibold ${
                  darkMode ? "text-gray-200" : "text-gray-800"
                }`}
              >
                {c.name}
              </span>
            </button>
          ))}

          {categories.length === 0 && (
            <p className="col-span-2 text-center text-sm text-gray-400">
              Loading hazard types...
            </p>
          )}
        </div>
      </div>
    </div>
  );
}
