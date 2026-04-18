import { useEffect, useState } from "react";
import api from "../utils/axios";

const Drivers = () => {
  const [drivers, setDrivers] = useState([]);
  const [search, setSearch] = useState("");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    const fetchDrivers = async () => {
      try {
        const res = await api.get("/drivers");
        setDrivers(res.data);
      } catch (err) {
        console.error(err);
        setError("Failed to load drivers");
      } finally {
        setLoading(false);
      }
    };

    fetchDrivers();
  }, []);

  const filteredDrivers = drivers.filter((d) =>
    d.name.toLowerCase().includes(search.toLowerCase())
  );

  if (loading) {
    return <div className="card animate-pulse h-40"></div>;
  }

  if (error) {
    return <div className="card text-danger">{error}</div>;
  }

  return (
    <div className="space-y-6">

      <div>
        <h1 className="text-2xl">DRIVERS</h1>
        <div className="racing-divider"></div>
      </div>

      <div className="card">
        <input
          type="text"
          placeholder="Search drivers..."
          className="input-field"
          value={search}
          onChange={(e) => setSearch(e.target.value)}
        />
      </div>

      <div className="card overflow-x-auto">
        <table className="w-full text-left">

          <thead className="border-b border-border text-textSecondary">
            <tr>
              <th className="py-3">Driver</th>
              <th>Team</th>
              <th>Points</th>
              <th>Wins</th>
              <th>Podiums</th>
            </tr>
          </thead>

          <tbody>
            {filteredDrivers.map((d) => (
              <tr key={d.driverId} className="border-b border-border hover:bg-[#111]">
                <td className="py-3">{d.name}</td>
                <td>{d.team}</td>
                <td>{d.points}</td>
                <td>{d.wins}</td>
                <td>{d.podiums}</td>
              </tr>
            ))}
          </tbody>

        </table>
      </div>

    </div>
  );
};

export default Drivers;