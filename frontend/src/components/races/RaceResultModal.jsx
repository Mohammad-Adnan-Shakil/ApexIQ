import { useState, useEffect } from "react";
import { motion, AnimatePresence } from "framer-motion";
import { X, Flag } from "lucide-react";
import { nationalityFlag } from "../../utils/formatters";
import PodiumBar from "./PodiumBar";

const RaceResultModal = ({ isOpen, onClose, race, results = [] }) => {
  const [podiumData, setPodiumData] = useState([]);

  useEffect(() => {
    if (results && results.length >= 3) {
      setPodiumData(results.slice(0, 3));
    }
  }, [results]);

  // Close on ESC key
  useEffect(() => {
    if (!isOpen) return;

    const handleKeyDown = (e) => {
      if (e.key === "Escape") {
        onClose();
      }
    };

    window.addEventListener("keydown", handleKeyDown);
    return () => window.removeEventListener("keydown", handleKeyDown);
  }, [isOpen, onClose]);

  if (!race) return null;

  return (
    <AnimatePresence>
      {isOpen && (
        <>
          {/* Backdrop */}
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            transition={{ duration: 0.2 }}
            onClick={onClose}
            className="fixed inset-0 bg-black/80 backdrop-blur-sm z-40"
          />

          {/* Modal */}
          <motion.div
            initial={{ opacity: 0, scale: 0.9, y: 20 }}
            animate={{ opacity: 1, scale: 1, y: 0 }}
            exit={{ opacity: 0, scale: 0.9, y: 20 }}
            transition={{ duration: 0.3, ease: "easeOut" }}
            className="fixed inset-0 z-50 flex items-center justify-center p-4 md:p-6"
          >
            <motion.div
              className="w-full max-w-4xl bg-gradient-to-br from-gray-900 via-gray-800 to-gray-900 border border-gray-700 rounded-2xl shadow-2xl"
              onClick={(e) => e.stopPropagation()}
            >
              {/* Header */}
              <motion.div
                initial={{ opacity: 0, y: -10 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ delay: 0.1, duration: 0.3 }}
                className="relative border-b border-gray-700/50 px-6 md:px-8 py-6 md:py-8"
              >
                <div className="flex items-start justify-between gap-4">
                  <div className="flex-1">
                    <div className="flex items-center gap-2 mb-2">
                      <Flag className="h-5 w-5 text-red-500" />
                      <p className="text-xs font-semibold uppercase tracking-widest text-gray-400">Race Results</p>
                    </div>
                    <h2 className="text-3xl md:text-4xl font-bold font-display uppercase tracking-wider text-white">
                      {race.raceName}
                    </h2>
                    <p className="text-sm text-gray-400 mt-2">
                      {race.circuitName} • {race.location}, {race.country}
                    </p>
                  </div>

                  {/* Close Button */}
                  <motion.button
                    whileHover={{ scale: 1.1 }}
                    whileTap={{ scale: 0.95 }}
                    onClick={onClose}
                    className="flex-shrink-0 p-2 hover:bg-gray-700/50 rounded-lg transition-colors text-gray-400 hover:text-white"
                  >
                    <X className="h-6 w-6" />
                  </motion.button>
                </div>
              </motion.div>

              {/* Content - Podium Visualization */}
              <motion.div
                initial={{ opacity: 0 }}
                animate={{ opacity: 1 }}
                transition={{ delay: 0.2, duration: 0.4 }}
                className="px-6 md:px-8 py-8 md:py-12"
              >
                {podiumData.length >= 3 ? (
                  <div className="flex justify-center items-end gap-4 md:gap-8 min-h-[500px]">
                    {/* 2nd Place - Left */}
                    <PodiumBar position={2} driver={podiumData[1]} delay={0} />

                    {/* 1st Place - Center */}
                    <motion.div
                      initial={{ scale: 0.9 }}
                      animate={{ scale: 1 }}
                      transition={{ delay: 0.3, duration: 0.4 }}
                      className="relative -mb-8"
                    >
                      <PodiumBar position={1} driver={podiumData[0]} delay={1} />
                    </motion.div>

                    {/* 3rd Place - Right */}
                    <PodiumBar position={3} driver={podiumData[2]} delay={2} />
                  </div>
                ) : (
                  <motion.div
                    initial={{ opacity: 0 }}
                    animate={{ opacity: 1 }}
                    transition={{ duration: 0.4 }}
                    className="flex items-center justify-center py-12 text-gray-400"
                  >
                    <p className="text-center">Race results not yet available</p>
                  </motion.div>
                )}
              </motion.div>

              {/* Footer - Additional Info */}
              {podiumData.length >= 3 && (
                <motion.div
                  initial={{ opacity: 0, y: 10 }}
                  animate={{ opacity: 1, y: 0 }}
                  transition={{ delay: 0.4, duration: 0.3 }}
                  className="border-t border-gray-700/50 px-6 md:px-8 py-4 md:py-6 bg-gray-800/30 rounded-b-2xl"
                >
                  <div className="text-center text-sm text-gray-400">
                    <p>Top 3 Podium Finishers • Total Points: {podiumData.reduce((sum, d) => sum + (d.points || 0), 0)} pts</p>
                  </div>
                </motion.div>
              )}
            </motion.div>
          </motion.div>
        </>
      )}
    </AnimatePresence>
  );
};

export default RaceResultModal;
