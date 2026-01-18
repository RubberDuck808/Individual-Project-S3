// Using 'buffer' package polyfill for browser compatibility (not Node.js built-in 'node:buffer')
import { Buffer } from "buffer"; // NOSONAR
globalThis.Buffer = Buffer;

import React from "react";
import ReactDOM from "react-dom/client";
import App from "./App";
import { ThemeProvider } from "./context/ThemeContext";
import "./index.css";

ReactDOM.createRoot(document.getElementById("root")).render(
  <ThemeProvider>
    <App />
  </ThemeProvider>
);
