import { useEffect, useMemo, useState, memo, useRef } from "react";
import { Area, AreaChart, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis } from "recharts";
import { Flag, Trophy, Users, Zap } from "lucide-react";
import { Card, AnimatedCount, LoadingState, ErrorState, EmptyState } from "../components/common";
import SkeletonLoader from "../components/SkeletonLoader";
import LiveClock from "../components/LiveClock";
import { useInView } from "../hooks/useInView";
import useFetch from "../hooks/useFetch";
import usePageTitle from "../hooks/usePageTitle";
import { dashboardDataPromise, racesDataPromise } from "../main";

const StatCard = memo(({ icon: Icon, label, value, subValue, accentClass = "text-whitePrimary", delay = 0, loading = false }) => (
  <Card delay={delay} className="relative overflow-hidden p-5">
    <div className="mb-4 flex items-center gap-3">
      <div className="flex h-10 w-10 items-center justify-center rounded-full bg-accentRed/20 text-accentRed">
        <Icon className="h-4 w-4" />
      </div>
      <p className="section-label">{label}</p>
    </div>

    {loading ? (
      <SkeletonLoader height="48px" width="80px" />
    ) : typeof value === "number" ? (
      <p className={`hero-number text-[42px] ${accentClass}`}>
        <AnimatedCount value={value} />
      </p>
    ) : (
      <p className={`text-2xl font-semibold ${accentClass}`}>{value}</p>
    )}

    {subValue && !loading && (
      <p className="mt-1 text-sm text-whiteMuted">{subValue}</p>
    )}
    <div className="absolute inset-x-0 bottom-0 h-[2px] bg-gradient-to-r from-accentRed/70 via-accentRed to-transparent" />
  </Card>
));

/**
 * PART 1: Static Hero Section
 * 
 * This renders IMMEDIATELY with zero API dependency.
 * The "2026 SEASON COMMAND CENTER" heading and LIVE clock
 * paint without waiting for data, dramatically improving LCP.
 */
const HeroSection = () => (
  <section className="flex flex-col justify-between gap-4 rounded-xl2 border border-borderSoft bg-bgElevated p-6 md:flex-row md:items-center">
    <div>
      <p className="section-label">Season Overview</p>
      <h1 className="font-display font-bold text-4xl uppercase tracking-widest mt-2 md:text-5xl">
        <span className="text-whitePrimary">2026 SEASON</span> <span className="text-accentRed">COMMAND CENTER</span>
      </h1>
      <p className="mt-2 text-sm text-whiteMuted">Real-time standings, race progress, and race intelligence at a glance.</p>
    </div>

    <div className="rounded-xl2 border border-borderSoft bg-bgCard px-4 py-3 text-right">
      <div className="flex items-center justify-end gap-2 text-xs uppercase tracking-[0.2em] text-whiteMuted">
        <span className="h-2.5 w-2.5 animate-pulse rounded-full bg-accentRed" /> LIVE
      </div>
      <LiveClock />
    </div>
  </section>
);

/**
 * PART 2: Stat Cards with Skeleton Loaders
 * 
 * These load from preloaded API promises.
 * Show skeleton while loading, then display actual data.
 */
const StatCardsSection = ({ drivers, races, loading }) => {
  const driverList = drivers || [];
  const raceList = (races || []).slice().sort((a, b) => (a.round ?? 999) - (b.round ?? 999));
  const completedRaces = raceList.filter((race) => race.status === "COMPLETED");
  
  const topDriver = driverList[0] || { name: "Andrea Kimi Antonelli", points: 72 };
  const topTeam = useMemo(() => {
    const grouped = Object.values(
      driverList.reduce((acc, driver) => {
        const key = driver.team || "Mercedes";
        if (!acc[key]) acc[key] = { name: key, points: 0 };
        acc[key].points += Number(driver.points || 0);
        return acc;
      }, {})
    ).sort((a, b) => b.points - a.points);
    return grouped[0] || { name: "Mercedes", points: 135 };
  }, [driverList]);

  return (
    <section className="grid grid-cols-1 gap-4 md:grid-cols-2 xl:grid-cols-4">
      <StatCard 
        icon={Users} 
        label="Drivers" 
        value={driverList.length || 22} 
        delay={0.05}
        loading={loading}
      />
      <StatCard 
        icon={Flag} 
        label="Completed Races" 
        value={completedRaces.length || 3} 
        accentClass="text-successGreen" 
        delay={0.1}
        loading={loading}
      />
      <StatCard 
        icon={Trophy} 
        label="Top Driver" 
        value={topDriver.name} 
        subValue={<span className="font-mono">{Math.round(topDriver.points || 0)} pts</span>} 
        delay={0.15}
        loading={loading}
      />
      <StatCard 
        icon={Zap} 
        label="Top Team" 
        value={topTeam.name} 
        subValue={<span className="font-mono">{Math.round(topTeam.points || 0)} pts</span>} 
        delay={0.2}
        loading={loading}
      />
    </section>
  );
};

