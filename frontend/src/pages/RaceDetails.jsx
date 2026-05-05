import { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { motion } from "framer-motion";
import { ArrowLeft, Calendar, MapPin, Clock, Flag } from "lucide-react";
import { Card, EmptyState, ErrorState, LoadingState } from "../components/common";
import RacePodium from "../components/RacePodium";
import useFetch from "../hooks/useFetch";
import usePageTitle from "../hooks/usePageTitle";
import { formatRaceDate } from "../utils/formatters";

const RaceDetails = () => {
  const { raceId } = useParams();
  const navigate = useNavigate();
  usePageTitle("Race Details");

  const { data: race, loading, error, refetch } = useFetch(`/races/${raceId}`);
  const { data: results, loading: resultsLoading, error: resultsError, refetch: refetchResults } = useFetch(`/races/${raceId}/results`);

  const loadingState = loading || resultsLoading;
  const errorState = error || resultsError;

  if (loadingState) return <LoadingState message="Loading race details..." />;
  if (errorState) return <ErrorState message={errorState} onRetry={() => { refetch(); refetchResults(); }} />;
  if (!race) return <EmptyState title="Race not found" description="The requested race could not be found." />;

  const isCompleted = race.status === "COMPLETED";
  const podiumResults = results?.slice(0, 3) || [];

  return (
    <div className="space-y-6">
      {/* Back Navigation */}
      <motion.div
        initial={{ opacity: 0, x: -20 }}
        animate={{ opacity: 1, x: 0 }}
        transition={{ duration: 0.3 }}
      >
        <button
          onClick={() => navigate("/races")}
          className="flex items-center gap-2 text-whiteMuted hover:text-whitePrimary transition-colors"
        >
          <ArrowLeft className="h-4 w-4" />
          Back to Races
        </button>
      </motion.div>

      {/* Race Header */}
      <motion.div
        initial={{ opacity: 0, y: -20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.5 }}
      >
        <Card className="bg-gradient-to-r from-accentRed/10 to-bgElevated" delay={0.1}>
          <div className="p-4 md:p-6">
            <div className="flex flex-col gap-4 md:flex-row md:items-start md:justify-between">
              <div>
                <div className="flex items-center gap-3 mb-3">
                  <div className={`font-display font-bold uppercase tracking-wide flex h-12 w-12 items-center justify-center rounded-full border text-sm font-semibold ring-2 ring-red-500/30 ${
                    isCompleted
                      ? "border-emerald-500 bg-emerald-900/50 text-emerald-400"
                      : "border-gray-600 bg-gray-800 text-gray-400"
                  }`}>
                    {race.round}
                  </div>
                  <div>
                    <p className="section-label">Grand Prix</p>
                    <h1 className="mt-1 font-display font-bold text-2xl uppercase tracking-widest sm:text-3xl md:text-4xl">
                      {race.raceName}
                    </h1>
                  </div>
                </div>
                
                <div className="space-y-2 text-sm text-whiteMuted">
                  <div className="flex items-center gap-2">
                    <MapPin className="h-4 w-4" />
                    {race.circuitName} · {race.location}, {race.country}
                  </div>
                  <div className="flex items-center gap-2">
                    <Calendar className="h-4 w-4" />
                    {formatRaceDate(race.date)}
                  </div>
                </div>
              </div>

              <div className="text-right">
                <div className="inline-flex items-center gap-1 rounded-full px-3 py-2 text-sm font-semibold">
                  {isCompleted ? (
                    <span className="inline-flex items-center gap-1 rounded-full bg-emerald-900/50 border border-emerald-700 px-3 py-2 text-emerald-400">
                      <Flag className="h-4 w-4" /> COMPLETED
                    </span>
                  ) : (
                    <span className="inline-flex items-center gap-1 rounded-full bg-gray-800 border border-gray-600 px-3 py-2 text-gray-400">
                      <Clock className="h-4 w-4" /> UPCOMING
                    </span>
                  )}
                </div>
                <div className="mt-2 text-xs text-whiteMuted">
                  Round {race.round} of 2026 Season
                </div>
              </div>
            </div>
          </div>
        </Card>
      </motion.div>

      {/* Race Results */}
      {isCompleted && (
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5, delay: 0.2 }}
        >
          <RacePodium results={podiumResults} delay={0.3} />
        </motion.div>
      )}

      {/* Additional Race Information */}
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.5, delay: 0.3 }}
      >
        <Card delay={0.4}>
          <h2 className="font-display font-semibold text-xl uppercase tracking-wider mb-4 text-whitePrimary">
            Race Information
          </h2>
          
          <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
            <div>
              <p className="text-sm text-whiteMuted mb-1">Circuit</p>
              <p className="font-semibold text-whitePrimary">{race.circuitName}</p>
            </div>
            <div>
              <p className="text-sm text-whiteMuted mb-1">Location</p>
              <p className="font-semibold text-whitePrimary">{race.location}, {race.country}</p>
            </div>
            <div>
              <p className="text-sm text-whiteMuted mb-1">Round</p>
              <p className="font-semibold text-whitePrimary">{race.round}</p>
            </div>
            <div>
              <p className="text-sm text-whiteMuted mb-1">Status</p>
              <p className="font-semibold text-whitePrimary">{race.status}</p>
            </div>
          </div>
        </Card>
      </motion.div>
    </div>
  );
};

export default RaceDetails;
