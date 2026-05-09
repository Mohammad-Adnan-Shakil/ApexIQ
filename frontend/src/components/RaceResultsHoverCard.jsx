import React from "react";
import { motion } from "framer-motion";
import { Trophy, Medal, MapPin } from "lucide-react";

/**
 * Map driver codes to country flags
 */
const driverFlags = {
  // Red Bull
  "VER": "🇳🇱",  // Max Verstappen - Netherlands
  "PER": "🇲🇽",  // Sergio Pérez - Mexico
  
  // Ferrari
  "LEC": "🇲🇨",  // Charles Leclerc - Monaco
  "SAI": "🇲🇽",  // Carlos Sainz - Spain
  
  // Mercedes
  "HAM": "🇬🇧",  // Lewis Hamilton - United Kingdom
  "RUS": "🇩🇪",  // George Russell - Germany
  
  // Alpine/Renault
  "ALO": "🇪🇸",  // Fernando Alonso - Spain
  "OCO": "🇫🇷",  // Esteban Ocon - France
  
  // McLaren
  "NOR": "🇬🇧",  // Lando Norris - United Kingdom
  "PIA": "🇵🇱",  // Oscar Piastri - Australia
  
  // Aston Martin
  "STR": "🇨🇦",  // Lance Stroll - Canada
  "AMR": "🇩🇪",  // Fernando Alonso - Spain
  
  // Williams
  "ALB": "🇬🇧",  // Alexander Albon - Thailand
  "LAT": "🇬🇧",  // Logan Sargeant - United States
  
  // Haas
  "MAG": "🇩🇰",  // Kevin Magnussen - Denmark
  "HUL": "🇩🇪",  // Nico Hulkenberg - Germany
  
  // RB
  "TSU": "🇯🇵",  // Yuki Tsunoda - Japan
  "GAS": "🇹🇭",  // Alexander Albon - Thailand
  
  // Kick Sauber
  "BOT": "🇫🇮",  // Valtteri Bottas - Finland
  "ZHO": "🇨🇳",  // Zhou Guanyu - China
  
  // Default for unknown drivers
  "DEF": "🏁"
};

const getFlag = (driverCode) => driverFlags[driverCode] || driverFlags["DEF"];

/**
 * Enhanced Race Results Hover Card Component
 * Features podium-style bar chart with premium styling and animations
 */
