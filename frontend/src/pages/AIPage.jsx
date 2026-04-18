import { useEffect, useState } from "react";
import api from "../utils/axios";
import { motion } from "framer-motion";

const AIPage = () => {
  const [drivers, setDrivers] = useState([]);
  const [races, setRaces] = useState([]);

  const [selectedDriver, setSelectedDriver] = useState("");
  const [selectedRace, setSelectedRace] = useState("");
  const [position, setPosition] = useState(1);

  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState(null);
  const [animatedPosition, setAnimatedPosition] = useState(20);

  // 🔄 Load initial data
  useEffect(() => {
    const fetchData = async () => {
      try {
        const [driversRes, racesRes] = await Promise.all([
          api.get("/drivers"),
          api.get("/races"),
        ]);

        setDrivers(driversRes.data);
        setRaces(racesRes.data);
      } catch (err) {
        console.error(err);
      }
    };

    fetchData();
  }, []);

  // 🔢 Animate prediction
  useEffect(() => {
    if (!result) return;

    let start = 20;
    const end = result?.prediction?.predictedPosition;

    const interval = setInterval(() => {
      start--;

      if (start <= end) {
        start = end;
        clearInterval(interval);
      }

      setAnimatedPosition(start);
    }, 50);

    return () => clearInterval(interval);
  }, [result]);

  // 🧠 Run AI
  const runAI = async () => {
    if (!selectedDriver || !selectedRace) return;

    setLoading(true);
    setResult(null);

    try {
      const res = await api.post("/ai/intelligence", {
        driverId: selectedDriver,
        raceId: selectedRace,
        simulatedPosition: position,
      });

      setResult(res.data);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="flex gap-6 h-full">
      {/* LEFT PANEL */}
      <div className="w-[35%] surface space-y-6">
        <div>
          <h2 className="font-display text-2xl tracking-widePlus">
            INTELLIGENCE ENGINE
          </h2>
          <div className="racing-divider"></div>
        </div>

        {/* Driver */}
        <div>
          <label className="text-sm text-textSecondary">Driver</label>
          <select
            className="input-field"
            value={selectedDriver}
            onChange={(e) => setSelectedDriver(e.target.value)}
          >
            <option value="">Select Driver</option>
            {drivers.map((d) => (
              <option key={d.driverId} value={d.driverId}>
                {d.name} ({d.team})
              </option>
            ))}
          </select>
        </div>

        {/* Race */}
        <div>
          <label className="text-sm text-textSecondary">Race</label>
          <select
            className="input-field"
            value={selectedRace}
            onChange={(e) => setSelectedRace(e.target.value)}
          >
            <option value="">Select Race</option>
            {races.map((r) => (
              <option key={r.raceId} value={r.raceId}>
                {r.name} — {r.location}
              </option>
            ))}
          </select>
        </div>

        {/* Slider */}
        <div>
          <label className="text-sm text-textSecondary">
            Simulated Position
          </label>
          <div className="flex items-center gap-4">
            <input
              type="range"
              min="1"
              max="20"
              value={position}
              onChange={(e) => setPosition(e.target.value)}
              className="w-full"
            />
            <span className="font-mono text-xl">{position}</span>
          </div>
        </div>

        <button
          onClick={runAI}
          className="btn-primary w-full"
          disabled={loading}
        >
          {loading ? "RUNNING..." : "RUN INTELLIGENCE"}
        </button>
      </div>

      {/* RIGHT PANEL */}
      <motion.div
        className="w-[65%] space-y-6 overflow-y-auto"
        initial={{ opacity: 0, x: 50 }}
        animate={{ opacity: 1, x: 0 }}
        transition={{ duration: 0.5 }}
      >
        {!result && !loading && (
          <div className="card text-center text-textSecondary">
            Select inputs and run intelligence
          </div>
        )}

        {loading && (
          <div className="card text-center font-mono text-xl">
            ANALYZING...
          </div>
        )}

        {result && (
          <>
            {/* Prediction */}
            <motion.div className="card flex justify-between">
              <div>
                <p className="text-textSecondary">Predicted Position</p>
                <h1 className="text-6xl font-mono">
                  P{animatedPosition}
                </h1>
              </div>

              <div className="w-64">
                <p className="text-textSecondary mb-2">Confidence</p>
                <div className="w-full h-3 bg-border rounded-full">
                  <div
                    className="h-full bg-primary transition-all"
                    style={{
                      width: `${
                        (result?.prediction?.confidence || 0) * 100
                      }%`,
                    }}
                  ></div>
                </div>
                <p className="text-right font-mono mt-2">
                  {(
                    (result?.prediction?.confidence || 0) * 100
                  ).toFixed(0)}
                  %
                </p>
              </div>
            </motion.div>

            {/* Insights */}
            <div className="grid grid-cols-3 gap-4">
              <div className="card">
                <p className="text-textSecondary">Average Finish</p>
                <p className="stat-number">
                  {result?.insights?.averageFinish}
                </p>
              </div>

              <div className="card">
                <p className="text-textSecondary">Consistency</p>
                <p className="stat-number">
                  {result?.insights?.consistencyScore}
                </p>
              </div>

              <div className="card">
                <p className="text-textSecondary">Trend</p>
                <p className="stat-number">
                  {result?.insights?.trend}
                </p>
              </div>
            </div>

            {/* Simulation */}
            <div className="card">
              <p className="text-textSecondary">Simulation</p>
              <p>
                {result?.simulation?.oldAverage} →{" "}
                {result?.simulation?.newAverage}
              </p>
            </div>

            {/* Comparison */}
            <div className="card">
              <p className="text-textSecondary">Comparison</p>
              <p>
                {result?.comparison?.driverA} vs{" "}
                {result?.comparison?.driverB}
              </p>
            </div>

            {/* Summary */}
            <div className="card">
              <p>{result?.summary}</p>
            </div>
          </>
        )}
      </motion.div>
    </div>
  );
};

export default AIPage;