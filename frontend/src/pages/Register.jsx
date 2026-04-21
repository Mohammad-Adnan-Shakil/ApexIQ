import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import api from "../utils/axios";
import { motion } from "framer-motion";

const Register = () => {
  useEffect(() => {
    document.title = "Register | ApexIQ";
  }, []);

  const navigate = useNavigate();

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

  return (
    <div className="h-screen flex">

      <div className="hidden md:flex w-1/2 bg-background items-center justify-center relative">
        <div className="absolute right-0 top-0 h-full w-[3px] bg-primary"></div>

        <div className="px-12">
          <h1 className="text-7xl">APEX IQ</h1>
          <div className="racing-divider mt-4 mb-6"></div>
          <p className="text-textSecondary text-lg max-w-md leading-relaxed">
            Join the future of F1 analytics and intelligence.
          </p>
        </div>
      </div>

      <motion.div
        className="w-full md:w-1/2 flex items-center justify-center px-6"
        initial={{ opacity: 0, x: 50 }}
        animate={{ opacity: 1, x: 0 }}
      >
        <form className="w-full max-w-md space-y-6" onSubmit={handleSubmit}>
          <h2 className="text-3xl">REGISTER</h2>
          <div className="racing-divider"></div>

          <input name="username" placeholder="Username" className="input-field" onChange={handleChange} />
          <input name="email" placeholder="Email" className="input-field" onChange={handleChange} />
          <input type="password" name="password" placeholder="Password" className="input-field" onChange={handleChange} />

          {error && <p className="text-danger">{error}</p>}
          {success && <p className="text-success">{success}</p>}

          <button className="btn-primary w-full">
            {loading ? "CREATING..." : "CREATE ACCOUNT"}
          </button>
        </form>
      </motion.div>
    </div>
  );
};

export default Register;