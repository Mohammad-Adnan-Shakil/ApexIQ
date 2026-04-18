import Sidebar from "./Sidebar";
import { useAuth } from "../context/AuthContext";
import { motion } from "framer-motion";

const MainLayout = ({ children }) => {
  const { user } = useAuth();

  return (
    <div className="flex h-screen bg-background">

      <Sidebar />

      <div className="flex-1 flex flex-col overflow-hidden">

        <div className="h-16 flex items-center justify-between px-6 border-b border-border">
          <h1 className="font-display text-xl">F1 PULSE</h1>

          <div className="text-sm text-textSecondary">
            {user?.username || "User"}
          </div>
        </div>

        <motion.div
          className="flex-1 overflow-y-auto p-6"
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          transition={{ duration: 0.3 }}
        >
          {children}
        </motion.div>

      </div>
    </div>
  );
};

export default MainLayout;