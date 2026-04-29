import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, CartesianGrid } from 'recharts';

const PredictionDistributionChart = ({ data }) => {
  if (!data || data.length === 0) {
    return null;
  }

  // Format probability to percentage
  const chartData = data.map(item => ({
    position: `P${item.position}`,
    probability: (item.probability * 100).toFixed(1)
  }));

  // Find max probability for highlighting
  const maxProb = Math.max(...data.map(d => d.probability));
  const mostLikelyPosition = data.find(d => d.probability === maxProb);

  const CustomTooltip = ({ active, payload }) => {
    if (active && payload && payload.length) {
      return (
        <div className="bg-black/90 border border-white/20 rounded-lg p-3 text-white">
          <p className="font-semibold">{payload[0].payload.position}</p>
          <p className="text-sm text-accentGold">{payload[0].value}% probability</p>
        </div>
      );
    }
    return null;
  };

  return (
    <div className="bg-black/40 p-4 rounded-xl border border-white/10">
      <div className="flex items-center justify-between mb-4">
        <h3 className="text-white font-semibold">Probability Distribution</h3>
        {mostLikelyPosition && (
          <span className="text-xs text-accentGold bg-accentGold/10 px-2 py-1 rounded-full">
            Most likely: P{mostLikelyPosition.position}
          </span>
        )}
      </div>
      <p className="text-xs text-whiteMuted mb-4">Probability of finishing at each position</p>
      <ResponsiveContainer width="100%" height={250}>
        <BarChart data={chartData} margin={{ top: 5, right: 30, left: 20, bottom: 5 }}>
          <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.1)" />
          <XAxis 
            dataKey="position" 
            stroke="#888" 
            fontSize={12}
            tick={{ fill: '#888' }}
          />
          <YAxis 
            stroke="#888" 
            fontSize={12}
            tick={{ fill: '#888' }}
            label={{ value: '%', angle: -90, position: 'insideLeft', fill: '#888' }}
          />
          <Tooltip content={<CustomTooltip />} />
          <Bar 
            dataKey="probability" 
            fill="#ef4444" 
            radius={[4, 4, 0, 0]}
            opacity={0.8}
          />
        </BarChart>
      </ResponsiveContainer>
    </div>
  );
};

export default PredictionDistributionChart;
