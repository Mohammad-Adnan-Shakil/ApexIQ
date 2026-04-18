import { Routes, Route, Navigate } from "react-router-dom";
import ProtectedRoute from "./routes/ProtectedRoute";
import MainLayout from "./layout/MainLayout";

// Pages
import Login from "./pages/Login";
import Register from "./pages/Register";
import Dashboard from "./pages/Dashboard";
import AIPage from "./pages/AIPage";
import Drivers from "./pages/Drivers";
import Races from "./pages/Races";
import Constructors from "./pages/Teams";
import Profile from "./pages/Profile";

function App() {
  return (
    <Routes>

      {/* Public Routes */}
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<Register />} />

      {/* Protected Routes with Layout */}
      <Route
        path="/dashboard"
        element={
          <ProtectedRoute>
            <MainLayout>
              <Dashboard />
            </MainLayout>
          </ProtectedRoute>
        }
      />

      <Route
        path="/ai"
        element={
          <ProtectedRoute>
            <MainLayout>
              <AIPage />
            </MainLayout>
          </ProtectedRoute>
        }
      />

      <Route
        path="/drivers"
        element={
          <ProtectedRoute>
            <MainLayout>
              <Drivers />
            </MainLayout>
          </ProtectedRoute>
        }
      />

      <Route
        path="/races"
        element={
          <ProtectedRoute>
            <MainLayout>
              <Races />
            </MainLayout>
          </ProtectedRoute>
        }
      />

      <Route
        path="/constructors"
        element={
          <ProtectedRoute>
            <MainLayout>
              <Constructors />
            </MainLayout>
          </ProtectedRoute>
        }
      />

      <Route
        path="/profile"
        element={
          <ProtectedRoute>
            <MainLayout>
              <Profile />
            </MainLayout>
          </ProtectedRoute>
        }
      />

      {/* Default */}
      <Route path="/" element={<Navigate to="/dashboard" />} />

      {/* Fallback */}
      <Route path="*" element={<Navigate to="/dashboard" />} />

    </Routes>
  );
}

export default App;