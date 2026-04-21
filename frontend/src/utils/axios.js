import axios from "axios";

const api = axios.create({
  baseURL: "http://localhost:8080/api",
});

// ✅ REQUEST INTERCEPTOR: Add JWT token to every request
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem("token");

    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }

    return config;
  },
  (error) => Promise.reject(error)
);

// ✅ RESPONSE INTERCEPTOR: Handle 401 errors and redirect to login
// This is set up dynamically in AuthProvider to have access to logout & navigate
export const setupResponseInterceptor = () => {
  // Return the interceptor ID so it can be removed/re-added if needed
  return api.interceptors.response.use(
    (response) => response,
    (error) => {
      if (error.response?.status === 401) {
        // Token expired or invalid - clear auth state and redirect
        localStorage.removeItem("token");
        window.location.href = "/login";
      }
      return Promise.reject(error);
    }
  );
};

export default api;