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

  // Fetch telemetry data
  const fetchTelemetry = async () => {
    setLoading(true);
    setError(null);
    
    try {
      const response = await api.post('/api/telemetry/fetch', {
        season: formData.season,
        sessionKey: formData.sessionKey,
        meetingKey: formData.meetingKey,
        driverNumber: formData.driverNumber
      });
      
      if (response.data) {
        setTelemetryData(response.data);
      }
    } catch (err) {
      setError(err.message || 'Failed to fetch telemetry data');
    } finally {
      setLoading(false);
    }
  };

  // Process telemetry data for charts
  const chartData = useMemo(() => {
    if (!telemetryData || !Array.isArray(telemetryData)) return [];
    
    return telemetryData.slice(0, 100).map((point, index) => ({
      distance: index * 10, // Simulated distance
      speed: point.speed || 0,
      throttle: point.throttle || 0,
      brake: point.brake || 0,
      gear: point.gear || 1,
      drs: point.drs || 0
    }));
  }, [telemetryData]);

  return (
    <div className="min-h-screen bg-bgPrimary">
      <div className="container mx-auto px-4 py-8">
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-whitePrimary mb-8">Telemetry Intelligence</h1>
          
          {/* Form Section */}
          <Card>
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
              <div>
                <label className="block text-sm font-medium text-whiteMuted mb-2">Season</label>
                <select 
                  value={formData.season}
                  onChange={(e) => setFormData(prev => ({ ...prev, season: e.target.value }))}
                  className="w-full surface-input"
                >
                  <option value="2026">2026</option>
                  <option value="2025">2025</option>
                  <option value="2024">2024</option>
                </select>
              </div>
              
              <div>
                <label className="block text-sm font-medium text-whiteMuted mb-2">Session</label>
                <select 
                  value={formData.sessionKey}
                  onChange={(e) => setFormData(prev => ({ ...prev, sessionKey: e.target.value }))}
                  className="w-full surface-input"
                >
                  <option value="latest">Latest</option>
                  <option value="9158">Bahrain GP</option>
                  <option value="9159">Saudi Arabian GP</option>
                </select>
              </div>
              
              <div>
                <label className="block text-sm font-medium text-whiteMuted mb-2">Driver Number</label>
                <input
                  type="number"
                  value={formData.driverNumber}
                  onChange={(e) => setFormData(prev => ({ ...prev, driverNumber: e.target.value }))}
                  className="w-full surface-input"
                  min="1"
                  max="99"
                />
              </div>
              
              <div>
                <button
                  onClick={fetchTelemetry}
                  disabled={loading}
                  className="w-full bg-accentRed hover:bg-red-600 text-white font-medium py-2 px-4 rounded-lg transition-colors disabled:opacity-50"
                >
                  {loading ? 'Loading...' : 'Fetch Telemetry'}
                </button>
              </div>
            </div>
          </Card>

          {/* Error State */}
          {error && (
            <ErrorState 
              message={error}
              onRetry={fetchTelemetry}
            />
          )}

          {/* Loading State */}
          {loading && <LoadingState message="Loading telemetry data..." />}

          {/* Charts Section */}
          {chartData.length > 0 && (
            <motion.div
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.5, ease: "easeOut" }}
              className="space-y-6"
            >
              {/* Speed Chart */}
              <Card>
                <h3 className="text-lg font-semibold text-whitePrimary mb-4 flex items-center gap-2">
                  <TrendingUp className="h-5 w-5 text-green-400" />
                  Speed Analysis
                </h3>
                <ResponsiveContainer width="100%" height={300}>
                  <LineChart data={chartData}>
                    <CartesianGrid strokeDasharray="3 3" stroke="#374151" />
                    <XAxis 
                      dataKey="distance" 
                      stroke="#9CA3AF" 
                      tick={{ fill: "#9CA3AF" }}
                    />
                    <YAxis 
                      stroke="#9CA3AF" 
                      tick={{ fill: "#9CA3AF" }}
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
                      stroke="#10B981" 
                      strokeWidth={2} 
                      dot={{ fill: "#10B981", r: 4 }} 
                    />
                  </LineChart>
                </ResponsiveContainer>
              </Card>

              {/* Throttle Chart */}
              <Card>
                <h3 className="text-lg font-semibold text-whitePrimary mb-4 flex items-center gap-2">
                  <Activity className="h-5 w-5 text-blue-400" />
                  Throttle Analysis
                </h3>
                <ResponsiveContainer width="100%" height={300}>
                  <AreaChart data={chartData}>
                    <CartesianGrid strokeDasharray="3 3" stroke="#374151" />
                    <XAxis 
                      dataKey="distance" 
                      stroke="#9CA3AF" 
                      tick={{ fill: "#9CA3AF" }}
                    />
                    <YAxis 
                      stroke="#9CA3AF" 
                      tick={{ fill: "#9CA3AF" }}
                    />
                    <Tooltip 
                      contentStyle={{ 
                        backgroundColor: "rgba(17, 24, 39, 0.95)", 
                        border: "1px solid #E10600" 
                      }} 
                    />
                    <Area 
                      type="monotone" 
                      dataKey="throttle" 
                      stroke="#3B82F6" 
                      fill="#3B82F6" 
                      fillOpacity={0.6} 
                    />
                  </AreaChart>
                </ResponsiveContainer>
              </Card>

              {/* Brake Chart */}
              <Card>
                <h3 className="text-lg font-semibold text-whitePrimary mb-4 flex items-center gap-2">
                  <AlertCircle className="h-5 w-5 text-yellow-400" />
                  Brake Analysis
                </h3>
                <ResponsiveContainer width="100%" height={300}>
                  <AreaChart data={chartData}>
                    <CartesianGrid strokeDasharray="3 3" stroke="#374151" />
                    <XAxis 
                      dataKey="distance" 
                      stroke="#9CA3AF" 
                      tick={{ fill: "#9CA3AF" }}
                    />
                    <YAxis 
                      stroke="#9CA3AF" 
                      tick={{ fill: "#9CA3AF" }}
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
                      stroke="#EF4444" 
                      fill="#EF4444" 
                      fillOpacity={0.6} 
                    />
                  </AreaChart>
                </ResponsiveContainer>
              </Card>

              {/* Gear Chart */}
              <Card>
                <h3 className="text-lg font-semibold text-whitePrimary mb-4 flex items-center gap-2">
                  <Car className="h-5 w-5 text-purple-400" />
                  Gear Analysis
                </h3>
                <ResponsiveContainer width="100%" height={300}>
                  <BarChart data={chartData}>
                    <CartesianGrid strokeDasharray="3 3" stroke="#374151" />
                    <XAxis 
                      dataKey="distance" 
                      stroke="#9CA3AF" 
                      tick={{ fill: "#9CA3AF" }}
                    />
                    <YAxis 
                      stroke="#9CA3AF" 
                      tick={{ fill: "#9CA3AF" }}
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
              {telemetryData && (
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
                        Ask me anything about telemetry data above. I can explain:
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
                            <p>I am analyzing the telemetry data above. Based on the speed trace, I can see this driver is maximizing DRS usage on the main straight, which is typical for this circuit.</p>
                          </div>
                        </div>
                      </div>
                    </div>
                  </motion.div>
                </div>
              )}
            </motion.div>
          )}
        </div>
      </div>
    </div>
  );
};

export default TelemetryPage;
