import { useState, useEffect } from "react";
import { Card } from "../components/common/Card";
import { Loader } from "../components/common/Loader";
import { Trophy, Medal } from "lucide-react";
import api from "../utils/axios";

/**
 * Hall of Champions Page
 * Shows all F1 world champions by year
 */
const HistoryChampions = () => {
  useEffect(() => {
    document.title = "Hall of Champions | ApexIQ";
  }, []);

  const [champions, setChampions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [selectedDecade, setSelectedDecade] = useState("all");

  useEffect(() => {
    const fetchChampions = async () => {
      try {
        setLoading(true);
        const response = await api.get("/historical/champions");
        setChampions(response.data);
      } catch (err) {
        setError(err.message || "Failed to fetch champions");
      } finally {
        setLoading(false);
      }
    };

    fetchChampions();
  }, []);

  const decades = [
    { value: "all", label: "All Time" },
    { value: "2020", label: "2020s" },
    { value: "2010", label: "2010s" },
    { value: "2000", label: "2000s" },
    { value: "1990", label: "1990s" },
  ];

  const filteredChampions = champions.filter((champ) => {
    if (selectedDecade === "all") return true;
    const decade = Math.floor(champ.year / 10) * 10;
    return decade.toString() === selectedDecade;
  });

  if (loading) return <Loader />;

  if (error) {
    return (
      <div className="p-6">
        <div className="text-center text-red-500">
          <p className="text-xl font-bold mb-2">Error Loading Champions</p>
          <p>{error}</p>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="mb-8">
        <h1 className="text-4xl font-bold text-whitePrimary mb-2">Hall of Champions</h1>
        <p className="text-whiteMuted">All F1 World Champions from 1950 to present</p>
      </div>

      {/* Filter */}
      <div className="flex gap-2 overflow-x-auto pb-2">
        {decades.map((decade) => (
          <button
            key={decade.value}
            onClick={() => setSelectedDecade(decade.value)}
            className={`px-4 py-2 rounded-lg whitespace-nowrap transition-colors ${
              selectedDecade === decade.value
                ? "bg-accentRed text-white"
                : "bg-white/5 text-whiteMuted hover:bg-white/10"
            }`}
          >
            {decade.label}
          </button>
        ))}
      </div>

      {/* Champions Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {filteredChampions.map((champ, idx) => (
          <Card key={idx} className="p-6 flex items-center justify-between">
            <div className="flex items-center gap-4">
              <div className="relative">
                <div className="w-12 h-12 rounded-full bg-accentRed/20 flex items-center justify-center">
                  {idx < 3 && <Medal className="w-6 h-6 text-accentRed" />}
                  {idx >= 3 && <Trophy className="w-6 h-6 text-whiteMuted" />}
                </div>
              </div>
              <div>
                <p className="text-whitePrimary font-bold">{champ.driverName}</p>
                <p className="text-whiteMuted text-sm">{champ.constructorName}</p>
              </div>
            </div>
            <p className="text-2xl font-bold text-accentRed">{champ.year}</p>
          </Card>
        ))}
      </div>

      {filteredChampions.length === 0 && (
        <Card className="p-12 text-center">
          <p className="text-whiteMuted">No champions found for this period</p>
        </Card>
      )}
    </div>
  );
};

export default HistoryChampions;
