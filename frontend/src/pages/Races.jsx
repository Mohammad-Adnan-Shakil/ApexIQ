import { useMemo, useState } from "react";
import { motion } from "framer-motion";
import { Card, Input, SkeletonTable } from "../components/common";
import { useFetch } from "../hooks/useFetch";

const Races = () => {
  const { data, loading, error } = useFetch("/races");
  const [search, setSearch] = useState("");
  const [statusFilter, setStatusFilter] = useState("all");

  const races = useMemo(() => {
    const source = data || [];
    const unique = new Map();

    for (const race of source) {
      const key =
        race?.round != null
          ? `round-${race.round}`
          : `fallback-${race?.raceName || "race"}-${race?.date || "date"}`;

      if (!unique.has(key)) {
        unique.set(key, race);
      }
    }

    return Array.from(unique.values()).sort((a, b) => {
      const aRound = a?.round ?? Number.MAX_SAFE_INTEGER;
      const bRound = b?.round ?? Number.MAX_SAFE_INTEGER;
      if (aRound !== bRound) return aRound - bRound;
      return String(a?.date || "").localeCompare(String(b?.date || ""));
    });
  }, [data]);

  const filteredRaces = useMemo(() => {
    return races.filter((race) => {
      const token = search.toLowerCase();
      const matchesSearch =
        race.raceName?.toLowerCase().includes(token) ||
        race.location?.toLowerCase().includes(token) ||
        race.country?.toLowerCase().includes(token);

      const status = (race.status || "SCHEDULED").toUpperCase();
      const matchesStatus =
        statusFilter === "all" ||
        (statusFilter === "completed" && status === "COMPLETED") ||
        (statusFilter === "upcoming" && status !== "COMPLETED");

      return matchesSearch && matchesStatus;
    });
  }, [races, search, statusFilter]);

  if (loading) {
    return (
      <div className="space-y-6">
        <h1 className="text-3xl font-bold text-white">Race Calendar</h1>
        <SkeletonTable rows={8} cols={5} />
      </div>
    );
  }

  if (error) {
    return (
      <Card>
        <p className="text-red-400 font-semibold">Unable to load race calendar.</p>
        <p className="text-gray-400 mt-2 text-sm">{error}</p>
      </Card>
    );
  }

  const completedCount = races.filter((race) => race.status === "COMPLETED").length;

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold text-white">2026 Race Calendar</h1>
        <p className="text-gray-400 mt-2">
          {completedCount} completed, {races.length - completedCount} scheduled
        </p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <div className="md:col-span-2">
          <Input
            placeholder="Search race, location, country"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
        </div>
        <select
          value={statusFilter}
          onChange={(e) => setStatusFilter(e.target.value)}
          className="px-4 py-2 bg-gray-800 border border-gray-700 rounded-lg text-white focus:outline-none focus:border-red-500"
        >
          <option value="all">All races</option>
          <option value="completed">Completed</option>
          <option value="upcoming">Upcoming</option>
        </select>
      </div>

      {filteredRaces.length === 0 ? (
        <Card>
          <p className="text-gray-300">No races match your filters.</p>
        </Card>
      ) : (
        <div className="space-y-4">
          {filteredRaces.map((race, idx) => (
            <motion.div
              key={race.raceId}
              initial={{ opacity: 0, y: 8 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: idx * 0.03 }}
            >
              <Card hover>
                <div className="grid grid-cols-1 lg:grid-cols-5 gap-4 items-center">
                  <div className="lg:col-span-2">
                    <p className="text-xs uppercase text-gray-400">Round {race.round}</p>
                    <h3 className="text-xl font-bold text-white mt-1">{race.raceName}</h3>
                    <p className="text-gray-400 mt-1">{race.circuitName}</p>
                  </div>
                  <div>
                    <p className="text-xs uppercase text-gray-400">Location</p>
                    <p className="text-white mt-1">{race.location}, {race.country}</p>
                  </div>
                  <div>
                    <p className="text-xs uppercase text-gray-400">Date</p>
                    <p className="text-white mt-1">{race.date}</p>
                  </div>
                  <div>
                    <p className="text-xs uppercase text-gray-400">Status</p>
                    <span
                      className={`inline-block mt-1 px-3 py-1 rounded-full text-xs font-semibold ${
                        race.status === "COMPLETED"
                          ? "bg-green-500/20 text-green-300"
                          : "bg-yellow-500/20 text-yellow-300"
                      }`}
                    >
                      {race.status}
                    </span>
                  </div>
                </div>
              </Card>
            </motion.div>
          ))}
        </div>
      )}
    </div>
  );
};

export default Races;