/**
 * PART 3: Charts with Deferred Rendering
 * 
 * Charts are EXTREMELY heavy (Recharts renders SVG).
 * Defer rendering by 100ms to let hero + stat cards paint first.
 * Then render with skeleton loaders.
 */
const ChartsSection = ({ drivers, races, loading }) => {
  const [chartsVisible, setChartsVisible] = useState(false);
  
  useEffect(() => {
    // Defer chart render until after main content paints
    const timer = setTimeout(() => setChartsVisible(true), 100);
    return () => clearTimeout(timer);
  }, []);

  const driverList = drivers || [];
  const raceList = (races || []).slice().sort((a, b) => (a.round ?? 999) - (b.round ?? 999));
  const completedRaces = raceList.filter((race) => race.status === "COMPLETED");

  const standingsData = useMemo(() => driverList.slice(0, 8), [driverList]);
  const maxPoints = useMemo(() => Math.max(...standingsData.map((d) => Number(d.points || 0)), 1), [standingsData]);

  const progressData = useMemo(() => raceList.map((race, idx) => {
    const completedCountAtPoint = raceList
      .slice(0, idx + 1)
      .filter((entry) => entry.status === "COMPLETED").length;

    const progressValue = race.status === "COMPLETED" ? completedCountAtPoint : completedRaces.length;

    return {
      round: `R${race.round}`,
      progress: progressValue,
      status: race.status,
    };
  }), [raceList, completedRaces]);

  return (
    <section className="grid grid-cols-1 gap-4 xl:grid-cols-2">
      <Card delay={0.25}>
        <div className="mb-5 flex items-center justify-between">
          <h2 className="font-display font-semibold text-xl uppercase tracking-wider text-whitePrimary">Driver Standings</h2>
          <p className="section-label">Top 8</p>
        </div>

        {chartsVisible && !loading ? (
          <div className="space-y-3">
            {standingsData.map((driver, index) => {
              const points = Number(driver.points || 0);
              const widthPct = Math.max((points / maxPoints) * 100, 6);
              const isTop = index === 0;

              return (
                <div key={driver.driverId || driver.code} className="grid grid-cols-[64px_1fr_40px] items-center gap-3">
                  <span className="font-display font-bold uppercase tracking-wide text-sm text-whiteMuted">{driver.code || "DRV"}</span>
                  <div className="h-8 overflow-hidden rounded-lg bg-white/5">
                    <div
                      className="h-full rounded-lg transition-all duration-700"
                      style={{
                        width: `${widthPct}%`,
                        background: isTop
                          ? "linear-gradient(90deg,#ffd700,#ffeb85)"
                          : "linear-gradient(90deg,#e8002d,#ff4d6d)",
                      }}
                    />
                  </div>
                  <span className="font-mono text-right text-sm font-semibold text-whitePrimary">{Math.round(points)}</span>
                </div>
              );
            })}
          </div>
        ) : (
          <div className="space-y-3">
            {[...Array(5)].map((_, i) => (
              <div key={i} className="grid grid-cols-[64px_1fr_40px] items-center gap-3">
                <SkeletonLoader height="16px" width="40px" />
                <SkeletonLoader height="32px" />
                <SkeletonLoader height="16px" width="30px" />
              </div>
            ))}
          </div>
        )}
      </Card>

      <Card delay={0.3}>
        <div className="mb-5 flex items-center justify-between">
          <h2 className="font-display font-semibold text-xl uppercase tracking-wider text-whitePrimary">Race Progress</h2>
          <p className="section-label">Completion Curve</p>
        </div>

        {chartsVisible && !loading ? (
          <ResponsiveContainer width="100%" height={280}>
            <AreaChart data={progressData}>
              <defs>
                <linearGradient id="raceProgressFill" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="0%" stopColor="rgba(232,0,45,0.3)" />
                  <stop offset="100%" stopColor="rgba(232,0,45,0)" />
                </linearGradient>
              </defs>
              <CartesianGrid stroke="rgba(255,255,255,0.08)" strokeDasharray="3 3" />
              <XAxis dataKey="round" stroke="rgba(255,255,255,0.6)" />
              <YAxis allowDecimals={false} domain={[0, raceList.length]} stroke="rgba(255,255,255,0.6)" />
              <Tooltip
                contentStyle={{
                  background: "#141420",
                  border: "1px solid rgba(255,255,255,0.06)",
                  borderRadius: 12,
                }}
                formatter={(value) => [`${value} completed races`, "Progress"]}
              />
              <Area type="monotone" dataKey="progress" stroke="#e8002d" strokeWidth={3} fill="url(#raceProgressFill)" dot={({ cx, cy, payload }) => (
                <circle
                  cx={cx}
                  cy={cy}
                  r={5}
                  fill={payload.status === "COMPLETED" ? "#e8002d" : "#141420"}
                  stroke={payload.status === "COMPLETED" ? "#e8002d" : "rgba(255,255,255,0.4)"}
                  strokeWidth={2}
                />
              )} />
            </AreaChart>
          </ResponsiveContainer>
        ) : (
          <SkeletonLoader height="300px" />
        )}
      </Card>
    </section>
  );
};

