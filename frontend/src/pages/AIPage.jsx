import { useMemo, useState } from "react";
import { motion } from "framer-motion";
import {
  Brain,
  Cpu,
  Lightbulb,
  LoaderCircle,
  Trophy,
  TrendingDown,
  TrendingUp,
} from "lucide-react";
import { Card, Button, LoadingState, ErrorState, EmptyState } from "../components/common";
import { useFetch, usePost } from "../hooks/useFetch";
import usePageTitle from "../hooks/usePageTitle";
import { confidenceToPercent, formatImpact, impactIcon, roundAverage, roundPosition, teamColor } from "../utils/formatters";
import PredictionDistributionChart from "../components/PredictionDistributionChart";

const ConfidenceRing = ({ percent }) => {
  const radius = 46;
  const circumference = 2 * Math.PI * radius;
  const dash = (percent / 100) * circumference;

  return (
    <div className="relative h-28 w-28">
      <svg viewBox="0 0 120 120" className="h-full w-full -rotate-90">
        <circle cx="60" cy="60" r={radius} fill="none" stroke="rgba(255,255,255,0.12)" strokeWidth="10" />
        <circle
          cx="60"
          cy="60"
          r={radius}
          fill="none"
          stroke="#e8002d"
          strokeWidth="10"
          strokeLinecap="round"
          strokeDasharray={`${dash} ${circumference - dash}`}
        />
      </svg>
      <div className="absolute inset-0 flex items-center justify-center text-xl font-semibold text-whitePrimary">{percent}%</div>
    </div>
  );
};

const resultColorByPosition = (pos) => {
  if (pos <= 3) return "text-accentGold";
  if (pos <= 10) return "text-successGreen";
  return "text-whiteMuted";
};

const confidenceLabel = (percent) => {
  if (percent >= 60) return "High";
  if (percent >= 30) return "Moderate";
  if (percent >= 15) return "Low";
  return "Very Low";
};

const verdictForPosition = (position) => {
  if (position <= 3) return { icon: "🏆", label: "Podium Contender", className: "bg-accentGold/20 text-accentGold" };
  if (position <= 10) return { icon: "✅", label: "Points Finish", className: "bg-successGreen/20 text-successGreen" };
  if (position <= 15) return { icon: "⚠️", label: "Midfield Battle", className: "bg-white/15 text-whiteMuted" };
  return { icon: "❌", label: "Tough Race", className: "bg-accentRed/20 text-accentRed" };
};

