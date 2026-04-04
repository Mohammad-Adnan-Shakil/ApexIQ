export const getPredictions = async () => {
  // fake API for now
  return [
    { race: "Next Race", driver: "Max Verstappen", confidence: 92 },
    { race: "Race 2", driver: "Lewis Hamilton", confidence: 87 },
    { race: "Race 3", driver: "Charles Leclerc", confidence: 84 },
    { race: "Race 4", driver: "Lando Norris", confidence: 79 },
  ];
};