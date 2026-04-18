import { useEffect, useState } from "react";
import api from "../utils/axios";

const Races = () => {
  const [races, setRaces] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    const fetchRaces = async () => {
      try {
        const res = await api.get("/races");
        setRaces(res.data);
      } catch (err) {
        console.error(err);
        setError("Failed to load races");
      } finally {
        setLoading(false);
      }
    };

    fetchRaces();
  }, []);

  if (loading) {
    return <div className="card animate-pulse h-40"></div>;
  }

  if (error) {
    return <div className="card text-danger">{error}</div>;
  }

  return (
    <div className="space-y-6">

      <div>
        <h1 className="text-2xl">RACES</h1>
        <div className="racing-divider"></div>
      </div>

      <div className="space-y-6">

        {races.map((race, index) => (
          <div key={race.raceId} className="flex gap-6">

            <div className="flex flex-col items-center">
              <div className="w-3 h-3 bg-primary rounded-full"></div>
              {index !== races.length - 1 && (
                <div className="w-[2px] h-full bg-border mt-1"></div>
              )}
            </div>

            <div className="card w-full">
              <div className="flex justify-between">
                <h2>{race.name}</h2>
                <span>{race.date}</span>
              </div>

              <p className="text-textSecondary">{race.location}</p>

              <div className="flex justify-between mt-2">
                <span>Winner</span>
                <span>{race.winner || "TBD"}</span>
              </div>
            </div>

          </div>
        ))}

      </div>

    </div>
  );
};

export default Races;