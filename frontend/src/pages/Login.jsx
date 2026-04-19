import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import api from "../utils/axios";
import { Eye, EyeOff } from "lucide-react";

const Login = () => {
  useEffect(() => {
    document.title = "Login | F1 Pulse";
  }, []);

  const navigate = useNavigate();
  const { login } = useAuth();

  const [form, setForm] = useState({
    email: "",
    password: "",
  });

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [showPassword, setShowPassword] = useState(false);

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError("");

    try {
      const res = await api.post("/auth/login", form);
      console.log("LOGIN RESPONSE:", res.data);
      login(res.data);
      navigate("/dashboard");
    } catch (err) {
      console.log(err);
      setError(err.response?.data?.message || "Invalid email or password");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex flex-col md:flex-row bg-[#0a0a0f]">
      {/* LEFT PANEL - BRANDING (Desktop only) */}
      <div className="hidden md:flex w-[55%] bg-[#0f0f1a] border-r border-white/6 items-center justify-center relative">
        {/* Branding Content */}
        <div className="flex flex-col items-center text-center">
          {/* F1 PULSE Logo */}
          <div className="flex items-baseline gap-0" style={{ letterSpacing: "-2px" }}>
            <h1 className="text-7xl font-black text-[#e8002d]">F1</h1>
            <h1 className="text-7xl font-black text-white">PULSE</h1>
          </div>

          {/* Tagline */}
          <div className="mt-6">
            <p className="text-xl font-medium text-white">AI-Powered Formula 1</p>
            <p className="text-xl font-normal text-white/50">Intelligence Platform</p>
          </div>

          {/* Feature Pills */}
          <div className="mt-10 flex flex-wrap gap-3 justify-center">
            <div className="px-4 py-2 rounded-full border border-white/10 bg-white/6 text-white text-xs">
              Race Prediction
            </div>
            <div className="px-4 py-2 rounded-full border border-white/10 bg-white/6 text-white text-xs">
              Performance Insights
            </div>
            <div className="px-4 py-2 rounded-full border border-white/10 bg-white/6 text-white text-xs">
              What-if Simulation
            </div>
          </div>

          {/* Bottom Footer Text */}
          <p className="absolute bottom-8 text-xs text-white/30">2026 Season · Live Data · PostgreSQL</p>
        </div>
      </div>

      {/* RIGHT PANEL - LOGIN FORM */}
      <div className="w-full md:w-[45%] flex items-center justify-center p-6 md:p-12">
        <div className="w-full max-w-sm">
          {/* Member Access Label */}
          <p className="text-[11px] uppercase tracking-widest text-white/40 font-semibold">Member Access</p>

          {/* Sign In Heading */}
          <h2 className="mt-2 text-4xl font-bold text-white">Sign In</h2>

          {/* Subtext */}
          <p className="mt-1 text-sm text-white/60">Access your F1 intelligence dashboard</p>

          {/* Form */}
          <form onSubmit={handleSubmit} className="mt-10 space-y-3">
            {/* Email Input */}
            <input
              type="email"
              name="email"
              placeholder="Email address"
              value={form.email}
              onChange={handleChange}
              required
              className="w-full px-4 py-3 bg-white/4 border border-white/10 rounded-xl text-white placeholder-white/30 focus:outline-none focus:border-[#e8002d]/60 focus:ring-3 focus:ring-[#e8002d]/10 transition-all text-sm"
            />

            {/* Password Input with Toggle */}
            <div className="relative">
              <input
                type={showPassword ? "text" : "password"}
                name="password"
                placeholder="Password"
                value={form.password}
                onChange={handleChange}
                required
                className="w-full px-4 py-3 bg-white/4 border border-white/10 rounded-xl text-white placeholder-white/30 focus:outline-none focus:border-[#e8002d]/60 focus:ring-3 focus:ring-[#e8002d]/10 transition-all text-sm pr-10"
              />
              <button
                type="button"
                onClick={() => setShowPassword(!showPassword)}
                className="absolute right-3 top-1/2 -translate-y-1/2 text-white/40 hover:text-white/60 transition-colors"
              >
                {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
              </button>
            </div>

            {/* Error Message */}
            {error && (
              <p className="mt-3 text-center text-sm text-[#e8002d]/90">{error}</p>
            )}

            {/* Submit Button */}
            <button
              type="submit"
              disabled={loading}
              className="w-full mt-6 px-4 py-3 bg-[#e8002d] text-white font-bold text-sm uppercase tracking-wider rounded-xl hover:bg-[#c8001f] active:scale-95 hover:scale-99 transition-all duration-200 disabled:opacity-75 disabled:cursor-not-allowed"
            >
              {loading ? (
                <span className="flex items-center justify-center gap-2">
                  <div className="w-4 h-4 border-2 border-t-2 border-white border-t-transparent rounded-full animate-spin"></div>
                </span>
              ) : (
                "ACCESS DASHBOARD"
              )}
            </button>
          </form>

          {/* Bottom Footer */}
          <p className="mt-10 text-center text-xs text-white/20">F1 Pulse · 2026 Season</p>
        </div>
      </div>
    </div>
  );
};

export default Login;