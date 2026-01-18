import React, { createContext, useContext, useEffect, useState, useMemo, useCallback } from "react";
import PropTypes from "prop-types";

const ThemeContext = createContext();

export const ThemeProvider = ({ children }) => {
  const [darkMode, setDarkMode] = useState(
    localStorage.getItem("theme") === "dark"
  );

  useEffect(() => {
    if (darkMode) {
      document.documentElement.classList.add("dark");
      localStorage.setItem("theme", "dark");
    } else {
      document.documentElement.classList.remove("dark");
      localStorage.setItem("theme", "light");
    }
  }, [darkMode]);

  const toggleDarkMode = useCallback(() => {
    setDarkMode(prev => !prev);
  }, []);

  const contextValue = useMemo(
    () => ({ darkMode, setDarkMode, toggleDarkMode }),
    [darkMode, toggleDarkMode]
  );

  return (
    <ThemeContext.Provider value={contextValue}>
      {children}
    </ThemeContext.Provider>
  );
};

export const useTheme = () => useContext(ThemeContext);

ThemeProvider.propTypes = {
  children: PropTypes.node.isRequired,
};
