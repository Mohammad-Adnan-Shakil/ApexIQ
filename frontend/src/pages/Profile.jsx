import { useEffect, useMemo, useState } from "react";
import { Calendar, Flag, Trophy, Users } from "lucide-react";
import { Card, EmptyState, ErrorState, LoadingState } from "../components/common";
import { useAuth } from "../context/AuthContext";
import useFetch from "../hooks/useFetch";
import usePageTitle from "../hooks/usePageTitle";
import { nationalityFlag, teamColor } from "../utils/formatters";

const FAV_KEY = "f1pulse:favourite-driver";

const Profile = () => {
  usePageTitle("Profile");

  const { user, token } = useAuth();
  const { data: profileData, loading: profileLoading, error: profileError, refetch: refetchProfile } = useFetch("/user/me");
  const { data: drivers, loading: driversLoading, error: driversError, refetch: refetchDrivers } = useFetch("/drivers");
  const { data: races, loading: racesLoading, error: racesError, refetch: refetchRaces } = useFetch("/races");

  const [favouriteDriverId, setFavouriteDriverId] = useState(localStorage.getItem(FAV_KEY) || "");

  useEffect(() => {
    localStorage.setItem(FAV_KEY, favouriteDriverId);
  }, [favouriteDriverId]);

  const profile = profileData || user || {};
  const emailOrUser = profile.email || profile.username || "user@f1pulse.app";
  const role = String(profile.role || user?.role || "USER").toUpperCase();

  const driverList = drivers || [];
  const raceList = races || [];
  const completedRaces = raceList.filter((race) => race.status === "COMPLETED").length;
  const upcomingRaces = raceList.length - completedRaces;

  const favouriteDriver = useMemo(
    () => driverList.find((driver) => String(driver.driverId) === String(favouriteDriverId)),
    [driverList, favouriteDriverId]
  );

  const loading = profileLoading || driversLoading || racesLoading;
  const error = profileError || driversError || racesError;

  if (loading) return <LoadingState message="Loading profile data..." />;
  if (error) {
    return (
      <ErrorState
        message={error}
        onRetry={() => {
          refetchProfile();
          refetchDrivers();
          refetchRaces();
        }}
      />
    );
  }

  if (!driverList.length) return <EmptyState title="No profile data" description="Driver data is required to build profile insights." />;

  return (
    <div className="space-y-6">
      <Card className="bg-gradient-to-r from-accentRed/10 to-bgElevated" delay={0.05}>
        <div className="flex flex-col items-start gap-4 md:flex-row md:items-center">
          <div className="flex h-[72px] w-[72px] items-center justify-center rounded-full bg-accentRed text-3xl font-bold text-white">
            {emailOrUser[0]?.toUpperCase() || "U"}
          </div>

          <div>
            <p className="section-label">Member Profile</p>
            <h1 className="mt-1 text-3xl font-bold">{emailOrUser}</h1>
            <div className="mt-2 flex items-center gap-2">
              <span className="rounded-full bg-accentRed/20 px-3 py-1 text-xs font-semibold text-accentRed">{role}</span>
              <span className="text-sm text-whiteMuted">Member · ApexIQ 2026</span>
            </div>
          </div>
        </div>
      </Card>

      <section className="grid grid-cols-1 gap-4 md:grid-cols-3">
        <Card delay={0.1}>
          <div className="mb-3 flex items-center gap-2 text-accentRed"><Users className="h-4 w-4" /><span className="section-label">Drivers Loaded</span></div>
          <p className="hero-number text-[52px]">{driverList.length}</p>
        </Card>

        <Card delay={0.15}>
          <div className="mb-3 flex items-center gap-2 text-accentRed"><Flag className="h-4 w-4" /><span className="section-label">Completed Races</span></div>
          <p className="hero-number text-[52px] text-successGreen">{completedRaces}</p>
        </Card>

        <Card delay={0.2}>
          <div className="mb-3 flex items-center gap-2 text-accentRed"><Calendar className="h-4 w-4" /><span className="section-label">Upcoming Rounds</span></div>
          <p className="hero-number text-[52px]">{upcomingRaces}</p>
        </Card>
      </section>

      <Card delay={0.25}>
        <p className="section-label">Session</p>
        <h2 className="mt-2 text-xl font-semibold">JWT Session Status</h2>
        <div className="mt-4 space-y-2 text-sm">
          <p className="flex items-center gap-2 text-whiteMuted"><span className="h-2.5 w-2.5 rounded-full bg-successGreen" /> Active</p>
          <p className="text-whiteMuted">Token Preview:</p>
          <code className="block rounded-lg border border-borderSoft bg-bgElevated px-3 py-2 text-xs text-whitePrimary">
            {token ? `${token.slice(0, 20)}...` : "No token"}
          </code>
          <p className="text-whiteMuted">Access Scope: <span className="rounded-full bg-white/10 px-2 py-1 text-xs text-whitePrimary">{role}</span></p>
        </div>
      </Card>

      <Card delay={0.3}>
        <p className="section-label">Favourite Driver</p>
        <h2 className="mt-2 text-xl font-semibold">Set Your Favourite Driver</h2>

        <select
          value={favouriteDriverId}
          onChange={(e) => setFavouriteDriverId(e.target.value)}
          className="surface-input mt-4"
        >
          <option value="">Select favourite driver</option>
          {driverList.map((driver) => (
            <option key={driver.driverId} value={driver.driverId}>
              {driver.name} ({driver.code || "DRV"})
            </option>
          ))}
        </select>

        {favouriteDriver ? (
          <div className="mt-4 rounded-xl2 border border-borderSoft bg-bgElevated p-4">
            <div className="flex items-start justify-between">
              <div>
                <p className="text-lg font-semibold">{favouriteDriver.name}</p>
                <p className="mt-1 text-sm text-whiteMuted">{favouriteDriver.code || "DRV"} · {nationalityFlag(favouriteDriver.nationality)} {favouriteDriver.nationality}</p>
              </div>
              <div className="text-right">
                <p className="section-label">Points</p>
                <p className="text-2xl font-bold text-accentGold">{Math.round(favouriteDriver.points || 0)}</p>
              </div>
            </div>
            <div className="mt-3 flex items-center gap-2 text-sm text-whiteMuted">
              <span className="h-2.5 w-2.5 rounded-full" style={{ backgroundColor: teamColor(favouriteDriver.team) }} />
              {favouriteDriver.team || "Unknown Team"}
            </div>
            <p className="mt-3 inline-flex items-center gap-1 rounded-full bg-accentRed/20 px-2.5 py-1 text-xs font-semibold text-accentRed">
              <Trophy className="h-3.5 w-3.5" /> Favourite Pick
            </p>
          </div>
        ) : null}
      </Card>
    </div>
  );
};

export default Profile;

