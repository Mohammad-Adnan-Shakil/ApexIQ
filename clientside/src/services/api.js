export const getPredictions = async () => {
  try {
    const res = await fetch("http://localhost:8080/api/predictions");
    return res.json();
  } catch {
    return [
      { race: "Next Race", driver: "Max Verstappen", confidence: 92 },
    ];
  }
};