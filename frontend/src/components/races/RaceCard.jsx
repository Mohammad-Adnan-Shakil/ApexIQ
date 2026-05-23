import { useState } from "react";
import { MapPin } from "lucide-react";
import { Card } from "../common";
import { formatRaceDate } from "../../utils/formatters";
import RaceResultModal from "./RaceResultModal";
import useFetch from "../../hooks/useFetch";

const RaceCard = ({ race, index, onNavigate }) => {
  const [isModalOpen, setIsModalOpen] = useState(false);
  const { data: results } = useFetch(
    isModalOpen ? `/races/${race.raceId}/results` : null
  );

  const isCompleted = race.status === "COMPLETED";
  const isClickable = isCompleted;

  const handleCardClick = () => {
    if (isClickable) {
      setIsModalOpen(true);
    }
  };

  return (
    <>
      <Card
        onClick={handleCardClick}
        className={`relative border-l-4 transition-all duration-200 ${
          isClickable
            ? "hover:bg-gray-800/80 hover:scale-[1.01] cursor-pointer"
            : "opacity-60 cursor-not-allowed"
        }`}
        style={{ borderLeftColor: isCompleted ? "#10b981" : "#6b7280" }}
        delay={index * 0.05}
      >
        <div className="grid grid-cols-[64px_1fr] items-center gap-4 sm:grid-cols-[64px_1fr_auto]">
          {/* Round Badge */}
          <div
            className={`font-display font-bold uppercase tracking-wide flex h-12 w-12 items-center justify-center rounded-full border text-sm font-semibold ring-2 ${
              isCompleted
                ? "border-emerald-500 bg-emerald-900/50 text-emerald-400 ring-emerald-500/30"
                : "border-gray-600 bg-gray-800 text-gray-400 ring-gray-600/30"
            }`}
          >
            {race.round}
          </div>

          {/* Race Info */}
          <div>
            <h2 className="text-lg font-semibold text-whitePrimary sm:text-xl">
              {race.raceName}
            </h2>
            <p className="text-sm text-whiteMuted">{race.circuitName}</p>
            <p className="mt-1 flex items-center gap-1 text-xs text-whiteMuted">
              <MapPin className="h-3.5 w-3.5" /> {race.location}, {race.country}
            </p>
          </div>

          {/* Date and Status */}
          <div className="text-right">
            <p className="font-mono text-sm text-whiteMuted">
              {formatRaceDate(race.date)}
            </p>
            <div className="mt-2 inline-flex items-center gap-1 rounded-full px-2.5 py-1 text-xs font-semibold">
              {isCompleted ? (
                <span className="inline-flex items-center gap-1 rounded-full bg-emerald-900/30 px-2 py-1 text-emerald-400">
                  <span className="h-2 w-2 rounded-full bg-emerald-400" /> COMPLETED
                </span>
              ) : (
                <span className="inline-flex items-center gap-1 rounded-full bg-gray-700/30 px-2 py-1 text-gray-400">
                  <span className="h-2 w-2 animate-pulse rounded-full bg-gray-400" /> SCHEDULED
                </span>
              )}
            </div>
          </div>
        </div>
      </Card>

      {/* Result Modal */}
      <RaceResultModal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        race={race}
        results={results}
      />
    </>
  );
};

export default RaceCard;
