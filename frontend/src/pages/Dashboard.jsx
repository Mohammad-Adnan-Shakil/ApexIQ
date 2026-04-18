import { useEffect, useState } from "react";
import api from "../utils/axios";
import {
  LineChart, Line, XAxis, YAxis, Tooltip, ResponsiveContainer
} from "recharts";

const Dashboard = () => {
  const [drivers, setDrivers] = useState([]);
  const [races, setRaces] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    const fetchData = async () => {
      try {
        const [driversRes, racesRes] = await Promise.all([
          api.get("/drivers"),
          api.get("/races"),
        ]);

        setDrivers(driversRes.data);
        setRaces(racesRes.data);
      } catch (err) {
        console.error(err);
        setError("Failed to load dashboard");
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, []);

  // ✅ FIX: inside component
  const chartData = drivers.slice(0, 5).map((d) => ({
    name: d.name,
    points: d.points,
  }));

  if (loading) {
    return (
      <div className="space-y-6">
        <div className="grid grid-cols-3 gap-6">
          {[1, 2, 3].map((i) => (
            <div key={i} className="card animate-pulse h-24"></div>
          ))}
        </div>
        <div className="card animate-pulse h-40"></div>
        <div className="card animate-pulse h-40"></div>
      </div>
    );
  }

  if (error) {
    return <div className="card text-danger">{error}</div>;
  }

  return (
    <div className="space-y-6">

      {/* Stats */}
      <div className="grid grid-cols-3 gap-6">
        <div className="card">
          <p>Total Drivers</p>
          <p className="stat-number">{drivers.length}</p>
        </div>

        <div className="card">
          <p>Total Races</p>
          <p className="stat-number">{races.length}</p>
        </div>

        <div className="card">
          <p>Top Driver</p>
          <p className="stat-number">{drivers[0]?.name}</p>
        </div>
      </div>

      {/* 🔥 Chart */}
      <div className="card">
        <h2 className="mb-4">Top Driver Performance</h2>

        <ResponsiveContainer width="100%" height={300}>
          <LineChart data={chartData}>
            <XAxis dataKey="name" />
            <YAxis />
            <Tooltip />
            <Line type="monotone" dataKey="points" stroke="#E8002D" />
          </LineChart>
        </ResponsiveContainer>
      </div>

      {/* Drivers */}
      <div className="card">
        <h2>Top Drivers</h2>
        {drivers.slice(0, 5).map((d) => (
          <div key={d.driverId} className="flex justify-between">
            <span>{d.name}</span>
            <span>{d.team}</span>
          </div>
        ))}
      </div>

      {/* Races */}
      <div className="card">
        <h2>Upcoming Races</h2>
        {races.slice(0, 5).map((r) => (
          <div key={r.raceId} className="flex justify-between">
            <span>{r.name}</span>
            <span>{r.location}</span>
          </div>
        ))}
      </div>

    </div>
  );
};

export default Dashboard;