import { defineConfig } from "vitest/config";

export default defineConfig({
  test: {
    globals: true,          
    environment: "happy-dom",
    setupFiles: "./tests/setupTests.js", 
    // Emit JUnit in CI so GitLab can show frontend unit tests in the pipeline summary
    reporters: process.env.CI ? ["default", "junit"] : ["default"],
    outputFile: process.env.CI ? { junit: "test-results/junit.xml" } : undefined,
    coverage: {
      provider: "v8",
      reporter: ["text", "lcov"],
      all: true,
      exclude: [
        "src/main.jsx",
        "src/App.jsx",
        "vite.config.js",
        "vitest.config.js",
        "sonarConfig.cjs",
        "**/*.config.*",
        "**/node_modules/**",
        "tests/**",
      ],
    },
  },
});
