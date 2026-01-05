import { defineConfig } from "vitest/config";

export default defineConfig({
  test: {
    globals: true,          
    environment: "happy-dom",
    setupFiles: "./tests/setupTests.js", 
    coverage: {
      provider: "v8",            
      reporter: ["text", "lcov"], 
      all: true,
    },
  },
});
