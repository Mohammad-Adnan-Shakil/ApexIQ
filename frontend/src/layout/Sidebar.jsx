import { motion } from "framer-motion";
import {
  Brain,
  Flag,
  LayoutDashboard,
  LogOut,
  Shield,
  User,
  Users,
  Lock,
  LogIn,
  Gauge,
  Radio,
} from "lucide-react";
import { NavLink, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

const NAV_ITEMS_PUBLIC = [
  { to: "/dashboard", label: "Dashboard", icon: LayoutDashboard },
  { to: "/drivers", label: "Drivers", icon: Users },
  { to: "/races", label: "Races", icon: Flag },
  { to: "/constructors", label: "Constructors", icon: Shield },
];

const NAV_ITEMS_PROTECTED = [
  { to: "/telemetry", label: "Telemetry", icon: Gauge },
  { to: "/race-engineer", label: "Race Engineer", icon: Radio },
];

const Sidebar = ({ mobileOpen = false, onNavigate = () => {} }) => {
  const navigate = useNavigate();
  const { logout, isAuthenticated, user } = useAuth();

  const handleAIClick = () => {
    navigate("/ai");
    onNavigate();
  };

  return (
    <>
      {mobileOpen ? (
        <button
          className="fixed inset-0 z-40 bg-black/50 lg:hidden"
          onClick={onNavigate}
          aria-label="Close navigation"
        />
      ) : null}

      <aside
        className={`
          fixed inset-y-0 left-0 z-50 border-r border-borderSoft bg-bgPrimary
          transition-transform duration-200 lg:sticky lg:top-0 lg:h-screen
          ${mobileOpen ? "translate-x-0" : "-translate-x-full lg:translate-x-0"}
          w-[240px] lg:w-[84px] xl:w-[240px]
        `}
      >
        <div className="flex h-full flex-col justify-between">
          <div>
            <div className="border-b border-borderSoft px-5 py-6 lg:px-4 xl:px-5">
              <div className="flex items-center gap-2 justify-center lg:justify-center xl:justify-start">
                <svg width="26" height="26" viewBox="0 0 28 28" fill="none">
                  <polygon points="14,2 26,26 2,26" fill="none" 
                    stroke="#EF4444" strokeWidth="2.5" strokeLinejoin="round"/>
                  <line x1="14" y1="10" x2="14" y2="20" 
                    stroke="#EF4444" strokeWidth="1.5" strokeLinecap="round"/>
                </svg>
                <span className="text-red-500 font-black tracking-widest text-xl">DELTA</span>
                <span className="text-white font-black tracking-widest text-xl">BOX</span>
              </div>
            </div>

            <nav className="px-3 py-4">
              {/* Public Navigation Items */}
              {NAV_ITEMS_PUBLIC.map(({ to, label, icon: Icon }) => (
                <NavLink key={to} to={to} onClick={onNavigate} className="group relative mb-1 block">
                  {({ isActive }) => (
                    <div
                      className={`
                        flex items-center gap-3 rounded-xl2 px-3 py-3 text-sm transition-all duration-200
                        ${isActive ? "bg-accentRed/10 text-whitePrimary" : "text-whiteMuted hover:bg-white/5 hover:text-whitePrimary"}
                      `}
                    >
                      <Icon className="h-4 w-4 shrink-0" />
                      <span className="font-display font-semibold uppercase tracking-wide lg:hidden xl:inline">{label}</span>

                      {isActive ? (
                        <motion.div
                          layoutId="active-nav-underline"
                          className="absolute bottom-1 left-3 right-3 h-[2px] rounded-full bg-accentRed"
                        />
                      ) : null}

                      {isActive ? <span className="absolute left-0 top-2 h-7 w-[3px] rounded-r-full bg-accentRed" /> : null}
                    </div>
                  )}
                </NavLink>
              ))}

              {/* Protected Navigation Items */}
              {/* AI Prediction - Special handling */}
              <button
                onClick={handleAIClick}
                className={`
                  group relative mb-1 w-full text-left
                  flex items-center gap-3 rounded-xl2 px-3 py-3 text-sm transition-all duration-200
                  ${isAuthenticated ? "text-whiteMuted hover:bg-white/5 hover:text-whitePrimary" : "text-whiteMuted/60 hover:bg-white/5"}
                `}
              >
                <Brain className="h-4 w-4 shrink-0" />
                <span className="font-display font-semibold uppercase tracking-wide lg:hidden xl:inline">AI Prediction</span>
                {!isAuthenticated && <Lock className="h-3 w-3 ml-auto lg:ml-0 xl:ml-auto" />}
              </button>

              {/* Telemetry and Race Engineer - Only show if authenticated */}
              {isAuthenticated && NAV_ITEMS_PROTECTED.map(({ to, label, icon: Icon }) => (
                <NavLink key={to} to={to} onClick={onNavigate} className="group relative mb-1 block">
                  {({ isActive }) => (
                    <div
                      className={`
                        flex items-center gap-3 rounded-xl2 px-3 py-3 text-sm transition-all duration-200
                        ${isActive ? "bg-accentRed/10 text-whitePrimary" : "text-whiteMuted hover:bg-white/5 hover:text-whitePrimary"}
                      `}
                    >
                      <Icon className="h-4 w-4 shrink-0" />
                      <span className="font-display font-semibold uppercase tracking-wide lg:hidden xl:inline">{label}</span>

                      {isActive ? (
                        <motion.div
                          layoutId="active-nav-underline"
                          className="absolute bottom-1 left-3 right-3 h-[2px] rounded-full bg-accentRed"
                        />
                      ) : null}

                      {isActive ? <span className="absolute left-0 top-2 h-7 w-[3px] rounded-r-full bg-accentRed" /> : null}
                    </div>
                  )}
                </NavLink>
              ))}

              {/* Profile - Only show if authenticated */}
              {isAuthenticated && (
                <NavLink to="/profile" onClick={onNavigate} className="group relative mb-1 block">
                  {({ isActive }) => (
                    <div
                      className={`
                        flex items-center gap-3 rounded-xl2 px-3 py-3 text-sm transition-all duration-200
                        ${isActive ? "bg-accentRed/10 text-whitePrimary" : "text-whiteMuted hover:bg-white/5 hover:text-whitePrimary"}
                      `}
                    >
                      <User className="h-4 w-4 shrink-0" />
                      <span className="font-display font-semibold uppercase tracking-wide lg:hidden xl:inline">Profile</span>

                      {isActive ? (
                        <motion.div
                          layoutId="active-nav-underline"
                          className="absolute bottom-1 left-3 right-3 h-[2px] rounded-full bg-accentRed"
                        />
                      ) : null}

                      {isActive ? <span className="absolute left-0 top-2 h-7 w-[3px] rounded-r-full bg-accentRed" /> : null}
                    </div>
                  )}
                </NavLink>
              )}
            </nav>
          </div>

          <div className="border-t border-borderSoft p-3">
            {isAuthenticated ? (
              <>
                {/* User Info */}
                <div className="px-3 py-2 mb-2 rounded-lg bg-white/5 lg:hidden xl:block">
                  <p className="text-xs text-whiteMuted truncate">Signed in as:</p>
                  <p className="text-sm font-medium text-whitePrimary truncate">{user?.username || "User"}</p>
                </div>

                {/* Logout Button */}
                <button
                  onClick={() => {
                    logout();
                    navigate("/dashboard");
                  }}
                  className="flex w-full items-center gap-3 rounded-xl2 px-3 py-3 text-left text-sm text-accentRed transition hover:bg-accentRed/10"
                >
                  <LogOut className="h-4 w-4" />
                  <span className="font-display font-semibold uppercase tracking-wide lg:hidden xl:inline">Logout</span>
                </button>
              </>
            ) : (
              <>
                {/* Sign In Button */}
                <button
                  onClick={() => navigate("/login")}
                  className="flex w-full items-center gap-3 rounded-xl2 px-3 py-3 text-left text-sm bg-accentRed text-white font-medium transition hover:bg-accentRed/90 mb-2"
                >
                  <LogIn className="h-4 w-4" />
                  <span className="font-display font-semibold uppercase tracking-wide lg:hidden xl:inline">Sign In</span>
                </button>

                {/* Sign Up Button */}
                <button
                  onClick={() => navigate("/register")}
                  className="flex w-full items-center gap-3 rounded-xl2 px-3 py-3 text-left text-sm border border-borderSoft text-whiteMuted font-medium transition hover:bg-white/5"
                >
                  <User className="h-4 w-4" />
                  <span className="font-display font-semibold uppercase tracking-wide lg:hidden xl:inline">Sign Up</span>
                </button>
              </>
            )}
          </div>
        </div>
      </aside>
    </>
  );
};

export default Sidebar;
