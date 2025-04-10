import { useState, useEffect } from "react";

export function useHeatmapPoints() {
  const [heatmapData, setHeatmapData] = useState([]);
  const [loadingHeatmap, setLoadingHeatmap] = useState(false);
  const [errorHeatmap, setErrorHeatmap] = useState(null);

  useEffect(() => {
    setLoadingHeatmap(true);
    setErrorHeatmap(null);

    fetch("http://localhost:8080/api/heatmap")
      .then((res) => {
        if (!res.ok) {
          throw new Error(`Błąd HTTP: ${res.status}`);
        }
        return res.json();
      })
      .then((result) => {
        // oczekujemy tablicy: [{ lat: 54.3, lon: 18.6, value: 80 }, ...]
        setHeatmapData(result);
      })
      .catch((err) => {
        console.error("Błąd pobierania heatmapy:", err);
        setErrorHeatmap(err.message);
      })
      .finally(() => setLoadingHeatmap(false));
  }, []);

  return { heatmapData, loadingHeatmap, errorHeatmap };
}
