import { useEffect, useState } from "react";
import api from "../utils/axios";

const Races = () => {
  const [races, setRaces] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchRaces = async () => {
      try {
        const res = await api.get("/races");
        setRaces(res.data);
      } catch (err) {
        console.error(err);
      } finally {
        setLoading(false);
      }
    };

    fetchRaces();
  }, []);

  if (loading) {
    return <div className="card">Loading races...</div>;
  }

  return (
    <div className="space-y-6">

      {/* HEADER */}
      <div>
        <h1 className="text-2xl">RACES</h1>
        <div className="racing-divider"></div>
      </div>

      {/* TIMELINE */}
      <div className="space-y-6">

        {races.map((race, index) => (
          <div key={race.raceId} className="flex gap-6">

            {/* LEFT — TIMELINE LINE */}
            <div className="flex flex-col items-center">
              <div className="w-3 h-3 bg-primary rounded-full"></div>

              {index !== races.length - 1 && (
                <div className="w-[2px] h-full bg-border mt-1"></div>
              )}
            </div>

            {/* RIGHT — CARD */}
            <div className="card w-full">

              <div className="flex justify-between items-center">
                <h2 className="text-lg">{race.name}</h2>
                <span className="text-textSecondary text-sm">
                  {race.date}
                </span>
              </div>

              <p className="text-textSecondary mt-1">
                {race.location}
              </p>

              <div className="mt-3 flex justify-between text-sm">
                <span className="text-textSecondary">Winner</span>
                <span className="font-medium">
                  {race.winner || "TBD"}
                </span>
              </div>

            </div>
          </div>
        ))}

      </div>

    </div>
  );
};

export default Races;