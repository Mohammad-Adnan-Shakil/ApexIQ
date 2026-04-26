import { useMemo, useState } from "react";
import { CalendarClock, CheckCircle2, Clock3, Flag, MapPin } from "lucide-react";
import { Card, EmptyState, ErrorState, LoadingState } from "../components/common";
import useFetch from "../hooks/useFetch";
import usePageTitle from "../hooks/usePageTitle";
import { formatRaceDate } from "../utils/formatters";

const Races = () => {
  usePageTitle("Races");

  const { data, loading, error, refetch } = useFetch("/races");
  const [search, setSearch] = useState("");

  const races = (data || []).slice().sort((a, b) => (a.round ?? 999) - (b.round ?? 999));
  const completed = races.filter((race) => race.status === "COMPLETED");
  const scheduled = races.filter((race) => race.status !== "COMPLETED");

  const nextRaceId = scheduled.length ? scheduled[0].raceId : null;

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
      <section className="rounded-xl2 border border-borderSoft bg-bgElevated p-6">
        <div className="flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
          <div>
            <p className="section-label">Season Calendar</p>
            <div className="mt-2 flex items-center gap-3">
              <CalendarClock className="h-6 w-6 text-accentRed" />
              <h1 className="font-display font-bold text-4xl uppercase tracking-widest">2026 RACE CALENDAR</h1>
            </div>
            <p className="mt-2 text-sm text-whiteMuted">{completed.length} completed · {scheduled.length} scheduled</p>
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
          {filtered.map((race, index) => {
            const isCompleted = race.status === "COMPLETED";
            const isNext = race.raceId === nextRaceId;

            return (
              <Card
                key={race.raceId || `${race.round}-${index}`}
                className={`relative ${isNext ? "border-accentRed/40 bg-accentRed/5" : ""}`}
                delay={index * 0.05}
              >
                {isNext ? <div className="absolute inset-y-3 left-0 w-1 rounded-r bg-accentRed" /> : null}

                <div className="grid grid-cols-[64px_1fr_auto] items-center gap-4">
                  <div
                    className={`font-display font-bold uppercase tracking-wide flex h-12 w-12 items-center justify-center rounded-full border text-sm font-semibold ${
                      isCompleted
                        ? "border-accentRed bg-accentRed text-white"
                        : "border-borderSoft bg-bgElevated text-whiteMuted"
                    }`}
                  >
                    {race.round}
                  </div>

                  <div>
                    <h2 className="text-xl font-semibold text-whitePrimary">{race.raceName}</h2>
                    <p className="text-sm text-whiteMuted">{race.circuitName}</p>
                    <p className="mt-1 flex items-center gap-1 text-xs text-whiteMuted">
                      <MapPin className="h-3.5 w-3.5" /> {race.location}, {race.country}
                    </p>
                  </div>

                  <div className="text-right">
                    <p className="font-mono text-sm text-whiteMuted">{formatRaceDate(race.date)}</p>
                    <div className="mt-2 inline-flex items-center gap-1 rounded-full px-2.5 py-1 text-xs font-semibold">
                      {isNext ? (
                        <span className="inline-flex items-center gap-1 rounded-full bg-accentRed/20 px-2 py-1 text-accentRed">
                          <span className="h-2 w-2 animate-pulse rounded-full bg-accentRed" /> NEXT
                        </span>
                      ) : isCompleted ? (
                        <span className="inline-flex items-center gap-1 rounded-full bg-successGreen/20 px-2 py-1 text-successGreen">
                          <CheckCircle2 className="h-3.5 w-3.5" /> COMPLETED
                        </span>
                      ) : (
                        <span className="inline-flex items-center gap-1 rounded-full bg-white/10 px-2 py-1 text-whiteMuted">
                          <Clock3 className="h-3.5 w-3.5" /> UPCOMING
                        </span>
                      )}
                    </div>
                  </div>
                </div>
              </Card>
            );
          })}
        </section>
      )}
    </div>
  );
};

export default Races;

