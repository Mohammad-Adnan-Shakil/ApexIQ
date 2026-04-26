/** @type {import('tailwindcss').Config} */
export default {
  content: ["./index.html", "./src/**/*.{js,ts,jsx,tsx}"],
  theme: {
    extend: {
      colors: {
        bgPrimary: "#0a0a0f",
        bgCard: "#0f0f1a",
        bgElevated: "#141420",
        accentRed: "#e8002d",
        accentGold: "#ffd700",
        whitePrimary: "#ffffff",
        whiteMuted: "rgba(255,255,255,0.6)",
        whiteFaint: "rgba(255,255,255,0.08)",
        borderSoft: "rgba(255,255,255,0.06)",
        successGreen: "#00d4a0",
      },
      fontFamily: {
        display: ['Barlow Condensed', 'sans-serif'],
        mono: ['DM Mono', 'monospace'],
      },
      borderRadius: {
        xl2: "16px",
      },
      boxShadow: {
        card: "0 10px 30px rgba(0,0,0,0.25)",
        redGlow: "0 0 0 1px rgba(232,0,45,0.25), 0 16px 32px rgba(232,0,45,0.15)",
      },
      letterSpacing: {
        section: "0.2em",
      },
    },
  },
  plugins: [],
};