const AIPage = () => {
  usePageTitle("AI Prediction");

  const { data: drivers, loading: driversLoading, error: driversError, refetch: refetchDrivers } = useFetch("/drivers");
  const { data: races, loading: racesLoading, error: racesError, refetch: refetchRaces } = useFetch("/races");
  const { execute: runPrediction, loading: predictionLoading } = usePost("/ai/intelligence");

  const [selectedDriver, setSelectedDriver] = useState("");
  const [selectedRace, setSelectedRace] = useState("");
  const [simulatedPosition, setSimulatedPosition] = useState(10);
  const [result, setResult] = useState(null);
  const [actionError, setActionError] = useState("");

  const driverList = drivers || [];
  const raceList = races || [];

  const selectedDriverData = useMemo(
    () => driverList.find((driver) => driver.driverId === Number(selectedDriver)),
    [driverList, selectedDriver]
  );
  const selectedRaceData = useMemo(
    () => raceList.find((race) => race.raceId === Number(selectedRace)),
    [raceList, selectedRace]
  );

  const upcomingRaces = useMemo(
    () => raceList.filter((race) => race.status !== "COMPLETED"),
    [raceList]
  );

  const setupLoading = driversLoading || racesLoading;
  const setupError = driversError || racesError;

  const handlePrediction = async () => {
    if (!selectedDriver || !selectedRace) {
      setActionError("Select driver and race before running prediction.");
      return;
    }

    try {
      setActionError("");
      const response = await runPrediction({
        driverId: Number(selectedDriver),
        raceId: Number(selectedRace),
        simulatedPosition,
      });
      setResult(response);
    } catch (err) {
      setActionError(err.message || "Prediction failed");
    }
  };

  if (setupLoading) return <LoadingState message="Loading AI prediction setup..." />;

  if (setupError) {
    return (
      <ErrorState
        message={setupError}
        onRetry={() => {
          refetchDrivers();
          refetchRaces();
        }}
      />
    );
  }

  if (!driverList.length || !raceList.length) {
    return <EmptyState title="No prediction inputs available" description="Driver or race data is empty." />;
  }

  const predictedRange = result?.prediction?.predictedRange || "P5–P10";
  const confidencePercent = confidenceToPercent(result?.prediction?.confidence);
  const roundedAvgFinish = roundPosition(result?.insights?.averageFinish);
  const consistencyPercent = confidenceToPercent(result?.insights?.consistencyScore);

  const trend = String(result?.prediction?.trend || result?.insights?.trend || "STABLE").toUpperCase();
  const trendImproving = trend === "IMPROVING";
  const trendDeclining = trend === "DECLINING";

  const simOld = roundAverage(result?.simulation?.oldAverage);
  const simNew = roundAverage(result?.simulation?.newAverage);
  const simImpact = result?.simulation?.impact;
  const verdict = verdictForPosition(Number(roundedAvgFinish || 20));

  const sliderPercent = ((simulatedPosition - 1) / 19) * 100;

  return (
    <div className="grid grid-cols-1 gap-4 xl:grid-cols-[360px_1fr]">
      <Card className="h-fit" delay={0.05}>
        <div className="mb-6 flex items-center gap-3">
          <div className="flex h-10 w-10 items-center justify-center rounded-full bg-accentRed/20 text-accentRed">
            <Brain className="h-5 w-5" />
          </div>
          <div>
            <p className="section-label">AI Intelligence</p>
            <p className="font-display font-semibold text-xl uppercase tracking-wider">Prediction Setup</p>
          </div>
        </div>

        <div className="space-y-4">
          <div>
            <label className="section-label mb-2 block">Driver</label>
            <div className="relative">
              <div className="pointer-events-none absolute left-3 top-1/2 flex h-7 w-7 -translate-y-1/2 items-center justify-center rounded-full bg-white/10 text-xs text-whiteMuted">
<span className="font-display font-bold uppercase tracking-wide">{selectedDriverData?.code?.slice(0, 2) || "DR"}</span>
              </div>
              <select
                value={selectedDriver}
                onChange={(e) => setSelectedDriver(e.target.value)}
                className="surface-input pl-12"
              >
                <option value="">Select driver</option>
                {driverList.map((driver) => (
                  <option key={driver.driverId} value={driver.driverId}>
                    {driver.name} ({driver.code || "DRV"})
                  </option>
                ))}
              </select>
            </div>
          </div>

          <div>
            <label className="section-label mb-2 block">Race</label>
            <select
              value={selectedRace}
              onChange={(e) => setSelectedRace(e.target.value)}
              className="surface-input"
            >
              <option value="">Select race</option>
              {upcomingRaces.map((race) => (
                <option key={race.raceId} value={race.raceId}>
                  R{race.round} - {race.raceName}
                </option>
              ))}
            </select>
          </div>

          <div>
            <div className="mb-3 flex items-center justify-between">
              <label className="section-label">Grid Position</label>
              <span className="text-xs text-whiteMuted">P1 - P20</span>
            </div>
            <div className="relative pt-7">
              <div
                className="absolute top-0 -translate-x-1/2 rounded-md bg-accentRed px-2 py-1 text-xs font-semibold text-white"
                style={{ left: `${sliderPercent}%` }}
              >
<span className="font-mono">P{simulatedPosition}</span>
              </div>
              <input
                type="range"
                min="1"
                max="20"
                value={simulatedPosition}
                onChange={(e) => setSimulatedPosition(Number(e.target.value))}
                className="h-2 w-full cursor-pointer appearance-none rounded-full bg-white/10 accent-accentRed"
              />
            </div>
          </div>

          {selectedDriverData ? (
            <div className="rounded-xl2 border border-borderSoft bg-bgElevated p-4">
              <p className="section-label mb-2">Driver Snapshot</p>
              <p className="text-lg font-semibold">{selectedDriverData.name}</p>
              <div className="mt-2 flex items-center gap-2 text-sm text-whiteMuted">
                <span className="h-2.5 w-2.5 rounded-full" style={{ backgroundColor: teamColor(selectedDriverData.team) }} />
                {selectedDriverData.team || "Unknown Team"}
              </div>
              <div className="mt-2 flex items-center gap-2 text-sm text-accentGold">
                <Trophy className="h-4 w-4" /> <span className="font-mono">{Math.round(selectedDriverData.points || 0)} pts</span>
              </div>
            </div>
          ) : null}

          {actionError ? <p className="text-sm text-accentRed">{actionError}</p> : null}

          <Button onClick={handlePrediction} disabled={predictionLoading} className="w-full" size="lg">
            {predictionLoading ? (
              <span className="flex items-center justify-center gap-2">
                <LoaderCircle className="h-4 w-4 animate-spin" /> Running prediction...
              </span>
            ) : (
              "RUN PREDICTION"
            )}
          </Button>
        </div>
      </Card>

      <div className="space-y-4">
        {!result && !predictionLoading ? (
          <Card delay={0.1} className="flex min-h-[460px] flex-col items-center justify-center text-center">
            <svg width="96" height="96" viewBox="0 0 96 96" fill="none" className="mb-4 opacity-70">
              <circle cx="48" cy="48" r="44" stroke="rgba(255,255,255,0.14)" strokeWidth="2" />
              <path d="M26 55C26 41 36 31 50 31C60 31 68 37 72 46" stroke="#e8002d" strokeWidth="4" strokeLinecap="round" />
              <path d="M24 56H74V65C74 68 72 70 69 70H29C26 70 24 68 24 65V56Z" fill="#141420" stroke="rgba(255,255,255,0.2)" />
            </svg>
            <p className="font-display font-bold text-4xl uppercase tracking-widest">Select driver and race to generate AI prediction</p>
            <p className="mt-2 max-w-md text-sm text-whiteMuted">
              Choose inputs from the panel to run XGBoost/Random Forest intelligence with confidence and simulation impact.
            </p>
          </Card>
        ) : null}

        {predictionLoading ? <LoadingState message="Running AI models..." /> : null}

        {result ? (
          <motion.div initial={{ opacity: 0, x: 40 }} animate={{ opacity: 1, x: 0 }} className="space-y-4">
            <Card delay={0.1} className="border-accentRed/40 bg-accentRed/10">
              <p className="text-sm text-whitePrimary leading-relaxed">
                Based on {selectedDriverData?.name || "this driver's"} recent form and starting from{" "}
<span className="font-mono font-semibold">P{simulatedPosition}</span>
                {selectedRaceData?.raceName ? ` at ${selectedRaceData.raceName}` : ""}, our models estimate a likely finish in the{" "}
<span className="font-mono font-semibold">{predictedRange}</span> range with{" "}
<span className="font-semibold">{confidencePercent}% confidence</span>.{" "}
{confidencePercent < 30
  ? `Very low confidence due to ${trendDeclining ? "declining performance" : "limited data or inconsistent performance"}. Use as a rough guide only.`
  : trendImproving
    ? "This driver is on an improving trend and looks set for a strong result."
    : trendDeclining
      ? "Recent trend is declining, so execution and strategy will be critical."
      : "Current trend is stable, with a result close to expected pace."}
              </p>
            </Card>

            <Card className="grid items-center gap-4 md:grid-cols-[1fr_auto]" delay={0.12}>
              <div>
                <p className="section-label">Predicted Finish</p>
                <p className={`font-mono mt-3 text-7xl font-bold tracking-tight ${resultColorByPosition(roundedAvgFinish)}`}>
                  {predictedRange}
                </p>
                {confidencePercent < 50 && (
                  <p className="mt-2 text-xs text-whiteMuted">
                    {confidencePercent < 30 ? "Wide range due to low confidence" : "Range based on moderate confidence"}
                  </p>
                )}
                <span className={`inline-flex mt-3 items-center gap-1 rounded-full px-3 py-1 text-xs font-semibold ${verdict.className}`}>
                  {verdict.icon} {verdict.label}
                </span>
              </div>
              <div className="text-center">
                <ConfidenceRing percent={confidencePercent} />
                <p className="mt-2 text-xs text-whiteMuted uppercase tracking-[0.2em]">
                  {confidenceLabel(confidencePercent)}
                </p>
                {confidencePercent < 30 && (
                  <p className="mt-1 text-xs text-accentRed">
                    Prediction unreliable
                  </p>
                )}
              </div>
            </Card>

            {result?.prediction?.probabilityDistribution && result.prediction.probabilityDistribution.length > 0 && (
              <PredictionDistributionChart data={result.prediction.probabilityDistribution} />
            )}

            {confidencePercent < 30 && result?.prediction?.uncertaintyFactors && result.prediction.uncertaintyFactors.length > 0 && (
              <Card delay={0.14} className="border-accentRed/30 bg-accentRed/5">
                <div className="flex items-start gap-3">
                  <div className="flex-shrink-0">
                    <p className="text-lg">⚠️</p>
                  </div>
                  <div className="flex-1">
                    <p className="section-label mb-2 text-accentRed">Why Low Confidence?</p>
                    <ul className="space-y-1">
                      {result.prediction.uncertaintyFactors.map((factor, idx) => (
                        <li key={idx} className="text-sm text-whiteMuted">
                          • {factor}
                        </li>
                      ))}
                    </ul>
                  </div>
                </div>
              </Card>
            )}

            <div className="grid grid-cols-1 gap-4 md:grid-cols-3">
              <Card delay={0.16}>
                <p className="section-label">AVG FINISH</p>
                <p className="font-mono mt-2 text-4xl font-bold">P{roundedAvgFinish}</p>
              </Card>

              <Card delay={0.2}>
                <p className="section-label">CONSISTENCY</p>
                <p className="font-mono mt-2 text-3xl font-bold">{consistencyPercent}%</p>
                <div className="mt-3 h-2 rounded-full bg-white/10">
                  <div className="h-2 rounded-full bg-successGreen" style={{ width: `${consistencyPercent}%` }} />
                </div>
              </Card>

              <Card delay={0.24}>
                <p className="section-label">TREND</p>
                <div className="mt-3 flex items-center gap-2">
                  {trendImproving ? (
                    <TrendingUp className="h-5 w-5 text-successGreen" />
                  ) : trendDeclining ? (
                    <TrendingDown className="h-5 w-5 text-accentRed" />
                  ) : (
                    <Cpu className="h-5 w-5 text-whiteMuted" />
                  )}
                  <p
                    className={`text-xl font-semibold ${
                      trendImproving ? "text-successGreen" : trendDeclining ? "text-accentRed" : "text-whiteMuted"
                    }`}
                  >
                    {trend}
                  </p>
                </div>
              </Card>
            </div>

            {result?.prediction?.performanceBreakdown && (
              <Card delay={0.26}>
                <p className="section-label">Performance Breakdown</p>
                <p className="text-xs text-whiteMuted mb-4">Prediction combines long-term skill, current season form, and recent race performance</p>
                <div className="grid grid-cols-2 gap-4 md:grid-cols-4">
                  <div className="rounded-xl border border-white/10 bg-black/20 p-3">
                    <p className="text-xs text-whiteMuted">Career Form</p>
                    <p className="font-mono mt-1 text-2xl font-bold text-whitePrimary">
                      P{result.prediction.performanceBreakdown.career?.toFixed(1) || "N/A"}
                    </p>
                  </div>
                  <div className="rounded-xl border border-white/10 bg-black/20 p-3">
                    <p className="text-xs text-whiteMuted">Season Form</p>
                    <p className="font-mono mt-1 text-2xl font-bold text-whitePrimary">
                      P{result.prediction.performanceBreakdown.season?.toFixed(1) || "N/A"}
                    </p>
                  </div>
                  <div className="rounded-xl border border-white/10 bg-black/20 p-3">
                    <p className="text-xs text-whiteMuted">Recent Form</p>
                    <p className="font-mono mt-1 text-2xl font-bold text-whitePrimary">
                      P{result.prediction.performanceBreakdown.recent?.toFixed(1) || "N/A"}
                    </p>
                  </div>
                  <div className="rounded-xl border border-accentRed/30 bg-accentRed/10 p-3">
                    <p className="text-xs text-accentRed">Weighted Avg</p>
                    <p className="font-mono mt-1 text-2xl font-bold text-accentRed">
                      P{result.prediction.performanceBreakdown.weighted?.toFixed(1) || "N/A"}
                    </p>
                  </div>
                </div>
              </Card>
            )}

            <Card delay={0.28}>
              <p className="section-label">What-if Simulation</p>
              <div className="mt-4 grid grid-cols-2 gap-4">
                <div className="rounded-xl2 border border-borderSoft bg-bgElevated p-4">
                  <p className="text-xs text-whiteMuted">CURRENT AVG</p>
                  <p className="font-mono mt-2 text-3xl font-bold text-whitePrimary">P{simOld}</p>
                </div>
                <div className="rounded-xl2 border border-borderSoft bg-bgElevated p-4">
                  <p className="text-xs text-whiteMuted">PROJECTED AVG</p>
                  <p className="font-mono mt-2 text-3xl font-bold text-successGreen">P{simNew}</p>
                </div>
              </div>
              <p className="mt-4 text-sm text-whiteMuted">
                {impactIcon(simImpact)} {formatImpact(simImpact)}
              </p>
              <p className="mt-2 text-sm text-whiteMuted">
                Starting from <span className="font-mono">P{simulatedPosition}</span> instead of the current average grid context is projected to{" "}
                {typeof simOld === "number" && typeof simNew === "number"
                  ? `${simNew > simOld ? "cost" : "improve"} about ${Math.abs((simNew - simOld)).toFixed(1)} positions on average finish`
                  : "have a measurable impact on average finish"}
                {selectedDriverData?.name ? ` for ${selectedDriverData.name}` : ""}.
              </p>
            </Card>

            <Card delay={0.32}>
              <div className="mb-3 flex items-center gap-2 text-accentGold">
                <Lightbulb className="h-4 w-4" />
                <p className="section-label">Why This Prediction?</p>
              </div>
              {result.topFeatures && result.topFeatures.length > 0 ? (
                <div className="space-y-3">
                  {result.topFeatures.map((feature, idx) => (
                    <div key={idx} className="rounded-lg border border-borderSoft bg-bgElevated p-3">
                      <div className="flex items-start justify-between mb-1">
                        <p className="text-sm font-semibold text-whitePrimary">{feature.explanation || `Factor ${idx + 1}`}</p>
                        <span className="text-xs px-2 py-1 rounded-full bg-accentRed/20 text-accentRed">
<span className="font-mono">{(feature.importance * 100).toFixed(1)}%</span>
                        </span>
                      </div>
                      <div className="h-1.5 rounded-full bg-white/10 mt-2">
                        <div 
                          className="h-1.5 rounded-full bg-accentRed" 
                          style={{ width: `${Math.min(100, (feature.importance * 100))}%` }}
                        />
                      </div>
                    </div>
                  ))}
                </div>
              ) : (
                <p className="text-sm text-whiteMuted">Feature analysis not available for this prediction.</p>
              )}
            </Card>

            <Card delay={0.36} className="border-accentRed/20 bg-accentRed/5">
              <div className="flex gap-3">
                <div className="flex-shrink-0">
                  <p className="text-xs font-bold text-accentRed">ℹ️</p>
                </div>
                <div>
                  <p className="text-xs font-semibold text-whitePrimary mb-1">Prediction Disclaimer</p>
                  <p className="text-xs text-whiteMuted leading-relaxed">
                    Predictions are AI-generated based on historical race data and statistical models. They represent educated estimates only and do not guarantee actual race outcomes. Many real-world variables (weather, safety cars, mechanical issues, tactical decisions) cannot be predicted. Use these results for entertainment and strategic planning only.
                  </p>
                </div>
              </div>
            </Card>
          </motion.div>
        ) : null}
      </div>
    </div>
  );
};

export default AIPage;