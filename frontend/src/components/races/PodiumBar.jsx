import { motion } from "framer-motion";
import { Trophy } from "lucide-react";
import { nationalityFlag } from "../../utils/formatters";

const PodiumBar = ({ position, driver, delay = 0 }) => {
  // Position color mapping
  const colorMap = {
    1: { bg: "from-yellow-500 to-amber-600", border: "border-yellow-500/50", text: "text-yellow-300", label: "gold", barBg: "bg-gradient-to-b from-yellow-500/20 to-yellow-600/10" },
    2: { bg: "from-gray-300 to-gray-500", border: "border-gray-400/50", text: "text-gray-200", label: "silver", barBg: "bg-gradient-to-b from-gray-400/20 to-gray-500/10" },
    3: { bg: "from-orange-400 to-orange-600", border: "border-orange-400/50", text: "text-orange-200", label: "bronze", barBg: "bg-gradient-to-b from-orange-400/20 to-orange-600/10" },
  };

  const colors = colorMap[position] || colorMap[3];
  
  // Bar height based on position
  const heightMap = {
    1: "h-48",
    2: "h-36",
    3: "h-28",
  };

  const height = heightMap[position] || heightMap[3];

  return (
    <motion.div
      initial={{ opacity: 0, y: 40 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{
        delay: delay * 0.15,
        duration: 0.6,
        ease: "easeOut",
      }}
      whileHover={{ scale: 1.05 }}
      className="flex flex-col items-center gap-4"
    >
      {/* Driver Card */}
      <motion.div
        initial={{ scale: 0.8, opacity: 0 }}
        animate={{ scale: 1, opacity: 1 }}
        transition={{
          delay: delay * 0.15 + 0.2,
          duration: 0.4,
        }}
        className="text-center"
      >
        <div className={`inline-block rounded-full border-2 ${colors.border} p-3 backdrop-blur-sm`}>
          <div className={`bg-gradient-to-br ${colors.bg} rounded-full h-16 w-16 flex items-center justify-center shadow-lg`}>
            <span className={`text-2xl font-bold ${colors.text}`}>{driver.code}</span>
          </div>
          {position === 1 && (
            <motion.div
              initial={{ rotate: -20, opacity: 0 }}
              animate={{ rotate: 0, opacity: 1 }}
              transition={{ delay: delay * 0.15 + 0.4, duration: 0.5 }}
              className="absolute -top-2 left-1/2 -translate-x-1/2"
            >
              <Trophy className="h-6 w-6 text-yellow-400 drop-shadow-lg" />
            </motion.div>
          )}
        </div>

        <div className="mt-3">
          <p className={`font-semibold text-sm ${colors.text}`}>{driver.name || `Driver ${position}`}</p>
          <p className="text-xs text-gray-400 flex items-center justify-center gap-1 mt-1">
            {nationalityFlag(driver.nationality)}
            <span className="uppercase tracking-wide">{driver.nationality || "Unknown"}</span>
          </p>
        </div>
      </motion.div>

      {/* Podium Bar */}
      <motion.div
        initial={{ height: 0, opacity: 0 }}
        animate={{ height: "auto", opacity: 1 }}
        transition={{
          delay: delay * 0.15 + 0.3,
          duration: 0.7,
          ease: "easeOut",
        }}
        className={`${height} ${colors.barBg} rounded-t-2xl border border-gray-600/30 backdrop-blur-sm shadow-lg relative overflow-hidden group hover:shadow-xl transition-shadow`}
        style={{
          minWidth: "96px",
          background: `linear-gradient(180deg, rgba(255,255,255,0.05) 0%, rgba(0,0,0,0.1) 100%), linear-gradient(to bottom, ${position === 1 ? "rgb(234,179,8)" : position === 2 ? "rgb(209,213,219)" : "rgb(251,146,60)"}, transparent)`,
        }}
      >
        {/* Reflective overlay */}
        <div className="absolute inset-0 bg-gradient-to-b from-white/10 to-transparent pointer-events-none" />
        
        {/* Position label at bottom */}
        <div className="absolute bottom-2 inset-x-0 flex items-center justify-center">
          <span className={`text-xs font-bold uppercase tracking-widest ${colors.text} drop-shadow`}>
            P{position}
          </span>
        </div>
      </motion.div>

      {/* Points Display */}
      <motion.div
        initial={{ opacity: 0, y: -10 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{
          delay: delay * 0.15 + 0.5,
          duration: 0.4,
        }}
        className="text-center"
      >
        <p className={`text-lg font-bold font-mono ${colors.text}`}>{driver.points} pts</p>
      </motion.div>
    </motion.div>
  );
};

export default PodiumBar;
