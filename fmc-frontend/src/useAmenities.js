import { useState, useEffect } from "react";

export function useAmenities(lat, lon) {
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (!lat || !lon) return;
    setLoading(true);
    setError(null);

    fetch(`http://localhost:8080/api/get-amenity-list?lat=${lat}&lon=${lon}`)
      .then((res) => {
        if (!res.ok) {
          throw new Error(`Błąd HTTP: ${res.status}`);
        }
        return res.json();
      })
      .then((result) => {
        const arrayData = Array.isArray(result) ? result : result.data;
        setData(arrayData);
      })
      .catch((err) => {
        console.error("Błąd API:", err);
        setError(err.message);
      })
      .finally(() => setLoading(false));
  }, [lat, lon]);

  return { data, loading, error };
}
