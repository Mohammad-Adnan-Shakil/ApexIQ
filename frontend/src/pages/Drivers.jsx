import { useMemo, useState } from "react";
import { motion } from "framer-motion";
import { Search } from "lucide-react";
import { Card, LoadingState, ErrorState, EmptyState } from "../components/common";
import useFetch from "../hooks/useFetch";
import usePageTitle from "../hooks/usePageTitle";
import { nationalityFlag, teamColor } from "../utils/formatters";

const positionBadgeClass = (position) => {
  if (position === 1) return "bg-accentGold text-black";
  if (position === 2) return "bg-zinc-300 text-black";
  if (position === 3) return "bg-amber-700 text-white";
  return "bg-bgElevated text-whiteMuted border border-borderSoft";
};

const Drivers = () => {
  usePageTitle("Drivers");

  const { data, loading, error, refetch } = useFetch("/drivers");
  const [search, setSearch] = useState("");
  const [sortBy, setSortBy] = useState("points");

  const drivers = data || [];

  const filteredDrivers = useMemo(() => {
    const token = search.toLowerCase();

    return drivers
      .filter((driver) => {
        return (
          driver.name?.toLowerCase().includes(token) ||
          driver.code?.toLowerCase().includes(token) ||
          driver.team?.toLowerCase().includes(token)
        );
      })
      .sort((a, b) => {
        const aValue = a[sortBy] ?? 0;
        const bValue = b[sortBy] ?? 0;

        if (typeof aValue === "string") return aValue.localeCompare(bValue);
        return Number(bValue) - Number(aValue);
      });
  }, [drivers, search, sortBy]);

  if (loading) return <LoadingState message="Loading driver standings..." />;
  if (error) return <ErrorState message={error} onRetry={refetch} />;
  if (!drivers.length) return <EmptyState title="No drivers found" description="No season driver data available." />;

  return (
    <div className="space-y-6">
      <section className="rounded-xl2 border border-borderSoft bg-bgElevated p-6">
        <div className="flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
          <div>
            <p className="section-label">Championship</p>
            <div className="mt-2 flex items-center gap-3">
              <svg viewBox="0 0 64 24" className="h-6 w-14 text-accentRed" fill="none" stroke="currentColor" strokeWidth="2">
                <path d="M2 18H16L24 10H40L47 18H62" />
                <circle cx="14" cy="18" r="3" />
                <circle cx="49" cy="18" r="3" />
              </svg>
              <h1 className="font-display font-bold text-4xl uppercase tracking-widest">DRIVER STANDINGS</h1>
            </div>
            <p className="mt-2 text-sm text-whiteMuted">{drivers.length} drivers · 2026 season</p>
          </div>

          <div className="grid w-full grid-cols-1 gap-3 md:w-auto md:grid-cols-[280px_170px]">
            <div className="relative">
              <Search className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-whiteMuted" />
              <input
                value={search}
                onChange={(e) => setSearch(e.target.value)}
                placeholder="Search drivers"
                className="surface-input pl-10"
              />
            </div>
            <select
              value={sortBy}
              onChange={(e) => setSortBy(e.target.value)}
              className="surface-input"
            >
              <option value="points">Sort: Points</option>
              <option value="name">Sort: Name</option>
              <option value="team">Sort: Team</option>
            </select>
          </div>
        </div>
      </section>

      {filteredDrivers.length === 0 ? (
        <EmptyState title="No matching drivers" description="Try a different search value." />
      ) : (
        <section className="grid grid-cols-1 gap-4 md:grid-cols-2 xl:grid-cols-3">
          {filteredDrivers.map((driver, index) => {
            const pos = index + 1;
            const points = Math.round(driver.points || 0);
            const highlightPointColor = pos === 1 ? "text-accentGold" : "text-whitePrimary";

            return (
              <motion.div
                key={driver.driverId || driver.code || index}
                initial={{ opacity: 0, y: 14 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ delay: index * 0.05 }}
                whileHover={{ scale: 1.02 }}
              >
                <Card className="group relative overflow-hidden border border-borderSoft">
                  <div className="flex items-start justify-between">
                    <span className={`rounded-full px-3 py-1 text-xs font-semibold ${positionBadgeClass(pos)}`}>#{pos}</span>
                    <p className={`font-mono text-4xl font-bold tracking-tight ${highlightPointColor}`}>{points}</p>
                  </div>

                  <div className="mt-4">
                    <h2 className="text-2xl font-bold text-whitePrimary">{driver.name}</h2>
                    <span className="mt-2 inline-flex rounded-md bg-accentRed/20 px-2 py-1 text-xs font-semibold text-accentRed">
                      <span className="font-display font-bold uppercase tracking-wide">{driver.code || "DRV"}</span>
                    </span>
                  </div>

                  <div className="mt-4 flex items-center justify-between text-sm">
                    <div className="flex items-center gap-2 text-whiteMuted">
                      <span className="h-2.5 w-2.5 rounded-full" style={{ backgroundColor: teamColor(driver.team) }} />
                      {driver.team || "Unknown Team"}
                    </div>
                    <span>{nationalityFlag(driver.nationality)} {driver.nationality || "Unknown"}</span>
                  </div>

                  <div className="absolute inset-x-0 bottom-0 h-[2px] bg-gradient-to-r from-transparent via-accentRed to-transparent opacity-0 transition-opacity duration-200 group-hover:opacity-100" />
                </Card>
              </motion.div>
            );
          })}
        </section>
      )}
    </div>
  );
};

export default Drivers;

