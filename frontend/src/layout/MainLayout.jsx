import { AnimatePresence, motion } from "framer-motion";
import { Menu } from "lucide-react";
import { useState } from "react";
import { useLocation } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import Sidebar from "./Sidebar";

const MainLayout = ({ children }) => {
  const { user } = useAuth();
  const [mobileNavOpen, setMobileNavOpen] = useState(false);
  const location = useLocation();

  return (
    <div className="min-h-screen bg-bgPrimary text-whitePrimary">
      <div className="lg:hidden flex h-14 items-center justify-between border-b border-borderSoft px-4 sticky top-0 bg-bgPrimary z-50">
        <button
          className="rounded-lg border border-borderSoft bg-bgElevated p-2 text-whiteMuted"
          onClick={() => setMobileNavOpen((prev) => !prev)}
          aria-label="Toggle menu"
        >
          <Menu className="h-4 w-4" />
        </button>
        <div className="flex items-center gap-2 flex-nowrap whitespace-nowrap">
          <svg width="26" height="26" viewBox="0 0 28 28" fill="none">
            <polygon points="14,2 26,26 2,26" fill="none" 
              stroke="#EF4444" strokeWidth="2.5" strokeLinejoin="round"/>
            <line x1="14" y1="10" x2="14" y2="20" 
              stroke="#EF4444" strokeWidth="1.5" strokeLinecap="round"/>
          </svg>
          <span className="text-red-500 font-black tracking-widest text-xl">DELTA</span>
          <span className="text-white font-black tracking-widest text-xl">BOX</span>
        </div>
        <p className="text-xs text-whiteMuted">{user?.username || "User"}</p>
      </div>

      <div className="flex">
        <Sidebar mobileOpen={mobileNavOpen} onNavigate={() => setMobileNavOpen(false)} />

        <main className="min-w-0 flex-1">
          <div className="hidden h-16 items-center justify-end border-b border-borderSoft px-6 lg:flex sticky top-0 bg-bgPrimary z-50">
            <p className="text-sm text-whiteMuted">{user?.username || "User"}</p>
          </div>

          <AnimatePresence mode="wait">
            <motion.div
              key={location.pathname}
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              exit={{ opacity: 0 }}
              transition={{ duration: 0.2 }}
              className="px-4 py-6 md:px-6"
            >
              {children}
            </motion.div>
          </AnimatePresence>
        </main>
      </div>
    </div>
  );
};

export default MainLayout;

