import React from "react";
import { describe, it, expect, beforeEach } from "vitest";
import { render, screen, act } from "@testing-library/react";
import { ThemeProvider, useTheme } from "../../src/context/ThemeContext";

// Test component that uses the theme
function TestComponent() {
  const { darkMode, toggleDarkMode } = useTheme();
  return (
    <div>
      <div data-testid="mode">{darkMode ? "dark" : "light"}</div>
      <button onClick={toggleDarkMode} data-testid="toggle">
        Toggle
      </button>
    </div>
  );
}

describe("ThemeContext", () => {
  beforeEach(() => {
    localStorage.clear();
    document.documentElement.classList.remove("dark");
  });

  it("should provide light mode by default", () => {
    render(
      <ThemeProvider>
        <TestComponent />
      </ThemeProvider>
    );

    expect(screen.getByTestId("mode")).toHaveTextContent("light");
    expect(document.documentElement.classList.contains("dark")).toBe(false);
  });

  it("should toggle between light and dark mode", () => {
    render(
      <ThemeProvider>
        <TestComponent />
      </ThemeProvider>
    );

    const toggle = screen.getByTestId("toggle");
    const mode = screen.getByTestId("mode");

    expect(mode).toHaveTextContent("light");

    act(() => {
      toggle.click();
    });

    expect(mode).toHaveTextContent("dark");
    expect(document.documentElement.classList.contains("dark")).toBe(true);

    act(() => {
      toggle.click();
    });

    expect(mode).toHaveTextContent("light");
    expect(document.documentElement.classList.contains("dark")).toBe(false);
  });

  it("should persist theme preference in localStorage", () => {
    render(
      <ThemeProvider>
        <TestComponent />
      </ThemeProvider>
    );

    const toggle = screen.getByTestId("toggle");

    act(() => {
      toggle.click();
    });

    expect(localStorage.getItem("theme")).toBe("dark");

    act(() => {
      toggle.click();
    });

    expect(localStorage.getItem("theme")).toBe("light");
  });

  it("should restore theme from localStorage on mount", () => {
    localStorage.setItem("theme", "dark");

    render(
      <ThemeProvider>
        <TestComponent />
      </ThemeProvider>
    );

    expect(screen.getByTestId("mode")).toHaveTextContent("dark");
    expect(document.documentElement.classList.contains("dark")).toBe(true);
  });
});
