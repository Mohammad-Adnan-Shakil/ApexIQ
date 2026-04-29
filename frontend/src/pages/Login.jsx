import { useState, useEffect } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import api from "../utils/axios";
import { Eye, EyeOff } from "lucide-react";

const Login = () => {
  useEffect(() => {
    document.title = "Login | DeltaBox";
  }, []);

  const navigate = useNavigate();
  const location = useLocation();
  const { login } = useAuth();

  const [isLogin, setIsLogin] = useState(true);

  // Login form state
  const [loginForm, setLoginForm] = useState({
    email: "",
    password: "",
  });

  // Register form state
  const [registerForm, setRegisterForm] = useState({
    name: "",
    email: "",
    password: "",
    confirmPassword: "",
  });

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);

  const handleLoginChange = (e) => {
    setLoginForm({ ...loginForm, [e.target.name]: e.target.value });
  };

  const handleRegisterChange = (e) => {
    setRegisterForm({ ...registerForm, [e.target.name]: e.target.value });
  };

  const handleLoginSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError("");

    try {
      const res = await api.post("/auth/login", loginForm);
      login(res.data);
      
      // Redirect to the page user was trying to access, or dashboard
      const from = location.state?.from || "/dashboard";
      navigate(from);
    } catch (err) {
      setError("Invalid email or password. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  const handleRegisterSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError("");
    setSuccess("");

    if (registerForm.password !== registerForm.confirmPassword) {
      setError("Passwords do not match");
      setLoading(false);
      return;
    }

    try {
      await api.post("/auth/register", {
        username: registerForm.name,
        email: registerForm.email,
        password: registerForm.password,
      });
      setSuccess("Account created successfully! Redirecting to login...");
      setTimeout(() => {
        setIsLogin(true);
        setRegisterForm({ name: "", email: "", password: "", confirmPassword: "" });
        setSuccess("");
      }, 1500);
    } catch (err) {
      setError(err.response?.data?.message || "Registration failed");
    } finally {
      setLoading(false);
    }
  };

  const inputBaseStyle = {
    width: "100%",
    boxSizing: "border-box",
    padding: "14px 16px",
    backgroundColor: "rgba(255, 255, 255, 0.04)",
    border: "1px solid rgba(255, 255, 255, 0.1)",
    borderRadius: "10px",
    color: "white",
    fontSize: "15px",
    fontFamily: "inherit",
    transition: "all 0.2s",
    outline: "none",
    minHeight: "44px",
  };

  const inputFocusStyle = {
    ...inputBaseStyle,
    borderColor: "rgba(232, 0, 45, 0.6)",
    boxShadow: "0 0 0 3px rgba(232, 0, 45, 0.1)",
  };

  return (
    <div style={{ display: "flex", flexDirection: "column", minHeight: "100vh", backgroundColor: "#0a0a0f", backgroundImage: "radial-gradient(ellipse at top left, rgba(232, 0, 45, 0.08), transparent 50%), radial-gradient(ellipse at bottom right, rgba(255, 255, 255, 0.03), transparent 50%)" }}>
      <style>{`
        @media (max-width: 767px) {
          .login-container { flex-direction: column; }
          .login-left { display: none; }
          .login-right { width: 100%; padding: 24px; }
          .login-right > div { maxWidth: 100%; padding: 24px; }
          .mobile-logo { display: flex !important; }
        }

        @media (min-width: 768px) and (max-width: 1023px) {
          .login-container { flex-direction: row; }
          .login-left { display: none; }
          .login-right { width: 100%; padding: 32px; }
          .mobile-logo { display: flex !important; }
        }

        @media (min-width: 1024px) {
          .login-container { flex-direction: row; }
          .login-left { width: 50%; }
          .login-right { width: 50%; padding: 48px; }
          .mobile-logo { display: none; }
        }

        html, body, #root {
          height: 100%;
          margin: 0;
          padding: 0;
        }
      `}</style>

      <div style={{ display: "flex", flex: 1 }} className="login-container">
        {/* LEFT PANEL - BRANDING */}
        <div
          className="login-left"
          style={{
            display: "flex",
            width: "50%",
            backgroundColor: "#0f0f1a",
            borderRight: "2px solid rgba(232, 0, 45, 0.3)",
            alignItems: "center",
            justifyContent: "center",
            position: "relative",
            backgroundImage: "linear-gradient(135deg, rgba(232, 0, 45, 0.1) 0%, transparent 50%), linear-gradient(45deg, transparent 50%, rgba(232, 0, 45, 0.05) 100%)",
          }}
        >
          <div
            className="login-left-content"
            style={{
              display: "flex",
              flexDirection: "column",
              alignItems: "center",
              textAlign: "center",
            }}
          >
            {/* DeltaBox Logo */}
            <div style={{ display: "flex", alignItems: "baseline", gap: 0, letterSpacing: "-3px" }}>
              <h1 className="logo-text" style={{ fontSize: "80px", fontWeight: 900, color: "#e8002d", margin: 0, textShadow: "0 0 40px rgba(232, 0, 45, 0.5)" }}>
                Delta
              </h1>
              <h1 className="logo-text" style={{ fontSize: "80px", fontWeight: 900, color: "white", margin: 0, textShadow: "0 0 40px rgba(255, 255, 255, 0.3)" }}>
                Box
              </h1>
            </div>

            {/* Tagline */}
            <div style={{ marginTop: "24px" }}>
              <p style={{ fontSize: "20px", fontWeight: 500, color: "white", margin: 0 }}>AI-Powered F1 Intelligence</p>
            </div>

            {/* Feature Pills */}
            <div
              className="feature-pills"
              style={{
                marginTop: "40px",
                display: "flex",
                flexWrap: "wrap",
                gap: "12px",
                justifyContent: "center",
              }}
            >
              <div style={{ padding: "10px 20px", borderRadius: "999px", border: "1px solid rgba(232, 0, 45, 0.5)", backgroundColor: "rgba(232, 0, 45, 0.15)", color: "white", fontSize: "13px", fontWeight: 600, boxShadow: "0 0 20px rgba(232, 0, 45, 0.3)" }}>
                Race Prediction
              </div>
              <div style={{ padding: "10px 20px", borderRadius: "999px", border: "1px solid rgba(232, 0, 45, 0.5)", backgroundColor: "rgba(232, 0, 45, 0.15)", color: "white", fontSize: "13px", fontWeight: 600, boxShadow: "0 0 20px rgba(232, 0, 45, 0.3)" }}>
                Performance Insights
              </div>
              <div style={{ padding: "10px 20px", borderRadius: "999px", border: "1px solid rgba(232, 0, 45, 0.5)", backgroundColor: "rgba(232, 0, 45, 0.15)", color: "white", fontSize: "13px", fontWeight: 600, boxShadow: "0 0 20px rgba(232, 0, 45, 0.3)" }}>
                What-if Simulation
              </div>
            </div>

            {/* Tagline below feature pills */}
            <p style={{ marginTop: "32px", fontSize: "14px", color: "rgba(255,255,255,0.5)", letterSpacing: "1px" }}>
              Your Edge in Formula 1 Analytics
            </p>

            {/* Bottom Footer Text */}
            <p
              className="metadata-footer"
              style={{
                position: "absolute",
                bottom: "32px",
                fontSize: "12px",
                color: "rgba(255,255,255,0.3)",
                margin: 0,
              }}
            >
              DeltaBox · 2026 Season
            </p>
          </div>
        </div>

        {/* RIGHT PANEL - LOGIN/REGISTER FORM */}
        <div
          className="login-right"
          style={{
            width: "50%",
            display: "flex",
            alignItems: "center",
            justifyContent: "center",
            padding: "48px 24px",
            backgroundColor: "rgba(15, 15, 26, 0.5)",
          }}
        >
          <div style={{ width: "100%", maxWidth: "400px", padding: "40px", backgroundColor: "rgba(15, 15, 26, 0.8)", border: "1px solid rgba(255, 255, 255, 0.08)", borderRadius: "20px", backdropFilter: "blur(10px)" }}>
            {/* Mobile Logo - Only shows on mobile/tablet */}
            <div className="mobile-logo" style={{ display: "none", alignItems: "baseline", gap: 0, letterSpacing: "-3px", justifyContent: "center", marginBottom: "24px" }}>
              <h1 style={{ fontSize: "48px", fontWeight: 900, color: "#e8002d", margin: 0, textShadow: "0 0 40px rgba(232, 0, 45, 0.5)" }}>
                Delta
              </h1>
              <h1 style={{ fontSize: "48px", fontWeight: 900, color: "white", margin: 0, textShadow: "0 0 40px rgba(255, 255, 255, 0.3)" }}>
                Box
              </h1>
            </div>

            {/* Header */}
            <p style={{ fontSize: "11px", textTransform: "uppercase", letterSpacing: "3px", color: "rgba(255,255,255,0.4)", fontWeight: 600, margin: 0 }}>
              {isLogin ? "Member Access" : "New Member"}
            </p>

            <h2 style={{ marginTop: "8px", fontSize: "32px", fontWeight: 700, color: "white", margin: "8px 0 0 0" }}>
              {isLogin ? "Sign In" : "Create Account"}
            </h2>

            <p style={{ marginTop: "4px", fontSize: "14px", color: "rgba(255,255,255,0.6)", margin: "4px 0 0 0" }}>
              {isLogin ? "Access your F1 intelligence dashboard" : "Join DeltaBox for the 2026 season"}
            </p>

            {/* Form */}
            <form
              onSubmit={isLogin ? handleLoginSubmit : handleRegisterSubmit}
              style={{
                marginTop: "40px",
                display: "flex",
                flexDirection: "column",
                gap: "12px",
              }}
            >
              {/* Register: Full Name Input */}
              {!isLogin && (
                <input
                  type="text"
                  name="name"
                  placeholder="Full name"
                  value={registerForm.name}
                  onChange={handleRegisterChange}
                  required={!isLogin}
                  style={inputBaseStyle}
                  onFocus={(e) => Object.assign(e.target.style, inputFocusStyle)}
                  onBlur={(e) => Object.assign(e.target.style, inputBaseStyle)}
                />
              )}

              {/* Email or Username Input */}
              <input
                type="text"
                name="email"
                placeholder="Email or username"
                value={isLogin ? loginForm.email : registerForm.email}
                onChange={isLogin ? handleLoginChange : handleRegisterChange}
                required
                style={inputBaseStyle}
                onFocus={(e) => Object.assign(e.target.style, inputFocusStyle)}
                onBlur={(e) => Object.assign(e.target.style, inputBaseStyle)}
              />

              {/* Password Input with Toggle */}
              <div style={{ position: "relative", width: "100%" }}>
                <input
                  type={showPassword ? "text" : "password"}
                  name="password"
                  placeholder="Password"
                  value={isLogin ? loginForm.password : registerForm.password}
                  onChange={isLogin ? handleLoginChange : handleRegisterChange}
                  required
                  style={{ ...inputBaseStyle, paddingRight: "48px" }}
                  onFocus={(e) => Object.assign(e.target.style, inputFocusStyle)}
                  onBlur={(e) => Object.assign(e.target.style, inputBaseStyle)}
                />
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  style={{
                    position: "absolute",
                    right: "14px",
                    top: "50%",
                    transform: "translateY(-50%)",
                    background: "none",
                    border: "none",
                    cursor: "pointer",
                    color: "rgba(255,255,255,0.5)",
                    display: "flex",
                    alignItems: "center",
                    padding: "0",
                    zIndex: 10,
                  }}
                >
                  {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                </button>
              </div>

              {/* Register: Confirm Password Input */}
              {!isLogin && (
                <div style={{ position: "relative", width: "100%" }}>
                  <input
                    type={showConfirmPassword ? "text" : "password"}
                    name="confirmPassword"
                    placeholder="Confirm password"
                    value={registerForm.confirmPassword}
                    onChange={handleRegisterChange}
                    required={!isLogin}
                    style={{ ...inputBaseStyle, paddingRight: "48px" }}
                    onFocus={(e) => Object.assign(e.target.style, inputFocusStyle)}
                    onBlur={(e) => Object.assign(e.target.style, inputBaseStyle)}
                  />
                  <button
                    type="button"
                    onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                    style={{
                      position: "absolute",
                      right: "14px",
                      top: "50%",
                      transform: "translateY(-50%)",
                      background: "none",
                      border: "none",
                      cursor: "pointer",
                      color: "rgba(255,255,255,0.5)",
                      display: "flex",
                      alignItems: "center",
                      padding: "0",
                      zIndex: 10,
                    }}
                  >
                    {showConfirmPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                  </button>
                </div>
              )}

              {/* Error Message */}
              {error && (
                <p style={{ marginTop: "12px", textAlign: "center", fontSize: "13px", color: "rgba(232,0,45,0.9)" }}>
                  {error}
                </p>
              )}

              {/* Success Message */}
              {success && (
                <p style={{ marginTop: "12px", textAlign: "center", fontSize: "13px", color: "rgba(76,175,80,0.9)" }}>
                  {success}
                </p>
              )}

              {/* Submit Button */}
              <button
                type="submit"
                disabled={loading}
                style={{
                  marginTop: "24px",
                  width: "100%",
                  padding: "14px",
                  backgroundColor: "#e8002d",
                  color: "white",
                  fontSize: "15px",
                  fontWeight: 700,
                  border: "none",
                  borderRadius: "10px",
                  letterSpacing: "1px",
                  cursor: loading ? "not-allowed" : "pointer",
                  opacity: loading ? 0.75 : 1,
                  transition: "all 0.2s",
                  textTransform: "uppercase",
                }}
                onMouseEnter={(e) => {
                  if (!loading) e.target.style.backgroundColor = "#c8001f";
                }}
                onMouseLeave={(e) => {
                  e.target.style.backgroundColor = "#e8002d";
                }}
                onMouseDown={(e) => {
                  if (!loading) e.target.style.transform = "scale(0.97)";
                }}
                onMouseUp={(e) => {
                  if (!loading) e.target.style.transform = "scale(1)";
                }}
              >
                {loading ? (
                  <div
                    style={{
                      width: "16px",
                      height: "16px",
                      border: "2px solid white",
                      borderTop: "2px solid transparent",
                      borderRadius: "50%",
                      animation: "spin 0.6s linear infinite",
                      margin: "0 auto",
                    }}
                  />
                ) : isLogin ? (
                  "ACCESS DASHBOARD"
                ) : (
                  "CREATE ACCOUNT"
                )}
              </button>
            </form>

            {/* Toggle Button */}
            <button
              type="button"
              onClick={() => {
                setIsLogin(!isLogin);
                setError("");
                setSuccess("");
              }}
              style={{
                width: "100%",
                marginTop: "12px",
                padding: "14px",
                backgroundColor: "transparent",
                border: "1px solid rgba(255,255,255,0.12)",
                borderRadius: "10px",
                color: "rgba(255,255,255,0.7)",
                fontSize: "15px",
                fontWeight: 600,
                letterSpacing: "1px",
                cursor: "pointer",
                transition: "all 0.2s",
                textTransform: "uppercase",
              }}
              onMouseEnter={(e) => {
                e.target.style.borderColor = "rgba(232,0,45,0.5)";
                e.target.style.color = "white";
                e.target.style.backgroundColor = "rgba(232,0,45,0.06)";
              }}
              onMouseLeave={(e) => {
                e.target.style.borderColor = "rgba(255,255,255,0.12)";
                e.target.style.color = "rgba(255,255,255,0.7)";
                e.target.style.backgroundColor = "transparent";
              }}
            >
              {isLogin ? "CREATE ACCOUNT" : "ALREADY HAVE AN ACCOUNT?"}
            </button>

            {/* Bottom Footer */}
            <p style={{ marginTop: "40px", textAlign: "center", fontSize: "12px", color: "rgba(255,255,255,0.2)", margin: "40px 0 0 0" }}>
              DeltaBox · 2026 Season
            </p>
          </div>
        </div>
      </div>

      <style>{`
        @keyframes spin {
          to {
            transform: rotate(360deg);
          }
        }
      `}</style>
    </div>
  );
};

export default Login;