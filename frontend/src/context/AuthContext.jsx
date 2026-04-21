import { createContext, useContext, useEffect, useState } from "react";
import api, { setupResponseInterceptor } from "../utils/axios";

const AuthContext = createContext();

export const useAuth = () => useContext(AuthContext);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [token, setToken] = useState(localStorage.getItem("token"));
  const [loading, setLoading] = useState(true);

  // ✅ Setup response interceptor on mount (only once)
  useEffect(() => {
    setupResponseInterceptor();
  }, []);

  // ✅ Fetch user data when token changes
  useEffect(() => {
    const fetchUser = async () => {
      if (!token) {
        setLoading(false);
        return;
      }

      try {
        const res = await api.get("/user/me");
        setUser(res.data?.data || res.data);
      } catch (err) {
        // Error handling if needed
      } finally {
        setLoading(false);
      }
    };

    fetchUser();
  }, [token]);

  const login = (data) => {
    localStorage.setItem("token", data.token);
    setToken(data.token);

    // optional
    setUser({
      username: data.username,
      role: data.role,
    });
  };

  const logout = () => {
    localStorage.removeItem("token");
    setToken(null);
    setUser(null);
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        token,
        loading,
        isAuthenticated: !!token,
        login,
        logout,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};


