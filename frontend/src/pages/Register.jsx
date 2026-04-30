import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import api from "../utils/axios";
import { motion } from "framer-motion";
import { GoogleLogin } from "@react-oauth/google";

const Register = () => {
  useEffect(() => {
    document.title = "Register | DeltaBox";
  }, []);

  const navigate = useNavigate();
  const { login } = useAuth();

  const [form, setForm] = useState({
    username: "",
    email: "",
    password: "",
  });

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError("");
    setSuccess("");

    try {
      await api.post("/auth/register", form);
      setSuccess("Account created. Redirecting...");
      setTimeout(() => navigate("/login"), 1500);
    } catch (err) {
      setError("Registration failed");
    } finally {
      setLoading(false);
    }
  };

  const handleGoogleLogin = async (credentialResponse) => {
    setLoading(true);
    setError("");

    try {
      const res = await api.post("/auth/google", {
        idToken: credentialResponse.credential,
      });
      login(res.data);
      navigate("/dashboard");
    } catch (err) {
      setError(err.response?.data?.message || "Google authentication failed");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-b from-gray-950 via-gray-900 to-black flex items-center justify-center p-4">

      <motion.div
        className="w-full max-w-md"
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.3 }}
      >
        <div className="mb-8 flex justify-center">
          <svg width="48" height="48" viewBox="0 0 28 28" fill="none">
            <polygon points="14,2 26,26 2,26" fill="none" 
              stroke="#EF4444" strokeWidth="2.5" strokeLinejoin="round"/>
            <line x1="14" y1="10" x2="14" y2="20" 
              stroke="#EF4444" strokeWidth="1.5" strokeLinecap="round"/>
          </svg>
        </div>

        <div className="border border-red-900/40 bg-gray-900/80 backdrop-blur rounded-2xl p-8">
          <h2 className="text-3xl font-black text-white text-center mb-2 tracking-widest">
            <span className="text-red-500">DELTA</span><span className="text-white">BOX</span>
          </h2>
          <p className="text-gray-500 text-sm text-center mb-8 tracking-widest uppercase">
            Create your account
          </p>

          <form className="space-y-6" onSubmit={handleSubmit}>
            <div>
              <label className="block text-gray-400 text-xs tracking-widest uppercase mb-2">
                Username
              </label>
              <input
                name="username"
                placeholder="Enter username"
                className="w-full bg-gray-800/60 border border-gray-700 text-white placeholder-gray-500 rounded-lg px-4 py-3 focus:outline-none focus:border-red-500 transition-all duration-200"
                onChange={handleChange}
              />
            </div>

            <div>
              <label className="block text-gray-400 text-xs tracking-widest uppercase mb-2">
                Email
              </label>
              <input
                name="email"
                type="email"
                placeholder="Enter email"
                className="w-full bg-gray-800/60 border border-gray-700 text-white placeholder-gray-500 rounded-lg px-4 py-3 focus:outline-none focus:border-red-500 transition-all duration-200"
                onChange={handleChange}
              />
            </div>

            <div>
              <label className="block text-gray-400 text-xs tracking-widest uppercase mb-2">
                Password
              </label>
              <input
                name="password"
                type="password"
                placeholder="Enter password"
                className="w-full bg-gray-800/60 border border-gray-700 text-white placeholder-gray-500 rounded-lg px-4 py-3 focus:outline-none focus:border-red-500 transition-all duration-200"
                onChange={handleChange}
              />
            </div>

            {error && <p className="text-red-500 text-sm">{error}</p>}
            {success && <p className="text-green-500 text-sm">{success}</p>}

            <button
              className="w-full bg-red-600 hover:bg-red-700 text-white font-bold py-3 rounded-lg tracking-widest transition-all duration-200"
              disabled={loading}
            >
              {loading ? "CREATING..." : "CREATE ACCOUNT"}
            </button>

            <div className="relative">
              <div className="absolute inset-0 flex items-center">
                <div className="w-full border-t border-gray-700"></div>
              </div>
              <div className="relative flex justify-center text-sm">
                <span className="px-2 bg-gray-900 text-gray-500">or continue with</span>
              </div>
            </div>

            <div className="flex justify-center">
              <GoogleLogin
                onSuccess={handleGoogleLogin}
                onError={() => setError("Google login failed")}
                useOneTap
                theme="filled_black"
                text="signup_with"
                shape="rectangular"
                width="100%"
                disabled={loading}
              />
            </div>
          </form>

          <p className="text-center text-gray-500 text-sm mt-6">
            Already have an account?{" "}
            <button
              onClick={() => navigate("/login")}
              className="text-red-500 hover:text-red-400 font-semibold"
            >
              Sign in
            </button>
          </p>
        </div>
      </motion.div>
    </div>
  );
};

export default Register;