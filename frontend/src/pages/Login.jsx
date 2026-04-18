import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import api from "../utils/axios";
import { motion } from "framer-motion";

const Login = () => {
  const navigate = useNavigate();
  const { login } = useAuth();

  const [form, setForm] = useState({
    email: "",
    password: "",
  });

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError("");

    try {
      const res = await api.post("/auth/login", form);
      login(res.data);
      navigate("/dashboard");
    } catch (err) {
      console.log(err);
      setError("Invalid email or password");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="h-screen flex">

      {/* LEFT */}
      <div className="hidden md:flex w-1/2 bg-background items-center justify-center relative">
        <div className="absolute right-0 top-0 h-full w-[3px] bg-primary"></div>

        <div className="px-12">
          <h1 className="text-7xl">F1 PULSE</h1>

          <div className="racing-divider mt-4 mb-6"></div>

          <p className="text-textSecondary text-lg max-w-md leading-relaxed">
            AI-powered Formula 1 intelligence platform for race prediction,
            performance insights, and strategic simulation.
          </p>
        </div>
      </div>

      {/* RIGHT */}
      <motion.div
        className="w-full md:w-1/2 flex items-center justify-center bg-background px-6"
        initial={{ opacity: 0, x: 50 }}
        animate={{ opacity: 1, x: 0 }}
        transition={{ duration: 0.5 }}
      >
        <form onSubmit={handleSubmit} className="w-full max-w-md space-y-6">

          <h2 className="text-3xl">LOGIN</h2>
          <div className="racing-divider"></div>

          <input
            type="email"
            name="email"
            placeholder="Email"
            value={form.email}
            onChange={handleChange}
            className="input-field"
            required
          />

          <input
            type="password"
            name="password"
            placeholder="Password"
            value={form.password}
            onChange={handleChange}
            className="input-field"
            required
          />

          {error && <p className="text-danger text-sm">{error}</p>}

          <button className="btn-primary w-full" disabled={loading}>
            {loading ? "SIGNING IN..." : "ACCESS DASHBOARD"}
          </button>

          <p className="text-sm text-textSecondary text-center">
            Don’t have an account?{" "}
            <span
              className="text-primary cursor-pointer"
              onClick={() => navigate("/register")}
            >
              Register
            </span>
          </p>

        </form>
      </motion.div>
    </div>
  );
};

export default Login;