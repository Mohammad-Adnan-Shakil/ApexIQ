import { useState, useEffect, useRef } from "react";
import { motion } from "framer-motion";
import api from "../services/api";
import { Radio, Send } from "lucide-react";

const RaceEngineerPage = () => {
  // Form state
  const [raceContext, setRaceContext] = useState({
    lap: 37,
    totalLaps: 57,
    position: 3,
    gapToLeader: "+12.4s",
    tyreCompound: "SOFT",
    tyreAge: 18,
    fuelLoad: 31.4,
    weather: "Dry",
    lastLapTime: "1:22.847"
  });

  // Chat state
  const [driverMessage, setDriverMessage] = useState("");
  const [conversation, setConversation] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const conversationEndRef = useRef(null);

  // Page title
  useEffect(() => {
    document.title = "Race Engineer | DeltaBox";
  }, []);

  // Auto-scroll to latest message
  useEffect(() => {
    conversationEndRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [conversation]);

  /**
   * Handle form field changes
   */
  const handleContextChange = (e) => {
    const { name, value } = e.target;
    setRaceContext(prev => ({
      ...prev,
      [name]: name === "lap" || name === "totalLaps" || name === "position" || name === "tyreAge" 
        ? parseInt(value) 
        : name === "fuelLoad" 
          ? parseFloat(value) 
          : value
    }));
  };

  /**
   * Submit message to engineer
   */
  const handleTransmit = async (e) => {
    e.preventDefault();
    
    if (!driverMessage.trim()) return;

    setError(null);
    setLoading(true);

    try {
      // Add driver message to conversation
      const updatedConversation = [
        ...conversation,
        { role: "driver", message: driverMessage }
      ];
      setConversation(updatedConversation);
      setDriverMessage("");

      // Call API with race context + driver message
      const response = await api.post("/race-engineer/ask", {
        ...raceContext,
        driverMessage: driverMessage
      });

      if (response.data.error) {
        // Add error message
        setError(response.data.error);
        setConversation(prev => [
          ...prev,
          { role: "engineer", message: `⚠️ ${response.data.error}`, isError: true }
        ]);
      } else {
        // Add engineer response
        setConversation(prev => [
          ...prev,
          { role: "engineer", message: response.data.response }
        ]);
      }
    } catch (err) {
      console.error("❌ Engineer error:", err);
      const errorMsg = err.response?.data?.error || err.message || "Failed to get engineer advice";
      setError(errorMsg);
      setConversation(prev => [
        ...prev,
        { role: "engineer", message: `⚠️ ${errorMsg}`, isError: true }
      ]);
    } finally {
      setLoading(false);
    }
  };

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
          <div className="flex items-center gap-3 mb-2">
            <Radio className="w-8 h-8 text-red-600" />
            <h1 className="font-display font-bold text-4xl uppercase tracking-widest text-whitePrimary">Race Engineer</h1>
          </div>
          <p className="text-whiteMuted">AI-powered pit wall strategy — powered by DeepSeek R1</p>
          
          {/* Status Indicator */}
          <div className="flex items-center gap-2 mt-4">
            <div className="w-3 h-3 bg-green-500 rounded-full animate-pulse" />
            <span className="text-sm text-green-400">ENGINEER ONLINE</span>
          </div>
        </motion.div>

        {/* Main Content - Two Columns */}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* Left Column - Race Context Form */}
          <motion.div
            initial={{ opacity: 0, x: -20 }}
            animate={{ opacity: 1, x: 0 }}
            transition={{ duration: 0.5, delay: 0.1 }}
            className="lg:col-span-1"
          >
            <div className="bg-bgSecondary border border-red-900/30 rounded-lg p-6 sticky top-6">
              <h2 className="font-display font-semibold text-xl uppercase tracking-wider mb-4 text-whitePrimary">Race Status</h2>
              
              <div className="space-y-4">
                {/* Lap */}
                <div className="grid grid-cols-2 gap-3">
                  <div className="flex flex-col">
                    <label className="text-xs text-whiteMuted mb-1">Lap</label>
                    <input
                      type="number"
                      name="lap"
                      value={raceContext.lap}
                      onChange={handleContextChange}
                      className="font-mono bg-bgPrimary border border-red-600/30 rounded px-2 py-1 text-white text-sm focus:outline-none focus:border-red-600"
                    />
                  </div>
                  <div className="flex flex-col">
                    <label className="text-xs text-whiteMuted mb-1">Total</label>
                    <input
                      type="number"
                      name="totalLaps"
                      value={raceContext.totalLaps}
                      onChange={handleContextChange}
                      className="font-mono bg-bgPrimary border border-red-600/30 rounded px-2 py-1 text-white text-sm focus:outline-none focus:border-red-600"
                    />
                  </div>
                </div>

                {/* Position & Gap */}
                <div className="grid grid-cols-2 gap-3">
                  <div className="flex flex-col">
                    <label className="text-xs text-whiteMuted mb-1">Position</label>
                    <input
                      type="number"
                      name="position"
                      value={raceContext.position}
                      onChange={handleContextChange}
                      className="font-mono bg-bgPrimary border border-red-600/30 rounded px-2 py-1 text-white text-sm focus:outline-none focus:border-red-600"
                    />
                  </div>
                  <div className="flex flex-col">
                    <label className="text-xs text-whiteMuted mb-1">Gap to Leader</label>
                    <input
                      type="text"
                      name="gapToLeader"
                      value={raceContext.gapToLeader}
                      onChange={handleContextChange}
                      placeholder="+12.4s"
                      className="font-mono bg-bgPrimary border border-red-600/30 rounded px-2 py-1 text-white text-sm focus:outline-none focus:border-red-600"
                    />
                  </div>
                </div>

                {/* Tyre */}
                <div className="grid grid-cols-2 gap-3">
                  <div className="flex flex-col">
                    <label className="text-xs text-whiteMuted mb-1">Compound</label>
                    <select
                      name="tyreCompound"
                      value={raceContext.tyreCompound}
                      onChange={handleContextChange}
                      className="font-mono bg-bgPrimary border border-red-600/30 rounded px-2 py-1 text-white text-sm focus:outline-none focus:border-red-600"
                    >
                      <option>SOFT</option>
                      <option>MEDIUM</option>
                      <option>HARD</option>
                      <option>INTER</option>
                      <option>WET</option>
                    </select>
                  </div>
                  <div className="flex flex-col">
                    <label className="text-xs text-whiteMuted mb-1">Age (laps)</label>
                    <input
                      type="number"
                      name="tyreAge"
                      value={raceContext.tyreAge}
                      onChange={handleContextChange}
                      className="font-mono bg-bgPrimary border border-red-600/30 rounded px-2 py-1 text-white text-sm focus:outline-none focus:border-red-600"
                    />
                  </div>
                </div>

                {/* Fuel & Weather */}
                <div className="grid grid-cols-2 gap-3">
                  <div className="flex flex-col">
                    <label className="text-xs text-whiteMuted mb-1">Fuel (kg)</label>
                    <input
                      type="number"
                      name="fuelLoad"
                      value={raceContext.fuelLoad}
                      onChange={handleContextChange}
                      step="0.1"
                      className="font-mono bg-bgPrimary border border-red-600/30 rounded px-2 py-1 text-white text-sm focus:outline-none focus:border-red-600"
                    />
                  </div>
                  <div className="flex flex-col">
                    <label className="text-xs text-whiteMuted mb-1">Weather</label>
                    <select
                      name="weather"
                      value={raceContext.weather}
                      onChange={handleContextChange}
                      className="font-mono bg-bgPrimary border border-red-600/30 rounded px-2 py-1 text-white text-sm focus:outline-none focus:border-red-600"
                    >
                      <option>Dry</option>
                      <option>Damp</option>
                      <option>Wet</option>
                    </select>
                  </div>
                </div>

                {/* Last Lap Time */}
                <div className="flex flex-col">
                  <label className="text-xs text-whiteMuted mb-1">Last Lap Time</label>
                  <input
                    type="text"
                    name="lastLapTime"
                    value={raceContext.lastLapTime}
                    onChange={handleContextChange}
                    placeholder="1:22.847"
                    className="bg-bgPrimary border border-red-600/30 rounded px-2 py-1 text-white text-sm focus:outline-none focus:border-red-600"
                  />
                </div>
              </div>
            </div>
          </motion.div>

          {/* Right Column - Engineer Radio Chat */}
          <motion.div
            initial={{ opacity: 0, x: 20 }}
            animate={{ opacity: 1, x: 0 }}
            transition={{ duration: 0.5, delay: 0.1 }}
            className="lg:col-span-2"
          >
            <div className="bg-bgSecondary border border-red-900/30 rounded-lg p-6 flex flex-col h-[600px] lg:h-auto lg:min-h-[600px]">
              <h2 className="font-display font-semibold text-xl uppercase tracking-wider mb-4 text-whitePrimary">Engineer Radio</h2>

              {/* Conversation Area */}
              <div className="flex-1 overflow-y-auto mb-4 space-y-3 pr-2 pb-2">
                {conversation.length === 0 ? (
                  <div className="text-center text-whiteMuted py-12">
                    <p className="text-sm">No transmission yet. Send your first message.</p>
                  </div>
                ) : (
                  conversation.map((msg, idx) => (
                    <motion.div
                      key={idx}
                      initial={{ opacity: 0, x: msg.role === "driver" ? 20 : -20 }}
                      animate={{ opacity: 1, x: 0 }}
                      transition={{ duration: 0.3 }}
                      className={`flex ${msg.role === "driver" ? "justify-end" : "justify-start"}`}
                    >
                      <div
                        className={`max-w-xs px-4 py-2 rounded-lg text-sm ${
                          msg.role === "driver"
                            ? "bg-red-900/20 text-whitePrimary border border-red-600/30"
                            : msg.isError
                            ? "bg-red-900/20 text-red-200 border border-red-600"
                            : "bg-gray-900/50 text-whitePrimary border border-gray-600/30"
                        } ${msg.role === "engineer" && !msg.isError ? "font-mono" : ""}`}
                      >
                        {msg.role === "engineer" && !msg.isError && (
                          <p className="text-xs text-whiteMuted mb-1">🎙️ Engineer</p>
                        )}
                        {msg.message}
                      </div>
                    </motion.div>
                  ))
                )}

                {/* Typing Indicator */}
                {loading && (
                  <motion.div
                    initial={{ opacity: 0 }}
                    animate={{ opacity: 1 }}
                    className="flex gap-1"
                  >
                    <div className="w-2 h-2 bg-whiteMuted rounded-full animate-bounce" />
                    <div className="w-2 h-2 bg-whiteMuted rounded-full animate-bounce" style={{ animationDelay: "0.1s" }} />
                    <div className="w-2 h-2 bg-whiteMuted rounded-full animate-bounce" style={{ animationDelay: "0.2s" }} />
                  </motion.div>
                )}

                <div ref={conversationEndRef} />
              </div>

              {/* Error Banner */}
              {error && !loading && (
                <motion.div
                  initial={{ opacity: 0, y: -10 }}
                  animate={{ opacity: 1, y: 0 }}
                  className="bg-red-900/20 border border-red-600 text-red-200 text-xs px-3 py-2 rounded mb-3"
                >
                  {error}
                </motion.div>
              )}

              {/* Input Area */}
              <form onSubmit={handleTransmit} className="flex gap-2">
                <input
                  type="text"
                  value={driverMessage}
                  onChange={(e) => setDriverMessage(e.target.value)}
                  placeholder="Driver message..."
                  disabled={loading}
                  className="flex-1 bg-bgPrimary border border-red-600/30 rounded px-3 py-2 text-white text-sm focus:outline-none focus:border-red-600 disabled:opacity-50"
                />
                <button
                  type="submit"
                  disabled={loading || !driverMessage.trim()}
                  className="bg-red-600 hover:bg-red-700 disabled:bg-red-900 text-white px-4 py-2 rounded font-semibold flex items-center gap-2 transition-colors"
                >
                  <Send className="w-4 h-4" />
                  Transmit
                </button>
              </form>
            </div>
          </motion.div>
        </div>
      </div>
    </div>
  );
};

export default RaceEngineerPage;
