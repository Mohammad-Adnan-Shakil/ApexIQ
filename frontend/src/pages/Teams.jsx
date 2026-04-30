import { useMemo, useState } from "react";
import { Shield } from "lucide-react";
import { Card, EmptyState, ErrorState, LoadingState } from "../components/common";
import useFetch from "../hooks/useFetch";
import usePageTitle from "../hooks/usePageTitle";
import { teamColor } from "../utils/formatters";

const Teams = () => {
  usePageTitle("Constructors");

  const {
    data: constructors,
    loading: constructorsLoading,
    error: constructorsError,
    refetch: refetchConstructors,
  } = useFetch("/constructors");
  const { data: drivers, loading: driversLoading, error: driversError, refetch: refetchDrivers } = useFetch("/drivers");

  const [search, setSearch] = useState("");

  const teams = constructors || [];
  const driverList = drivers || [];

  const loading = constructorsLoading || driversLoading;
  const error = constructorsError || driversError;

  const standings = useMemo(() => {
    return teams
      .filter((team) => team.name?.toLowerCase().includes(search.toLowerCase()))
      .map((team) => {
        const lineup = driverList
          .filter((driver) => driver.team === team.name)
          .sort((a, b) => Number(b.points || 0) - Number(a.points || 0));

        const points = lineup.reduce((sum, driver) => sum + Number(driver.points || 0), 0);

        return {
          ...team,
          points,
          lineup,
        };
      })
      .sort((a, b) => b.points - a.points);
  }, [teams, driverList, search]);

  if (loading) return <LoadingState message="Loading constructor standings..." />;
  if (error) {
    return (
      <ErrorState
        message={error}
        onRetry={() => {
          refetchConstructors();
          refetchDrivers();
        }}
      />
    );
  }
  if (!standings.length) return <EmptyState title="No constructor standings" description="No teams found for this season." />;

  return (
    <div className="space-y-6">
      <section className="rounded-xl2 border border-borderSoft bg-bgElevated p-4 md:p-6">
        <div className="flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
          <div>
            <p className="section-label">Team Championship</p>
            <div className="mt-2 flex items-center gap-3">
              <Shield className="h-5 w-5 text-accentRed md:h-6 md:w-6" />
              <h1 className="text-2xl font-bold tracking-tight sm:text-3xl">CONSTRUCTOR STANDINGS</h1>
            </div>
            <div className="mt-1 h-[2px] w-16 bg-red-600"></div>
          </div>

          <input
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Search constructor"
            className="surface-input w-full lg:w-[260px]"
          />
        </div>
      </section>

      <section className="grid grid-cols-1 gap-4 md:grid-cols-2">
        {standings.map((team, index) => {
          const isLeader = index === 0;
          const accent = isLeader ? "#ffd700" : teamColor(team.name);

          return (
            <Card
              key={team.id || team.name}
              delay={index * 0.05}
              className={`relative overflow-hidden border-l-4 hover:bg-gray-800/80 hover:scale-[1.01] transition-all duration-200 cursor-pointer`}
              style={{ borderLeftColor: accent }}
            >
              <div className="flex items-start justify-between">
                <div>
                  <span className="bg-red-600 text-white text-xs font-black px-2 py-1 rounded-full">P{index + 1}</span>
                  <h2 className="mt-2 text-2xl font-bold text-whitePrimary">{team.name}</h2>
                  <p className="mt-1 text-sm text-whiteMuted">{team.nationality}</p>
                </div>
                <p className="font-mono text-3xl font-black text-red-500">{Math.round(team.points)}</p>
              </div>

              <div className="mt-5 border-t border-borderSoft pt-4">
                <p className="section-label mb-3">Driver Pairing</p>
                {team.lineup.length ? (
                  <div className="space-y-2">
                    {team.lineup.map((driver) => (
                      <div key={driver.driverId} className="flex items-center justify-between text-sm">
                        <span className="text-whitePrimary">{driver.name}</span>
                        <span className="font-mono text-red-500 font-bold">{Math.round(driver.points || 0)}</span>
                      </div>
                    ))}
                  </div>
                ) : (
                  <p className="text-sm text-whiteMuted">No mapped drivers.</p>
                )}
              </div>
            </Card>
          );
        })}
      </section>
    </div>
  );
};

export default Teams;

