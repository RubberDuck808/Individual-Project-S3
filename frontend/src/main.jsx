import { Buffer } from "node:buffer";
globalThis.Buffer = Buffer;

import React from "react";
import ReactDOM from "react-dom/client";
import { BrowserRouter } from "react-router-dom";

import App from "./App";
import { ThemeProvider } from "./context/ThemeContext";
import { ToastProvider } from "./context/ToastContext";
import Toaster from "./components/Toaster";
import "./index.css";

ReactDOM.createRoot(document.getElementById("root")).render(
  <BrowserRouter>
    <ThemeProvider>
      <ToastProvider>
        <App />
        <Toaster />
      </ToastProvider>
    </ThemeProvider>
  </BrowserRouter>
);