const RaceResultsHoverCard = ({ 
  raceName, 
  results = [], 
  loading = false,
  isVisible = false,
  onClose = () => {} 
}) => {
  if (!isVisible) return null;

  // Get top 3 drivers for podium display
  const topThree = results.slice(0, 3).filter(Boolean);
  
  // Calculate max points for bar chart scaling
  const maxPoints = Math.max(...topThree.map(r => r.points || 0), 1);

  const podiumPositions = {
    1: { color: "from-yellow-400 to-yellow-600", medal: "🥇", label: "P1" },
    2: { color: "from-gray-300 to-gray-500", medal: "🥈", label: "P2" },
    3: { color: "from-orange-600 to-orange-800", medal: "🥉", label: "P3" }
  };

  return (
    <motion.div
      initial={{ opacity: 0, scale: 0.9, y: -20 }}
      animate={{ opacity: 1, scale: 1, y: 0 }}
      exit={{ opacity: 0, scale: 0.9, y: -20 }}
      transition={{ 
        duration: 0.3, 
        ease: [0.4, 0, 0.2, 1] 
      }}
      className="fixed top-20 right-4 z-50 w-96 rounded-2xl shadow-2xl"
      style={{
        background: "rgba(17, 24, 39, 0.95)",
        backdropFilter: "blur(12px)",
        border: "1px solid rgba(255, 255, 255, 0.1)",
        boxShadow: "0 25px 50px -12px rgba(0, 0, 0, 0.25), 0 0 0 1px rgba(255, 255, 255, 0.1)"
      }}
    >
      {/* Close Button */}
      <button
        onClick={onClose}
        className="absolute top-3 right-3 p-1.5 rounded-lg bg-black/20 hover:bg-black/30 transition-colors duration-200 z-10"
        style={{ backdropFilter: "blur(8px)" }}
      >
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="text-gray-400 hover:text-white">
          <path d="M18 6L6 6M18 18L6 18"/>
        </svg>
      </button>

      {/* Header */}
      <div className="p-6 border-b border-white/10">
        <div className="flex items-center gap-3">
          <div className="p-2 rounded-xl bg-gradient-to-br from-yellow-500/20 to-yellow-600/20">
            <Trophy className="h-5 w-5 text-yellow-400" />
          </div>
          <div>
            <h3 className="font-bold text-white text-lg mb-1">🏆 Race Results</h3>
            <p className="text-sm text-gray-300">{raceName}</p>
          </div>
        </div>
      </div>

      {/* Content */}
      <div className="p-6">
        {loading ? (
          <div className="flex flex-col items-center justify-center py-8">
            <div className="animate-spin rounded-full h-8 w-8 border-2 border-white/20 border-t-white"></div>
            <p className="text-sm text-gray-400 mt-3">Loading results...</p>
          </div>
        ) : topThree.length === 0 ? (
          <div className="text-center py-8">
            <p className="text-gray-400">No results available</p>
          </div>
        ) : (
          <div className="space-y-6">
            {/* Podium Display */}
            <div className="grid grid-cols-3 gap-4 mb-6">
              {topThree.map((result, index) => {
                const position = index + 1;
                const podiumInfo = podiumPositions[position];
                
                return (
                  <motion.div
                    key={result.driverCode || position}
                    initial={{ opacity: 0, y: 20 }}
                    animate={{ opacity: 1, y: 0 }}
                    transition={{ 
                      delay: index * 0.1,
                      duration: 0.4 
                    }}
                    className="text-center"
                  >
                    {/* Medal */}
                    <div className="text-3xl mb-2">{podiumInfo.medal}</div>
                    
                    {/* Position */}
                    <div className="text-xs text-gray-400 font-semibold mb-2">
                      {podiumInfo.label}
                    </div>
                    
                    {/* Driver Flag and Name */}
                    <div className="flex items-center justify-center gap-2 mb-3">
                      <span className="text-2xl">{getFlag(result.driverCode)}</span>
                      <span className="text-white font-medium text-sm">
                        {result.driverCode || `P${position}`}
                      </span>
                    </div>
                    
                    {/* Final Position */}
                    <div className="text-xs text-gray-400">
                      Position: {result.position}
                    </div>
                  </motion.div>
                );
              })}
            </div>

            {/* Mini Podium Bar Chart */}
            <div className="bg-black/30 rounded-xl p-4">
              <h4 className="text-sm font-semibold text-gray-300 mb-4">Podium Performance</h4>
              <div className="space-y-3">
                {topThree.map((result, index) => {
                  const position = index + 1;
                  const podiumInfo = podiumPositions[position];
                  const barHeight = maxPoints > 0 ? ((result.points || 0) / maxPoints) * 100 : 20;
                  
                  return (
                    <div key={result.driverCode || position} className="flex items-center gap-3">
                      {/* Position Label */}
                      <div className="w-12 text-right">
                        <span className="text-xs font-semibold text-gray-400">
                          {podiumInfo.label}
                        </span>
                      </div>
                      
                      {/* Bar */}
                      <div className="flex-1 relative">
                        <div className="h-6 bg-gray-800 rounded-full overflow-hidden">
                          <motion.div
                            initial={{ width: 0 }}
                            animate={{ width: `${barHeight}%` }}
                            transition={{ 
                              delay: index * 0.15,
                              duration: 0.6,
                              ease: "easeOut" 
                            }}
                            className={`h-full rounded-full bg-gradient-to-r ${podiumInfo.color}`}
                          />
                        </div>
                      </div>
                      
                      {/* Driver Info */}
                      <div className="flex items-center gap-2 min-w-0">
                        <span className="text-sm">{getFlag(result.driverCode)}</span>
                        <span className="text-white text-xs font-medium truncate">
                          {result.driverCode || `P${position}`}
                        </span>
                        {result.points && (
                          <span className="text-xs text-gray-400 ml-2">
                            {result.points}pts
                          </span>
                        )}
                      </div>
                    </div>
                  );
                })}
              </div>
            </div>
          </div>
        )}
      </div>
    </motion.div>
  );
};

export default RaceResultsHoverCard;
