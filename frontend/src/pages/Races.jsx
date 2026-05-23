import { useMemo, useState } from "react";
import { CalendarClock } from "lucide-react";
import { EmptyState, ErrorState, LoadingState } from "../components/common";
import { RaceCard } from "../components/races";
import useFetch from "../hooks/useFetch";
import usePageTitle from "../hooks/usePageTitle";
import { useNavigate } from "react-router-dom";

const Races = () => {
  usePageTitle("Races");
  const navigate = useNavigate();

  const { data, loading, error, refetch } = useFetch("/races");
  const [search, setSearch] = useState("");

  const races = (data || []).slice().sort((a, b) => (a.round ?? 999) - (b.round ?? 999));
  const completed = races.filter((race) => race.status === "COMPLETED");
  const scheduled = races.filter((race) => race.status !== "COMPLETED");

  const filtered = useMemo(() => {
    const token = search.toLowerCase();
    return races.filter((race) => {
      return (
        race.raceName?.toLowerCase().includes(token) ||
        race.circuitName?.toLowerCase().includes(token) ||
        race.location?.toLowerCase().includes(token)
      );
    });
  }, [races, search]);

  if (loading) return <LoadingState message="Loading race calendar..." />;
  if (error) return <ErrorState message={error} onRetry={refetch} />;
  if (!races.length) return <EmptyState title="No races found" description="No race calendar rows available." />;

  return (
    <div className="space-y-6">
      <section className="rounded-xl2 border border-borderSoft bg-bgElevated p-4 md:p-6">
        <div className="flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
          <div>
            <p className="section-label">Season Calendar</p>
            <div className="mt-2 flex items-center gap-3">
              <CalendarClock className="h-5 w-5 text-accentRed md:h-6 md:w-6" />
              <h1 className="font-display font-bold text-2xl uppercase tracking-widest sm:text-3xl md:text-4xl">2026 RACE CALENDAR</h1>
            </div>
            <div className="mt-1 h-[2px] w-16 bg-red-600"></div>
            <p className="mt-2 text-xs text-gray-500 tracking-widest uppercase sm:text-sm">{completed.length} completed · {scheduled.length} scheduled</p>
          </div>

          <input
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Search race, circuit or location"
            className="surface-input w-full lg:w-[320px]"
          />
        </div>
      </section>

      {filtered.length === 0 ? (
        <EmptyState title="No matching races" description="Try different keywords." />
      ) : (
        <section className="space-y-3">
          {filtered.map((race, index) => (
            <RaceCard
              key={race.raceId || `${race.round}-${index}`}
              race={race}
              index={index}
              onNavigate={(raceId) => navigate(`/races/${raceId}`)}
            />
          ))}
        </section>
      )}
    </div>
  );
};

export default Races;

