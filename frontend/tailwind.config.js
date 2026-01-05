/** @type {import('tailwindcss').Config} */
export default {
  darkMode: "class",
  content: [
    "./index.html",
    "./src/**/*.{js,jsx,ts,tsx}",
  ],
  theme: {
    extend: {
      fontFamily: {
        gamja: ['"Gamja Flower"', "sans-serif"],
        archivo: ['"Archivo Black"', "sans-serif"]
      },
    },
  },
  plugins: [],
};