import React from "react";
import ReactDOM from "react-dom/client";
import { BrowserRouter } from "react-router-dom";
import App from "./App.jsx";
import "./index.css";
import api from "./utils/axios.js";

// Auth Context
import { AuthProvider } from "./context/AuthContext.jsx";

// Google OAuth
import { GoogleOAuthProvider } from "@react-oauth/google";

// CRITICAL: Preload dashboard API data before components mount
// This ensures data is partially/fully loaded by the time Dashboard renders
// Uses configured API instance with JWT token interceptor

export const dashboardDataPromise = api.get('/drivers')
  .then(r => r.data)
  .catch(() => null)

export const racesDataPromise = api.get('/races')
  .then(r => r.data)
  .catch(() => null)

// Web Vitals tracking — measure real Core Web Vitals metrics
// Dynamic import to avoid blocking startup
import('web-vitals').then(({ onCLS, onFID, onFCP, onLCP, onTTFB }) => {
  const reportVitals = (metric) => {
    // In production, send to analytics service
    if (window.__REPORT_VITALS) {
      window.__REPORT_VITALS(metric)
    }
  }
  
  onCLS(reportVitals)
  onFID(reportVitals)
  onFCP(reportVitals)
  onLCP(reportVitals)
  onTTFB(reportVitals)
}).catch(() => {
  // web-vitals not available, skip
})

ReactDOM.createRoot(document.getElementById("root")).render(
  <React.StrictMode>
    <GoogleOAuthProvider clientId={import.meta.env.VITE_GOOGLE_CLIENT_ID || "YOUR_GOOGLE_CLIENT_ID"}>
      <BrowserRouter>
        <AuthProvider>
          <App />
        </AuthProvider>
      </BrowserRouter>
    </GoogleOAuthProvider>
  </React.StrictMode>
);