import { useState, useEffect } from "react";
import { useParams } from "react-router-dom";
import { Card } from "../components/common/Card";
import { Loader } from "../components/common/Loader";
import { Trophy, Users, Flag } from "lucide-react";
import api from "../utils/axios";

/**
 * Driver Career Page
 * Shows driver's historical stats and race-by-race results
 */
const HistoryDriver = () => {
  const { driverCode } = useParams();
  
  useEffect(() => {
    document.title = "Driver Career | ApexIQ";
  }, []);

  const [driver, setDriver] = useState(null);
  const [stats, setStats] = useState(null);
  const [results, setResults] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchDriverData = async () => {
      try {
        setLoading(true);
        const response = await api.get(`/historical/driver/${driverCode}/career`);
        setDriver(response.data.driver);
        setStats(response.data.careerStats);
        setResults(response.data.results || []);
      } catch (err) {
        setError(err.message || "Driver not found");
      } finally {
        setLoading(false);
      }
    };

    if (driverCode) {
      fetchDriverData();
    }
  }, [driverCode]);

  if (loading) return <Loader />;

  if (error) {
    return (
      <div className="p-6">
        <div className="text-center text-red-500">
          <p className="text-xl font-bold mb-2">Error Loading Driver</p>
          <p>{error}</p>
        </div>
      </div>
    );
  }

  if (!driver) return <Loader />;

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="mb-8">
        <div className="flex items-start justify-between">
          <div>
            <h1 className="text-4xl font-bold text-whitePrimary mb-2">
              {driver.fullName}
            </h1>
            <p className="text-whiteMuted">
              {driver.nationality} • Driver Code: {driver.code}
            </p>
          </div>
          <div className="text-right">
            <p className="text-3xl font-bold text-accentRed">{driver.code}</p>
          </div>
        </div>
      </div>

      {/* Career Stats */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        <Card className="p-6">
          <div className="flex items-center gap-3 mb-2">
            <Flag className="h-5 w-5 text-accentRed" />
            <p className="text-whiteMuted text-sm">Total Races</p>
          </div>
          <p className="text-3xl font-bold text-whitePrimary">{stats.races}</p>
        </Card>

        <Card className="p-6">
          <div className="flex items-center gap-3 mb-2">
            <Trophy className="h-5 w-5 text-accentRed" />
            <p className="text-whiteMuted text-sm">Wins</p>
          </div>
          <p className="text-3xl font-bold text-whitePrimary">{stats.wins}</p>
        </Card>

        <Card className="p-6">
          <div className="flex items-center gap-3 mb-2">
            <Trophy className="h-5 w-5 text-accentRed" />
            <p className="text-whiteMuted text-sm">Podiums</p>
          </div>
          <p className="text-3xl font-bold text-whitePrimary">{stats.podiums}</p>
        </Card>

        <Card className="p-6">
          <div className="flex items-center gap-3 mb-2">
            <Trophy className="h-5 w-5 text-accentRed" />
            <p className="text-whiteMuted text-sm">Championships</p>
          </div>
          <p className="text-3xl font-bold text-whitePrimary">{stats.championships}</p>
        </Card>
      </div>

      {/* Results Table */}
      <Card className="p-6 overflow-x-auto">
        <h2 className="text-xl font-bold text-whitePrimary mb-4">Race Results</h2>
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b border-borderSoft">
              <th className="text-left py-2 text-whiteMuted">Season</th>
              <th className="text-left py-2 text-whiteMuted">Round</th>
              <th className="text-left py-2 text-whiteMuted">Race</th>
              <th className="text-center py-2 text-whiteMuted">Grid</th>
              <th className="text-center py-2 text-whiteMuted">Position</th>
              <th className="text-center py-2 text-whiteMuted">Points</th>
            </tr>
          </thead>
          <tbody>
            {results.length > 0 ? (
              results.slice(0, 50).map((result, idx) => (
                <tr key={idx} className="border-b border-borderSoft hover:bg-white/5">
                  <td className="py-3 text-whitePrimary">2026</td>
                  <td className="py-3 text-whiteMuted">-</td>
                  <td className="py-3 text-whiteMuted">-</td>
                  <td className="py-3 text-center text-whitePrimary">{result.gridPosition || "-"}</td>
                  <td className="py-3 text-center text-whitePrimary">{result.finishPosition || "-"}</td>
                  <td className="py-3 text-center text-accentRed font-bold">{result.points || 0}</td>
                </tr>
              ))
            ) : (
              <tr>
                <td colSpan="6" className="py-8 text-center text-whiteMuted">
                  No results available
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </Card>
    </div>
  );
};

export default HistoryDriver;
