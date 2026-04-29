import { useState, useEffect } from "react";
import { Brain, TrendingUp, TrendingDown, Award } from "lucide-react";

const ComparePage = () => {
  const [drivers, setDrivers] = useState([]);
  const [races, setRaces] = useState([]);
  const [selectedDriverA, setSelectedDriverA] = useState("");
  const [selectedDriverB, setSelectedDriverB] = useState("");
  const [selectedRace, setSelectedRace] = useState("");
  const [gridA, setGridA] = useState(1);
  const [gridB, setGridB] = useState(1);
  const [comparison, setComparison] = useState(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    // Fetch drivers
    fetch("http://localhost:8080/api/drivers")
      .then((res) => res.json())
      .then((data) => setDrivers(data))
      .catch((err) => console.error("Failed to fetch drivers:", err));

    // Fetch races
    fetch("http://localhost:8080/api/races/upcoming")
      .then((res) => res.json())
      .then((data) => setRaces(data))
      .catch((err) => console.error("Failed to fetch races:", err));
  }, []);

  const handleCompare = async () => {
    if (!selectedDriverA || !selectedDriverB || !selectedRace) {
      alert("Please select both drivers and a race");
      return;
    }

    setLoading(true);
    try {
      const response = await fetch("http://localhost:8080/api/ai/compare", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          driverAId: parseInt(selectedDriverA),
          driverBId: parseInt(selectedDriverB),
          gridA,
          gridB,
          raceId: parseInt(selectedRace),
        }),
      });

      const data = await response.json();
      if (response.ok) {
        setComparison(data);
      } else {
        alert("Comparison failed: " + data.message);
      }
    } catch (err) {
      alert("Error: " + err.message);
    } finally {
      setLoading(false);
    }
  };

  const getDriverName = (id) => {
    const driver = drivers.find((d) => d.driverId === parseInt(id));
    return driver ? driver.name : "Unknown";
  };

  const getDriverCode = (id) => {
    const driver = drivers.find((d) => d.driverId === parseInt(id));
    return driver ? driver.code : "DR";
  };

  const confidenceLabel = (confidence) => {
    if (confidence < 20) return "Very Low";
    if (confidence < 40) return "Low";
    if (confidence < 60) return "Moderate";
    if (confidence < 80) return "High";
    return "Very High";
  };

  return (
    <div className="min-h-screen bg-bgPrimary p-6">
      <div className="mx-auto max-w-6xl">
        <div className="mb-8 flex items-center gap-3">
          <div className="flex h-12 w-12 items-center justify-center rounded-full bg-accentRed/20 text-accentRed">
            <Brain className="h-6 w-6" />
          </div>
          <div>
            <h1 className="font-display text-3xl font-bold uppercase tracking-wider text-whitePrimary">
              Driver Comparison
            </h1>
            <p className="text-whiteMuted">Head-to-head AI prediction</p>
          </div>
        </div>

        <div className="mb-8 rounded-xl border border-white/10 bg-black/40 p-6">
          <div className="mb-6 grid grid-cols-1 gap-6 md:grid-cols-2">
            <div>
              <label className="mb-2 block text-sm font-semibold text-whitePrimary">
                Driver A
              </label>
              <select
                value={selectedDriverA}
                onChange={(e) => setSelectedDriverA(e.target.value)}
                className="w-full rounded-lg border border-white/10 bg-bgElevated p-3 text-whitePrimary"
              >
                <option value="">Select driver</option>
                {drivers.map((driver) => (
                  <option key={driver.driverId} value={driver.driverId}>
                    {driver.name} ({driver.code || "DRV"})
                  </option>
                ))}
              </select>
            </div>
            <div>
              <label className="mb-2 block text-sm font-semibold text-whitePrimary">
                Driver B
              </label>
              <select
                value={selectedDriverB}
                onChange={(e) => setSelectedDriverB(e.target.value)}
                className="w-full rounded-lg border border-white/10 bg-bgElevated p-3 text-whitePrimary"
              >
                <option value="">Select driver</option>
                {drivers.map((driver) => (
                  <option key={driver.driverId} value={driver.driverId}>
                    {driver.name} ({driver.code || "DRV"})
                  </option>
                ))}
              </select>
            </div>
          </div>

          <div className="mb-6 grid grid-cols-1 gap-6 md:grid-cols-2">
            <div>
              <label className="mb-2 block text-sm font-semibold text-whitePrimary">
                Grid Position A
              </label>
              <input
                type="number"
                min="1"
                max="20"
                value={gridA}
                onChange={(e) => setGridA(parseInt(e.target.value))}
                className="w-full rounded-lg border border-white/10 bg-bgElevated p-3 text-whitePrimary"
              />
            </div>
            <div>
              <label className="mb-2 block text-sm font-semibold text-whitePrimary">
                Grid Position B
              </label>
              <input
                type="number"
                min="1"
                max="20"
                value={gridB}
                onChange={(e) => setGridB(parseInt(e.target.value))}
                className="w-full rounded-lg border border-white/10 bg-bgElevated p-3 text-whitePrimary"
              />
            </div>
          </div>

          <div className="mb-6">
            <label className="mb-2 block text-sm font-semibold text-whitePrimary">
              Race
            </label>
            <select
              value={selectedRace}
              onChange={(e) => setSelectedRace(e.target.value)}
              className="w-full rounded-lg border border-white/10 bg-bgElevated p-3 text-whitePrimary"
            >
              <option value="">Select race</option>
              {races.map((race) => (
                <option key={race.raceId} value={race.raceId}>
                  R{race.round} - {race.raceName}
                </option>
              ))}
            </select>
          </div>

          <button
            onClick={handleCompare}
            disabled={loading}
            className="w-full rounded-lg bg-accentRed px-6 py-3 font-semibold text-whitePrimary transition-colors hover:bg-accentRed/80 disabled:opacity-50"
          >
            {loading ? "Comparing..." : "Compare Drivers"}
          </button>
        </div>

        {comparison && (
          <div className="space-y-6">
            {comparison.lowConfidenceWarning && (
              <div className="rounded-lg border border-accentRed/30 bg-accentRed/5 p-4">
                <p className="text-sm font-semibold text-accentRed">
                  ⚠️ {comparison.lowConfidenceWarning}
                </p>
              </div>
            )}

            <div className="rounded-xl border border-white/10 bg-black/40 p-6">
              <h2 className="mb-6 text-2xl font-bold text-whitePrimary">
                {getDriverName(selectedDriverA)} vs {getDriverName(selectedDriverB)}
              </h2>

              <div className="mb-6">
                <h3 className="mb-4 text-lg font-semibold text-whitePrimary">
                  Win Probability
                </h3>
                <div className="mb-4 flex h-8 rounded-lg overflow-hidden">
                  <div
                    className="bg-accentRed flex items-center justify-center text-sm font-bold text-white"
                    style={{
                      width: `${comparison.driverA.winProbability * 100}%`,
                    }}
                  >
                    {(comparison.driverA.winProbability * 100).toFixed(1)}%
                  </div>
                  <div
                    className="bg-accentGold flex items-center justify-center text-sm font-bold text-black"
                    style={{
                      width: `${comparison.driverB.winProbability * 100}%`,
                    }}
                  >
                    {(comparison.driverB.winProbability * 100).toFixed(1)}%
                  </div>
                </div>
              </div>

              <div className="mb-6 grid grid-cols-1 gap-6 md:grid-cols-2">
                <div className="rounded-lg border border-white/10 bg-black/20 p-4">
                  <div className="mb-3 flex items-center gap-2">
                    <div className="flex h-8 w-8 items-center justify-center rounded-full bg-accentRed/20 text-xs font-bold text-accentRed">
                      {getDriverCode(selectedDriverA)}
                    </div>
                    <h4 className="font-semibold text-whitePrimary">
                      {getDriverName(selectedDriverA)}
                    </h4>
                  </div>
                  <div className="space-y-2">
                    <div>
                      <p className="text-xs text-whiteMuted">Likely Finish</p>
                      <p className="font-mono text-xl font-bold text-whitePrimary">
                        {comparison.driverA.range}
                      </p>
                    </div>
                    <div>
                      <p className="text-xs text-whiteMuted">Confidence</p>
                      <p className="font-mono text-lg font-semibold text-whitePrimary">
                        {confidenceLabel(comparison.driverA.confidence)}
                      </p>
                    </div>
                  </div>
                </div>

                <div className="rounded-lg border border-white/10 bg-black/20 p-4">
                  <div className="mb-3 flex items-center gap-2">
                    <div className="flex h-8 w-8 items-center justify-center rounded-full bg-accentGold/20 text-xs font-bold text-accentGold">
                      {getDriverCode(selectedDriverB)}
                    </div>
                    <h4 className="font-semibold text-whitePrimary">
                      {getDriverName(selectedDriverB)}
                    </h4>
                  </div>
                  <div className="space-y-2">
                    <div>
                      <p className="text-xs text-whiteMuted">Likely Finish</p>
                      <p className="font-mono text-xl font-bold text-whitePrimary">
                        {comparison.driverB.range}
                      </p>
                    </div>
                    <div>
                      <p className="text-xs text-whiteMuted">Confidence</p>
                      <p className="font-mono text-lg font-semibold text-whitePrimary">
                        {confidenceLabel(comparison.driverB.confidence)}
                      </p>
                    </div>
                  </div>
                </div>
              </div>

              <div className="rounded-lg border border-successGreen/30 bg-successGreen/5 p-4">
                <div className="flex items-center gap-3">
                  <Award className="h-6 w-6 text-successGreen" />
                  <div>
                    <p className="text-sm text-whiteMuted">🏆 Likely Winner</p>
                    <p className="text-xl font-bold text-successGreen">
                      {comparison.winner}
                    </p>
                  </div>
                </div>
              </div>
            </div>

            {comparison.driverA.insights && comparison.driverA.insights.length > 0 && (
              <div className="rounded-xl border border-white/10 bg-black/40 p-6">
                <h3 className="mb-4 text-lg font-semibold text-whitePrimary">
                  Driver A Insights
                </h3>
                <ul className="space-y-2">
                  {comparison.driverA.insights.map((insight, idx) => (
                    <li key={idx} className="text-sm text-whiteMuted">
                      • {insight}
                    </li>
                  ))}
                </ul>
              </div>
            )}

            {comparison.driverB.insights && comparison.driverB.insights.length > 0 && (
              <div className="rounded-xl border border-white/10 bg-black/40 p-6">
                <h3 className="mb-4 text-lg font-semibold text-whitePrimary">
                  Driver B Insights
                </h3>
                <ul className="space-y-2">
                  {comparison.driverB.insights.map((insight, idx) => (
                    <li key={idx} className="text-sm text-whiteMuted">
                      • {insight}
                    </li>
                  ))}
                </ul>
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  );
};

export default ComparePage;
