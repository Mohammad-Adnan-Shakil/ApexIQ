import { useState, useEffect, useMemo } from "react";
import { motion } from "framer-motion";
import { 
  LineChart, 
  Line, 
  AreaChart, 
  Area, 
  XAxis, 
  YAxis, 
  CartesianGrid, 
  Tooltip, 
  Legend, 
  ResponsiveContainer,
  BarChart,
  Bar
} from "recharts";
import { 
  Calendar, 
  Car, 
  Gauge, 
  Users, 
  TrendingUp, 
  Activity, 
  Zap,
  MessageCircle,
  AlertCircle,
  Send,
  X,
  Bot
} from "lucide-react";
import { Card, LoadingState, ErrorState } from "../components/common";
import useFetch from "../hooks/useFetch";
import usePageTitle from "../hooks/usePageTitle";
import api from "../services/api";

const TelemetryPage = () => {
  usePageTitle("Telemetry Intelligence");
  
  // Form state for race/session selection
  const [formData, setFormData] = useState({
    season: "2026",
    sessionKey: "latest",
    meetingKey: "latest",
    driverNumber: "1"
  });

  // Telemetry data state
  const [telemetryData, setTelemetryData] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  // Fetch dropdown data
  const { data: seasons } = useFetch("/api/telemetry/seasons");
  const { data: meetings } = useFetch("/api/telemetry/meetings", {
    season: formData.season
  });
  const { data: drivers } = useFetch("/api/telemetry/drivers", {
    sessionKey: formData.sessionKey,
    meetingKey: formData.meetingKey
  });

  // Fetch telemetry data
  const fetchTelemetryData = async () => {
    setLoading(true);
    setError(null);
    
    try {
      const response = await api.get("/api/telemetry/data", {
        params: {
          sessionKey: formData.sessionKey,
          meetingKey: formData.meetingKey,
          driverNumber: formData.driverNumber
        }
      });

      if (response.data?.success) {
        setTelemetryData(response.data.data);
        setError(null);
      } else {
        setError(response.data?.message || "Failed to fetch telemetry data");
        setTelemetryData(null);
      }
    } catch (err) {
      console.error("Telemetry fetch error:", err);
      setError(err.response?.data?.message || err.message || "Failed to fetch telemetry data");
      setTelemetryData(null);
    } finally {
      setLoading(false);
    }
  };

  // Auto-fetch when form is complete
  useEffect(() => {
    if (formData.season && formData.sessionKey && formData.meetingKey && formData.driverNumber) {
      fetchTelemetryData();
    }
  }, [formData.season, formData.sessionKey, formData.meetingKey, formData.driverNumber]);

  // Parse telemetry data for charts
  const chartData = useMemo(() => {
    if (!telemetryData) return [];
    
    try {
      const telemetry = JSON.parse(telemetryData);
      return telemetry.distance?.map((dist, idx) => ({
        distance: Math.round(dist),
        speed: telemetry.speed?.[idx] || 0,
        throttle: telemetry.throttle?.[idx] || 0,
        brake: telemetry.brake?.[idx] || 0,
        gear: telemetry.gear?.[idx] || 0,
        drs: telemetry.drs?.[idx] || 0
      })) || [];
    } catch (e) {
      console.error("Error parsing telemetry data:", e);
      return [];
    }
  }, [telemetryData]);

  return (
    <div className="min-h-screen bg-bgPrimary text-whitePrimary p-6">
      <div className="max-w-7xl mx-auto">
        {/* Header */}
        <motion.div
          initial={{ opacity: 0, y: -20 }}
          animate={{ opacity: 1, y: 0 }}
          className="mb-8"
        >
          <h1 className="font-display font-bold text-4xl uppercase tracking-widest text-whitePrimary mb-2">
            Telemetry Intelligence
          </h1>
          <p className="text-lg text-whiteMuted mb-6">
            Advanced F1 telemetry analysis with AI-powered insights
          </p>
        </motion.div>

        {/* Selection Form */}
        <Card className="mb-8">
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
            {/* Season */}
            <div>
              <label className="block text-sm font-medium text-whiteMuted mb-2">Season</label>
              <select
                value={formData.season}
                onChange={(e) => setFormData(prev => ({ ...prev, season: e.target.value }))}
                className="surface-input w-full"
              >
                {seasons?.data?.map(season => (
                  <option key={season} value={season}>{season}</option>
                ))}
              </select>
            </div>

            {/* Session */}
            <div>
              <label className="block text-sm font-medium text-whiteMuted mb-2">Session</label>
              <select
                value={formData.sessionKey}
                onChange={(e) => setFormData(prev => ({ ...prev, sessionKey: e.target.value }))}
                className="surface-input w-full"
              >
                <option value="latest">Latest</option>
                {meetings?.data?.map(meeting => (
                  <option key={meeting} value={meeting}>{meeting}</option>
                ))}
              </select>
            </div>

            {/* Meeting */}
            <div>
              <label className="block text-sm font-medium text-whiteMuted mb-2">Meeting</label>
              <select
                value={formData.meetingKey}
                onChange={(e) => setFormData(prev => ({ ...prev, meetingKey: e.target.value }))}
                className="surface-input w-full"
              >
                {meetings?.data?.map(meeting => (
                  <option key={meeting} value={meeting}>{meeting}</option>
                ))}
              </select>
            </div>

            {/* Driver */}
            <div>
              <label className="block text-sm font-medium text-whiteMuted mb-2">Driver</label>
              <select
                value={formData.driverNumber}
                onChange={(e) => setFormData(prev => ({ ...prev, driverNumber: parseInt(e.target.value) }))}
                className="surface-input w-full"
              >
                {drivers?.data?.map((driver, index) => (
                  <option key={index + 1} value={index + 1}>{driver}</option>
                ))}
              </select>
            </div>
          </div>

          <div className="flex justify-center mt-6">
            <button
              onClick={fetchTelemetryData}
              disabled={loading}
              className="bg-accentRed hover:bg-red-500 disabled:bg-gray-700 text-white px-8 py-3 rounded-lg font-semibold transition-colors duration-200 flex items-center gap-2"
            >
              <Activity className="h-5 w-5" />
              {loading ? "Analyzing..." : "Analyze Telemetry"}
            </button>
          </div>
        </Card>

        {/* Loading State */}
        {loading && <LoadingState message="Loading telemetry data..." />}

        {/* Error State */}
        {error && !loading && (
          <ErrorState 
            message={error} 
            onRetry={fetchTelemetryData} 
          />
        )}

        {/* Charts Section */}
        {chartData.length > 0 && (
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            className="space-y-6"
          >
            {/* Speed Analysis */}
            <Card>
              <h3 className="text-lg font-semibold text-whitePrimary mb-4 flex items-center gap-2">
                <TrendingUp className="h-5 w-5 text-accentRed" />
                Speed Analysis
              </h3>
              <ResponsiveContainer width="100%" height={250}>
                <LineChart data={chartData}>
                  <CartesianGrid stroke="#222" />
                  <XAxis 
                    dataKey="distance" 
                    tick={{ fill: "#999", fontSize: 12 }} 
                    label="Distance (m)"
                  />
                  <YAxis 
                    tick={{ fill: "#999", fontSize: 12 }} 
                    label="Speed (km/h)"
                  />
                  <Tooltip 
                    contentStyle={{ 
                      backgroundColor: "rgba(17, 24, 39, 0.95)", 
                      border: "1px solid #E10600" 
                    }} 
                  />
                  <Line 
                    type="monotone" 
                    dataKey="speed" 
                    stroke="#E10600" 
                    strokeWidth={2} 
                    dot={{ fill: "#E10600" }} 
                  />
                </LineChart>
              </ResponsiveContainer>
            </Card>

            {/* Throttle Analysis */}
            <Card>
              <h3 className="text-lg font-semibold text-whitePrimary mb-4 flex items-center gap-2">
                <Gauge className="h-5 w-5 text-accentRed" />
                Throttle Analysis
              </h3>
              <ResponsiveContainer width="100%" height={250}>
                <LineChart data={chartData}>
                  <CartesianGrid stroke="#222" />
                  <XAxis 
                    dataKey="distance" 
                    tick={{ fill: "#999", fontSize: 12 }} 
                    label="Distance (m)"
                  />
                  <YAxis 
                    tick={{ fill: "#999", fontSize: 12 }} 
                    label="Throttle (%)"
                  />
                  <Tooltip 
                    contentStyle={{ 
                      backgroundColor: "rgba(17, 24, 39, 0.95)", 
                      border: "1px solid #E10600" 
                    }} 
                  />
                  <Line 
                    type="monotone" 
                    dataKey="throttle" 
                    stroke="#10B981" 
                    strokeWidth={2} 
                    dot={{ fill: "#10B981" }} 
                  />
                </LineChart>
              </ResponsiveContainer>
            </Card>

            {/* Brake Analysis */}
            <Card>
              <h3 className="text-lg font-semibold text-whitePrimary mb-4 flex items-center gap-2">
                <Car className="h-5 w-5 text-accentRed" />
                Brake Analysis
              </h3>
              <ResponsiveContainer width="100%" height={250}>
                <AreaChart data={chartData}>
                  <CartesianGrid stroke="#222" />
                  <XAxis 
                    dataKey="distance" 
                    tick={{ fill: "#999", fontSize: 12 }} 
                    label="Distance (m)"
                  />
                  <YAxis 
                    tick={{ fill: "#999", fontSize: 12 }} 
                    label="Brake Pressure"
                  />
                  <Tooltip 
                    contentStyle={{ 
                      backgroundColor: "rgba(17, 24, 39, 0.95)", 
                      border: "1px solid #E10600" 
                    }} 
                  />
                  <Area 
                    type="monotone" 
                    dataKey="brake" 
                    stroke="#F59E0B" 
                    fill="#F59E0B" 
                    fillOpacity={0.3} 
                  />
                </AreaChart>
              </ResponsiveContainer>
            </Card>

            {/* Gear Analysis */}
            <Card>
              <h3 className="text-lg font-semibold text-whitePrimary mb-4 flex items-center gap-2">
                <Users className="h-5 w-5 text-accentRed" />
                Gear Analysis
              </h3>
              <ResponsiveContainer width="100%" height={250}>
                <BarChart data={chartData}>
                  <CartesianGrid stroke="#222" />
                  <XAxis 
                    dataKey="distance" 
                    tick={{ fill: "#999", fontSize: 12 }} 
                    label="Distance (m)"
                  />
                  <YAxis 
                    tick={{ fill: "#999", fontSize: 12 }} 
                    label="Gear"
                  />
                  <Tooltip 
                    contentStyle={{ 
                      backgroundColor: "rgba(17, 24, 39, 0.95)", 
                      border: "1px solid #E10600" 
                    }} 
                  />
                  <Bar 
                    dataKey="gear" 
                    fill="#8B5CF6" 
                  />
                </BarChart>
              </ResponsiveContainer>
            </Card>

            {/* DRS Analysis */}
            <Card>
              <h3 className="text-lg font-semibold text-whitePrimary mb-4 flex items-center gap-2">
                <Zap className="h-5 w-5 text-accentRed" />
                DRS Analysis ({chartData.filter(point => point.drs === 1).length > 0 ? Math.round((chartData.filter(point => point.drs === 1).length / chartData.length) * 100) : 0}% active)
              </h3>
              <div className="bg-bgElevated rounded-lg p-4">
                <div className="text-center">
                  <div className="text-3xl font-bold text-accentRed mb-2">
                    {chartData.filter(point => point.drs === 1).length > 0 ? Math.round((chartData.filter(point => point.drs === 1).length / chartData.length) * 100) : 0}%
                  </div>
                  <p className="text-sm text-whiteMuted">of lap time with DRS deployed</p>
                </div>
                <div className="mt-4 space-y-2">
                  {chartData.map((point, idx) => (
                    <div 
                      key={idx} 
                      className={`h-2 w-full rounded ${point.drs === 1 ? 'bg-green-500' : 'bg-gray-700'}`}
                    />
                  ))}
                </div>
              </div>
            </Card>
          
          {/* Delta Analyst AI Chatbot */}
          <div className="fixed bottom-4 right-4 z-20 lg:bottom-6 lg:right-6">
            <motion.div
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.3, ease: "easeOut" }}
              className="bg-surface/90 backdrop-blur-xl rounded-lg shadow-2xl border border-borderSoft p-4 w-80 max-w-md"
            >
              <div className="flex items-center gap-2 mb-3">
                <Bot className="h-5 w-5 text-accentRed" />
                <h4 className="font-semibold text-whitePrimary">Delta Analyst</h4>
              </div>
              
              <div className="space-y-3 max-h-96 overflow-y-auto">
                <div className="text-sm text-whiteMuted mb-2">
                  Ask me anything about the telemetry data above. I can explain:
                </div>
                <ul className="text-xs text-whiteMuted space-y-1">
                  <li>• Driver performance patterns</li>
                  <li>• DRS usage analysis</li>
                  <li>• Braking points comparison</li>
                  <li>• Gear shift strategies</li>
                  <li>• Speed differentials</li>
                </ul>
              </div>
              
              <div className="border-t border-borderSoft pt-3">
                <div className="flex items-center gap-2 mb-2">
                  <input
                    type="text"
                    placeholder="Ask about telemetry data..."
                    className="flex-1 surface-input text-sm px-3 py-2 rounded-lg"
                  />
                  <button className="bg-accentRed hover:bg-red-600 text-white p-2 rounded-lg transition-colors">
                    <Send className="h-4 w-4" />
                  </button>
                </div>
                
                <div className="space-y-2">
                  <div className="flex items-start gap-2">
                    <div className="w-2 h-2 rounded-full bg-accentRed/20 flex items-center justify-center text-xs font-semibold text-accentRed">
                      AI
                    </div>
                    <div className="flex-1 text-sm text-whiteMuted">
                      <p>I'm analyzing the telemetry data above. Based on the speed trace, I can see this driver is maximizing DRS usage on the main straight, which is typical for this circuit.</p>
                    </div>
                  </div>
                </div>
              </div>
            </motion.div>
          </div>
        )}
      </div>
    </div>
  );
};

export default TelemetryPage;
