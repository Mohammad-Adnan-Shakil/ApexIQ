import React from "react";
import { Trophy } from "lucide-react";
import { nationalityFlag, teamColor } from "../utils/formatters";

const RacePodium = ({ results = [], className = "" }) => {
  if (!results || results.length < 3) return null;

  const [p1, p2, p3] = results.slice(0, 3);

  return (
    <div className={`bg-bgElevated rounded-xl2 border border-borderSoft p-6 ${className}`}>
      <div className="flex items-center gap-2 mb-6">
        <Trophy className="h-5 w-5 text-accentGold" />
        <h3 className="font-display font-semibold text-xl uppercase tracking-wider text-whitePrimary">
          Podium Results
        </h3>
      </div>

      <div className="relative flex flex-col items-center justify-center gap-4 md:gap-6 lg:flex-row lg:items-end lg:justify-center lg:gap-8">
        
        {/* P2 - Left */}
        <div className="flex flex-col items-center order-2 lg:order-1">
          <div className="relative">
            <div className="flex h-16 w-16 items-center justify-center rounded-full border-2 border-gray-600 bg-gray-800 text-lg font-bold text-gray-400 md:h-20 md:w-20 md:text-xl">
              {p2.code || "P2"}
            </div>
            <div className="absolute -top-1 -right-1 h-4 w-4 rounded-full bg-gray-600 flex items-center justify-center text-xs text-white">
              2
            </div>
          </div>
          
          <div className="mt-3 text-center">
            <p className="font-semibold text-whitePrimary">{p2.name || "Driver 2"}</p>
            <p className="text-sm text-whiteMuted flex items-center justify-center gap-1 mt-1">
              {nationalityFlag(p2.nationality)} {p2.nationality}
            </p>
            <div className="mt-2 flex items-center justify-center gap-1 text-sm text-whiteMuted">
              <div 
                className="h-2 w-2 rounded-full" 
                style={{ backgroundColor: teamColor(p2.team) }}
              />
              {p2.team || "Unknown Team"}
            </div>
            {p2.points && (
              <p className="mt-2 font-mono text-lg font-bold text-gray-400">{p2.points} pts</p>
            )}
          </div>
          
          <div className="mt-3 h-16 w-24 bg-gray-800 rounded-t-lg border border-gray-600 border-b-0 flex items-center justify-center md:h-20 md:w-28">
            <span className="text-gray-400 font-bold text-sm md:text-base">2ND</span>
          </div>
        </div>

        {/* P1 - Center (Elevated) */}
        <div className="flex flex-col items-center order-1 lg:order-2 lg:scale-110">
          <div className="relative">
            <div className="flex h-20 w-20 items-center justify-center rounded-full border-2 border-accentGold bg-accentRed/20 text-xl font-bold text-accentGold md:h-24 md:w-24 md:text-2xl shadow-lg shadow-accentRed/30">
              {p1.code || "P1"}
            </div>
            <div className="absolute -top-1 -right-1 h-5 w-5 rounded-full bg-accentGold flex items-center justify-center text-sm text-bgPrimary font-bold">
              1
            </div>
            <Trophy className="absolute -top-3 left-1/2 transform -translate-x-1/2 h-6 w-6 text-accentGold" />
          </div>
          
          <div className="mt-3 text-center">
            <p className="font-semibold text-whitePrimary text-lg">{p1.name || "Driver 1"}</p>
            <p className="text-sm text-whiteMuted flex items-center justify-center gap-1 mt-1">
              {nationalityFlag(p1.nationality)} {p1.nationality}
            </p>
            <div className="mt-2 flex items-center justify-center gap-1 text-sm text-whiteMuted">
              <div 
                className="h-2 w-2 rounded-full" 
                style={{ backgroundColor: teamColor(p1.team) }}
              />
              {p1.team || "Unknown Team"}
            </div>
            {p1.points && (
              <p className="mt-2 font-mono text-xl font-bold text-accentGold">{p1.points} pts</p>
            )}
          </div>
          
          <div className="mt-3 h-20 w-28 bg-accentRed/20 rounded-t-lg border border-accentRed border-b-0 flex items-center justify-center md:h-24 md:w-32 shadow-lg shadow-accentRed/20">
            <span className="text-accentGold font-bold text-base md:text-lg">WINNER</span>
          </div>
        </div>

        {/* P3 - Right */}
        <div className="flex flex-col items-center order-3">
          <div className="relative">
            <div className="flex h-16 w-16 items-center justify-center rounded-full border-2 border-orange-700 bg-orange-900/30 text-lg font-bold text-orange-400 md:h-20 md:w-20 md:text-xl">
              {p3.code || "P3"}
            </div>
            <div className="absolute -top-1 -right-1 h-4 w-4 rounded-full bg-orange-600 flex items-center justify-center text-xs text-white">
              3
            </div>
          </div>
          
          <div className="mt-3 text-center">
            <p className="font-semibold text-whitePrimary">{p3.name || "Driver 3"}</p>
            <p className="text-sm text-whiteMuted flex items-center justify-center gap-1 mt-1">
              {nationalityFlag(p3.nationality)} {p3.nationality}
            </p>
            <div className="mt-2 flex items-center justify-center gap-1 text-sm text-whiteMuted">
              <div 
                className="h-2 w-2 rounded-full" 
                style={{ backgroundColor: teamColor(p3.team) }}
              />
              {p3.team || "Unknown Team"}
            </div>
            {p3.points && (
              <p className="mt-2 font-mono text-lg font-bold text-orange-400">{p3.points} pts</p>
            )}
          </div>
          
          <div className="mt-3 h-16 w-24 bg-orange-900/30 rounded-t-lg border border-orange-700 border-b-0 flex items-center justify-center md:h-20 md:w-28">
            <span className="text-orange-400 font-bold text-sm md:text-base">3RD</span>
          </div>
        </div>
      </div>
    </div>
  );
};

export default RacePodium;
