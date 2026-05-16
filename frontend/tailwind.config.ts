import type { Config } from "tailwindcss";

export default {
  content: ["./index.html", "./src/**/*.{ts,tsx}"],
  theme: {
    extend: {
      colors: {
        canvas: "#f4f6f1",
        ink: "#0f172a",
        mist: "#e5ebdf",
        accent: "#0f766e",
        accentSoft: "#d7f2ee",
        warning: "#b45309",
        danger: "#b91c1c",
      },
      boxShadow: {
        panel: "0 20px 50px rgba(15, 23, 42, 0.08)",
      },
      backgroundImage: {
        "hero-glow":
          "radial-gradient(circle at top left, rgba(15,118,110,0.22), transparent 38%), radial-gradient(circle at bottom right, rgba(217,119,6,0.15), transparent 32%)",
      },
    },
  },
  plugins: [],
} satisfies Config;