/**
 * PART 4: Below-Fold Content with Lazy Loading
 * 
 * The "Upcoming Races" section only renders when scrolled into view.
 * This saves rendering time on initial page load.
 */
const UpcomingRacesSection = ({ races, loading }) => {
  const [ref, inView] = useInView({ threshold: 0.1 });
  const raceList = (races || []).slice().sort((a, b) => (a.round ?? 999) - (b.round ?? 999));
  const upcomingRaces = raceList.filter((race) => race.status !== "COMPLETED");

  return (
    <div ref={ref}>
      {inView ? (
        <Card delay={0.35}>
          <div className="mb-4 flex items-center justify-between">
            <h2 className="font-display font-semibold text-xl uppercase tracking-wider text-whitePrimary">Upcoming Races</h2>
            <p className="section-label">Next 6</p>
          </div>

          {loading ? (
            <div className="space-y-2">
              {[...Array(4)].map((_, i) => (
                <SkeletonLoader key={i} height="60px" />
              ))}
            </div>
          ) : (
            <div className="space-y-2">
              {upcomingRaces.slice(0, 6).map((race) => (
                <div key={race.raceId} className="rounded-xl2 border border-white/5 bg-bgElevated px-4 py-3">
                  <div className="flex items-center justify-between gap-4">
                    <div>
                      <p className="font-mono text-sm font-semibold text-whitePrimary">R{race.round} · {race.raceName}</p>
                      <p className="text-xs text-whiteMuted">{race.circuitName} · {race.location}, {race.country}</p>
                    </div>
                    <p className="text-xs text-whiteMuted">{race.date}</p>
                  </div>
                </div>
              ))}
            </div>
          )}
        </Card>
      ) : (
        <SkeletonLoader height="400px" />
      )}
    </div>
  );
};

/**
 * Main Dashboard Component
 * 
 * CRITICAL: This component now splits rendering into:
 * 1. Static hero (renders immediately)
 * 2. Stat cards (load from preloaded promises)
 * 3. Charts (deferred 100ms, rendered with skeletons)
 * 4. Upcoming races (lazy-loaded on scroll)
 * 
 * This massively improves LCP and overall performance.
 */
const Dashboard = () => {
  usePageTitle("Dashboard");

  // Use preloaded promises from main.jsx for faster data availability
  const [drivers, setDrivers] = useState(null);
  const [races, setRaces] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    Promise.all([dashboardDataPromise, racesDataPromise])
      .then(([driversData, racesData]) => {
        setDrivers(driversData);
        setRaces(racesData);
        setLoading(false);
      })
      .catch((err) => {
        setError(err.message);
        setLoading(false);
      });
  }, []);

  // Fallback error state
  if (error && !drivers && !races) {
    return (
      <ErrorState
        message={error}
        onRetry={() => window.location.reload()}
      />
    );
  }

  return (
    <div className="space-y-6">
      {/* PART 1: Hero renders immediately */}
      <HeroSection />

      {/* PART 2: Stat cards load from preloaded promises */}
      <StatCardsSection drivers={drivers} races={races} loading={loading} />

      {/* PART 3: Charts deferred 100ms for LCP */}
      <ChartsSection drivers={drivers} races={races} loading={loading} />

      {/* PART 4: Below-fold content lazy-loaded on scroll */}
      <UpcomingRacesSection races={races} loading={loading} />
    </div>
  );
};

export default Dashboard;


