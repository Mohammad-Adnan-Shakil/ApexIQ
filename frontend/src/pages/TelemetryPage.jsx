import { useState, useEffect } from "react";
import { motion } from "framer-motion";
import { LineChart, Line, AreaChart, Area, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from "recharts";
import api from "../services/api";
import SkeletonLoader from "../components/SkeletonLoader";
import useFetch from "../hooks/useFetch";

const TelemetryPage = () => {
  // Form state
  const [formData, setFormData] = useState({
    year: 2024,
    grandPrix: "Monaco",
    sessionType: "Q",
    driver1: "VER",
    driver2: "LEC"
  });

  // Data state
  const [telemetryData, setTelemetryData] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  
  // Fetch dropdown data
  const { data: drivers } = useFetch("/drivers");
  const { data: races } = useFetch("/races");
  
  // Extract unique values for dropdowns
  const allYears = races ? [...new Set(races.map(r => r.season))] : [];
  const defaultYears = [2018, 2019, 2020, 2021, 2022, 2023, 2024, 2025, 2026];
  const availableYears = [...new Set([...allYears, ...defaultYears])].sort((a, b) => b - a);
  const availableGrandPrix = races ? [...new Set(races.map(r => r.raceName))].sort() : ["Monaco"];
  const availableDrivers = drivers || [];

  // Page title
  useEffect(() => {
    document.title = "Telemetry | DeltaBox";
  }, []);

  /**
   * Format telemetry response into chart-ready data structure
   */
  const formatChartData = (rawData) => {
    if (!rawData || !rawData.distance) return [];

    return rawData.distance.map((dist, idx) => ({
      distance: Math.round(dist),
      driver1_speed: rawData.driver1_speed[idx],
      driver2_speed: rawData.driver2_speed[idx],
      driver1_throttle: rawData.driver1_throttle[idx],
      driver2_throttle: rawData.driver2_throttle[idx],
      driver1_brake: rawData.driver1_brake[idx],
      driver2_brake: rawData.driver2_brake[idx],
      driver1_gear: rawData.driver1_gear[idx],
      driver2_gear: rawData.driver2_gear[idx],
      delta: rawData.delta[idx]
    }));
  };

  /**
   * Calculate summary stats from telemetry
   */
  const getMaxDelta = () => {
    if (!telemetryData || !telemetryData.delta || telemetryData.delta.length === 0) return 0;
    const deltas = telemetryData.delta.map(d => Math.abs(d));
    return Math.max(...deltas).toFixed(2);
  };

  /**
   * Handle form input changes
   */
  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: name === "year" ? parseInt(value) : value
    }));
  };

  /**
   * Fetch telemetry data from API
   */
  const handleAnalyze = async () => {
    setError(null);
    setLoading(true);

    try {
      const response = await api.get("/telemetry/compare", {
        params: {
          year: formData.year,
          grandPrix: formData.grandPrix,
          sessionType: formData.sessionType,
          driver1: formData.driver1,
          driver2: formData.driver2
        }
      });

      const data = typeof response.data === 'string' ? JSON.parse(response.data) : response.data;
      
      if (data.error) {
        setError(data.error);
        setTelemetryData(null);
      } else {
        setTelemetryData(data);
        setError(null);
      }
    } catch (err) {
      console.error("❌ Telemetry error:", err);
      setError(err.response?.data?.error || err.message || "Failed to fetch telemetry data");
      setTelemetryData(null);
    } finally {
      setLoading(false);
    }
  };

  const chartData = telemetryData ? formatChartData(telemetryData) : [];

  return (
    <div className="min-h-screen bg-bgPrimary text-whitePrimary p-6">
      <div className="max-w-7xl mx-auto">
        {/* Header */}
        <motion.div 
          initial={{ opacity: 0, y: -20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5 }}
          className="mb-8"
        >
          <h1 className="font-display font-bold text-2xl uppercase tracking-widest text-whitePrimary mb-2 sm:text-3xl md:text-4xl">Telemetry Comparison</h1>
          <p className="text-xs text-whiteMuted sm:text-sm">Compare lap telemetry between two drivers</p>
        </motion.div>

        {/* Control Panel */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5, delay: 0.1 }}
          className="bg-gray-900 border border-gray-800 rounded-2xl p-6 mb-8"
        >
          <h2 className="text-red-500 font-black tracking-widest text-lg mb-6 uppercase">Session Parameters</h2>
          
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-5 gap-4 mb-6">
            {/* Year */}
            <div className="flex flex-col">
              <label className="text-gray-400 text-xs uppercase tracking-widest mb-2">Year</label>
              <select
                name="year"
                value={formData.year}
                onChange={handleInputChange}
                className="bg-gray-800 border border-gray-700 text-white rounded-lg px-4 py-3 w-full appearance-none focus:border-red-500 outline-none font-medium"
              >
                {availableYears.map(year => (
                  <option key={year} value={year}>{year}</option>
                ))}
              </select>
            </div>

            {/* Grand Prix */}
            <div className="flex flex-col">
              <label className="text-gray-400 text-xs uppercase tracking-widest mb-2">Grand Prix</label>
              <select
                name="grandPrix"
                value={formData.grandPrix}
                onChange={handleInputChange}
                className="bg-gray-800 border border-gray-700 text-white rounded-lg px-4 py-3 w-full appearance-none focus:border-red-500 outline-none font-medium"
              >
                {availableGrandPrix.map(gp => (
                  <option key={gp} value={gp}>{gp}</option>
                ))}
              </select>
            </div>

            {/* Session Type */}
            <div className="flex flex-col">
              <label className="text-gray-400 text-xs uppercase tracking-widest mb-2">Session</label>
              <select
                name="sessionType"
                value={formData.sessionType}
                onChange={handleInputChange}
                className="bg-gray-800 border border-gray-700 text-white rounded-lg px-4 py-3 w-full appearance-none focus:border-red-500 outline-none font-medium"
              >
                <option value="Q">Qualifying</option>
                <option value="R">Race</option>
                <option value="FP1">FP1</option>
                <option value="FP2">FP2</option>
                <option value="FP3">FP3</option>
              </select>
            </div>

            {/* Driver 1 */}
            <div className="flex flex-col">
              <label className="text-gray-400 text-xs uppercase tracking-widest mb-2">Driver 1</label>
              <select
                name="driver1"
                value={formData.driver1}
                onChange={handleInputChange}
                className="bg-gray-800 border border-gray-700 text-white rounded-lg px-4 py-3 w-full appearance-none focus:border-red-500 outline-none font-medium"
              >
                {availableDrivers.map(driver => (
                  <option key={driver.driverId} value={driver.code}>{driver.code} - {driver.name}</option>
                ))}
              </select>
            </div>

            {/* Driver 2 */}
            <div className="flex flex-col">
              <label className="text-gray-400 text-xs uppercase tracking-widest mb-2">Driver 2</label>
              <select
                name="driver2"
                value={formData.driver2}
                onChange={handleInputChange}
                className="bg-gray-800 border border-gray-700 text-white rounded-lg px-4 py-3 w-full appearance-none focus:border-red-500 outline-none font-medium"
              >
                {availableDrivers.map(driver => (
                  <option key={driver.driverId} value={driver.code}>{driver.code} - {driver.name}</option>
                ))}
              </select>
            </div>
          </div>

          {/* Analyze Button */}
          <button
            onClick={handleAnalyze}
            disabled={loading}
            className="w-full lg:w-auto bg-red-600 hover:bg-red-500 active:scale-95 py-4 rounded-xl font-black tracking-widest text-lg shadow-lg shadow-red-900/30 transition-all duration-150 flex items-center justify-center gap-2"
          >
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <path d="M13 2L3 14h9l-1 8 10-12h-9l1-8z"/>
            </svg>
            {loading ? "Analyzing..." : "ANALYZE"}
          </button>

          <div className="bg-gray-800/50 border border-gray-700 rounded-lg px-3 py-2 text-gray-500 text-xs flex items-center gap-2 mt-3">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <circle cx="12" cy="12" r="10"/>
              <path d="M12 16v-4"/>
              <path d="M12 8h.01"/>
            </svg>
            First load may take 20-30 seconds
          </div>
        </motion.div>

        {/* Loading State */}
        {loading && (
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            className="flex flex-col items-center justify-center py-12"
          >
            <SkeletonLoader className="w-12 h-12 mb-4" />
            <p className="text-whiteMuted">Loading telemetry data...</p>
          </motion.div>
        )}

        {/* Error State */}
        {error && !loading && (
          <motion.div
            initial={{ opacity: 0, y: 10 }}
            animate={{ opacity: 1, y: 0 }}
            className="bg-red-900/20 border border-red-600 text-red-200 px-4 py-3 rounded mb-6"
          >
            <p className="font-semibold">Error</p>
            <p className="text-sm mt-1">{error}</p>
          </motion.div>
        )}

        {/* Results Section */}
        {telemetryData && !loading && (
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            transition={{ duration: 0.5 }}
            className="space-y-6"
          >
            {/* Summary Bar */}
            <div className="bg-bgSecondary border border-red-900/30 rounded-lg p-4 flex items-center justify-between flex-wrap gap-4">
              <div className="flex items-center gap-6">
                <div>
                  <p className="text-xs text-whiteMuted">Driver 1</p>
                  <p className="font-display font-bold uppercase tracking-wide text-lg text-red-600">{formData.driver1}</p>
                  <p className="font-mono text-sm text-whiteMuted">{telemetryData.driver1_lap_time}</p>
                </div>
                <div className="text-red-600">●</div>
                <div>
                  <p className="text-xs text-whiteMuted">Driver 2</p>
                  <p className="font-display font-bold uppercase tracking-wide text-lg text-blue-600">{formData.driver2}</p>
                  <p className="font-mono text-sm text-whiteMuted">{telemetryData.driver2_lap_time}</p>
                </div>
              </div>
              <div className="text-right">
                <p className="text-xs text-whiteMuted">Max Gap</p>
                <p className="font-mono text-lg font-bold text-whitePrimary">{getMaxDelta()}s</p>
              </div>
            </div>

            {/* Charts - Stacked */}
            <div className="space-y-6">
              {/* Speed Chart */}
              <motion.div
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ delay: 0.1 }}
                className="bg-bgSecondary border border-red-900/30 rounded-lg p-4"
              >
                <h3 className="font-display font-semibold text-xl uppercase tracking-wider mb-4 text-whitePrimary">Speed (km/h)</h3>
                <ResponsiveContainer width="100%" height={180}>
                  <LineChart data={chartData}>
                    <CartesianGrid stroke="#222" />
                    <XAxis dataKey="distance" tick={{ fill: "#999", fontSize: 12 }} />
                    <YAxis tick={{ fill: "#999", fontSize: 12 }} />
                    <Tooltip contentStyle={{ backgroundColor: "#1a1a1e", borderColor: "#E10600" }} />
                    <Legend />
                    <Line type="monotone" dataKey="driver1_speed" stroke="#E10600" name={formData.driver1} isAnimationActive={false} />
                    <Line type="monotone" dataKey="driver2_speed" stroke="#3B82F6" name={formData.driver2} isAnimationActive={false} />
                  </LineChart>
                </ResponsiveContainer>
              </motion.div>

              {/* Throttle Chart */}
              <motion.div
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ delay: 0.2 }}
                className="bg-bgSecondary border border-red-900/30 rounded-lg p-4"
              >
                <h3 className="font-display font-semibold text-xl uppercase tracking-wider mb-4 text-whitePrimary">Throttle (%)</h3>
                <ResponsiveContainer width="100%" height={180}>
                  <LineChart data={chartData}>
                    <CartesianGrid stroke="#222" />
                    <XAxis dataKey="distance" tick={{ fill: "#999", fontSize: 12 }} />
                    <YAxis tick={{ fill: "#999", fontSize: 12 }} />
                    <Tooltip contentStyle={{ backgroundColor: "#1a1a1e", borderColor: "#E10600" }} />
                    <Legend />
                    <Line type="monotone" dataKey="driver1_throttle" stroke="#E10600" name={formData.driver1} isAnimationActive={false} />
                    <Line type="monotone" dataKey="driver2_throttle" stroke="#3B82F6" name={formData.driver2} isAnimationActive={false} />
                  </LineChart>
                </ResponsiveContainer>
              </motion.div>

              {/* Brake Chart */}
              <motion.div
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ delay: 0.3 }}
                className="bg-bgSecondary border border-red-900/30 rounded-lg p-4"
              >
                <h3 className="font-display font-semibold text-xl uppercase tracking-wider mb-4 text-whitePrimary">Brake (0/1)</h3>
                <ResponsiveContainer width="100%" height={180}>
                  <LineChart data={chartData}>
                    <CartesianGrid stroke="#222" />
                    <XAxis dataKey="distance" tick={{ fill: "#999", fontSize: 12 }} />
                    <YAxis tick={{ fill: "#999", fontSize: 12 }} />
                    <Tooltip contentStyle={{ backgroundColor: "#1a1a1e", borderColor: "#E10600" }} />
                    <Legend />
                    <Line type="stepAfter" dataKey="driver1_brake" stroke="#E10600" name={formData.driver1} isAnimationActive={false} />
                    <Line type="stepAfter" dataKey="driver2_brake" stroke="#3B82F6" name={formData.driver2} isAnimationActive={false} />
                  </LineChart>
                </ResponsiveContainer>
              </motion.div>

              {/* Gear Chart */}
              <motion.div
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ delay: 0.4 }}
                className="bg-bgSecondary border border-red-900/30 rounded-lg p-4"
              >
                <h3 className="font-display font-semibold text-xl uppercase tracking-wider mb-4 text-whitePrimary">Gear</h3>
                <ResponsiveContainer width="100%" height={180}>
                  <LineChart data={chartData}>
                    <CartesianGrid stroke="#222" />
                    <XAxis dataKey="distance" tick={{ fill: "#999", fontSize: 12 }} />
                    <YAxis tick={{ fill: "#999", fontSize: 12 }} />
                    <Tooltip contentStyle={{ backgroundColor: "#1a1a1e", borderColor: "#E10600" }} />
                    <Legend />
                    <Line type="stepAfter" dataKey="driver1_gear" stroke="#E10600" name={formData.driver1} isAnimationActive={false} />
                    <Line type="stepAfter" dataKey="driver2_gear" stroke="#3B82F6" name={formData.driver2} isAnimationActive={false} />
                  </LineChart>
                </ResponsiveContainer>
              </motion.div>

              {/* Delta Chart */}
              <motion.div
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ delay: 0.5 }}
                className="bg-bgSecondary border border-red-900/30 rounded-lg p-4"
              >
                <h3 className="font-display font-semibold text-xl uppercase tracking-wider mb-4 text-whitePrimary">Time Delta (s)</h3>
                <ResponsiveContainer width="100%" height={180}>
                  <AreaChart data={chartData}>
                    <CartesianGrid stroke="#222" />
                    <XAxis dataKey="distance" tick={{ fill: "#999", fontSize: 12 }} />
                    <YAxis tick={{ fill: "#999", fontSize: 12 }} />
                    <Tooltip contentStyle={{ backgroundColor: "#1a1a1e", borderColor: "#E10600" }} />
                    <Area
                      type="monotone"
                      dataKey="delta"
                      stroke="#E10600"
                      fill="#E10600"
                      fillOpacity={0.1}
                      isAnimationActive={false}
                    />
                  </AreaChart>
                </ResponsiveContainer>
              </motion.div>
            </div>
          </motion.div>
        )}

        {/* Empty State */}
        {!telemetryData && !loading && !error && (
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            className="text-center py-12 text-whiteMuted"
          >
            <p>Select parameters and click "Analyze" to compare telemetry</p>
          </motion.div>
        )}
      </div>
    </div>
  );
};

export default TelemetryPage;
