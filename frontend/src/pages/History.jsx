import { useEffect, useMemo, useState } from "react";
import { AlertTriangle, CalendarDays, Flag, Trophy } from "lucide-react";
import { Card } from "../components/common/Card";
import { Loader } from "../components/common/Loader";
import api from "../services/api";

const toSeasonArray = (payload) => {
  if (Array.isArray(payload)) return payload;
  if (Array.isArray(payload?.data)) return payload.data;
  if (Array.isArray(payload?.seasons)) return payload.seasons;
  return [];
};

const History = () => {
  useEffect(() => {
    document.title = "History | ApexIQ";
  }, []);

  const [seasons, setSeasons] = useState([]);
  const [selectedYear, setSelectedYear] = useState(null);
  const [seasonDetail, setSeasonDetail] = useState(null);
  const [races, setRaces] = useState([]);

  const [seasonsLoading, setSeasonsLoading] = useState(true);
  const [seasonLoading, setSeasonLoading] = useState(false);
  const [error, setError] = useState(null);

  const sortedSeasons = useMemo(
    () => seasons.slice().sort((a, b) => Number(b.year) - Number(a.year)),
    [seasons]
  );

  const fetchSeasons = async () => {
    try {
      setError(null);
      setSeasonsLoading(true);

      const response = await api.get("/historical/seasons");
      const list = toSeasonArray(response.data);
      setSeasons(list);

      if (list.length > 0) {
        const firstYear = Number(list[0].year);
        setSelectedYear(firstYear);
      }
    } catch (err) {
      setError(err.response?.data?.message || err.message || "Failed to load seasons");
    } finally {
      setSeasonsLoading(false);
    }
  };

  const fetchSeasonDetail = async (year) => {
    if (!year) return;

    try {
      setError(null);
      setSeasonLoading(true);

      const response = await api.get(`/historical/season/${year}`);
      const payload = response.data;
      setSeasonDetail(payload?.season || null);
      setRaces(Array.isArray(payload?.races) ? payload.races : []);
    } catch (err) {
      setError(err.response?.data?.message || err.message || "Failed to load season details");
      setSeasonDetail(null);
      setRaces([]);
    } finally {
      setSeasonLoading(false);
    }
  };

  useEffect(() => {
    fetchSeasons();
  }, []);

  useEffect(() => {
    if (selectedYear) {
      fetchSeasonDetail(selectedYear);
    }
  }, [selectedYear]);

  if (seasonsLoading) {
    return (
      <div className="py-10">
        <Loader size="lg" message="Loading historical seasons..." />
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold text-whitePrimary">F1 History Browser</h1>
        <p className="text-whiteMuted mt-1">Explore full season calendars and race details.</p>
      </div>

      {error ? (
        <Card className="border-accentRed/40 bg-accentRed/10">
          <div className="flex gap-3 items-start">
            <AlertTriangle className="h-5 w-5 text-accentRed mt-0.5" />
            <div>
              <p className="font-semibold text-whitePrimary">Unable to load history</p>
              <p className="text-sm text-whiteMuted mt-1">{error}</p>
            </div>
          </div>
        </Card>
      ) : null}

      <Card>
        <label className="text-sm text-whiteMuted uppercase tracking-[0.2em] block mb-2">Season</label>
        <select
          value={selectedYear || ""}
          onChange={(e) => setSelectedYear(Number(e.target.value))}
          className="surface-input max-w-xs"
        >
          {sortedSeasons.map((season) => (
            <option key={season.id || season.year} value={season.year}>
              {season.year}
            </option>
          ))}
        </select>
      </Card>

      {seasonLoading ? (
        <Card>
          <Loader message={`Loading ${selectedYear} season...`} />
        </Card>
      ) : null}

      {seasonDetail && !seasonLoading ? (
        <Card className="bg-gradient-to-r from-accentRed/10 to-transparent">
          <div className="flex items-center justify-between gap-4 flex-wrap">
            <div>
              <p className="text-sm text-whiteMuted uppercase tracking-[0.2em]">Season Overview</p>
              <h2 className="text-2xl font-bold text-whitePrimary mt-2">{seasonDetail.year} Championship</h2>
            </div>
            <div className="inline-flex items-center gap-2 rounded-full bg-bgElevated px-3 py-1.5 text-sm text-whiteMuted">
              <CalendarDays className="h-4 w-4 text-accentRed" />
              {seasonDetail.totalRounds || races.length} rounds
            </div>
          </div>
        </Card>
      ) : null}

      {!seasonLoading && races.length > 0 ? (
        <div>
          <h3 className="text-xl font-bold text-whitePrimary mb-4">Race Calendar</h3>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {races.map((race) => {
              // Get race winner from results
              const raceWinner = race.results && race.results.length > 0 
                ? race.results.find(r => r.position === 1)
                : null;
              
              return (
                <Card key={race.id || `${race.round}-${race.raceName}`} className="p-4 hover:border-accentRed/40 cursor-pointer transition-all">
                  <div className="flex items-start justify-between mb-2">
                    <div className="flex-1">
                      <p className="text-xs text-whiteMuted uppercase tracking-[0.2em]">Round {race.round}</p>
                      <h4 className="text-whitePrimary font-bold mt-1">{race.raceName}</h4>
                    </div>
                    <Flag className="h-4 w-4 text-accentRed mt-1 flex-shrink-0 ml-2" />
                  </div>
                  <p className="text-sm text-whiteMuted">{race.circuitName}</p>
                  <p className="text-xs text-whiteMuted mt-1">{race.location}</p>
                  {race.date ? (
                    <p className="text-xs text-whiteMuted mt-2">{new Date(race.date).toLocaleDateString()}</p>
                  ) : null}
                  
                  {/* Display race winner if available */}
                  {raceWinner ? (
                    <div className="mt-3 pt-3 border-t border-borderSoft flex items-center gap-2">
                      <Trophy className="h-3.5 w-3.5 text-accentGold" />
                      <span className="text-xs font-semibold text-accentGold">
                        Winner: {raceWinner.driverName || "Unknown"}
                      </span>
                    </div>
                  ) : null}
                </Card>
              );
            })}
          </div>
        </div>
      ) : null}

      {!seasonLoading && selectedYear && races.length === 0 && !error ? (
        <Card className="text-center py-10">
          <p className="text-whiteMuted">No races found for {selectedYear}.</p>
        </Card>
      ) : null}
    </div>
  );
};

export default History;
