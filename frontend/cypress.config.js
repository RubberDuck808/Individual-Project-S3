import { defineConfig } from "cypress";

export default defineConfig({
  projectId: "7wtzuj",

  e2e: {
    // Use environment variable for base URL, default to local dev server
    // For e2e tests with test DB, use: http://localhost:5174
    baseUrl: process.env.CYPRESS_BASE_URL || process.env.CYPRESS_baseUrl || "http://localhost:5173",
    viewportWidth: 1280,
    viewportHeight: 720,
    video: false,
    screenshotOnRunFailure: true,
    defaultCommandTimeout: 10000,
    requestTimeout: 10000,
    responseTimeout: 10000,
    // Generate JUnit XML reports for CI/CD
    reporter: "junit",
    reporterOptions: {
      mochaFile: "cypress/results/[hash].xml",
      toConsole: false,
      outputs: true,
    },
    env: {
      // API URL for the backend (used by the app via VITE_API_URL)
      // For e2e tests with test DB, use: http://localhost:8081
      apiUrl:
        process.env.CYPRESS_API_URL ||
        process.env.VITE_API_URL ||
        "http://localhost:8080",
      // Test database connection info (for reference)
      testDbUrl:
        process.env.TEST_DB_URL || "jdbc:postgresql://localhost:5433/testdb",
      testDbUser: process.env.TEST_DB_USER || "test",
      testDbPassword: process.env.TEST_DB_PASSWORD || "test",
    },
    setupNodeEvents(on, config) {
      // Allow environment-specific configuration
      // You can override baseUrl and env vars via CYPRESS_* environment variables
      // Example: CYPRESS_BASE_URL=http://localhost:5174 CYPRESS_API_URL=http://localhost:8081 npm run test:e2e
      return config;
    },
  },

  component: {
    devServer: {
      framework: "react",
      bundler: "vite",
    },
  },
});
