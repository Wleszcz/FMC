// src/hooks/useAmenities.js
import { useState } from "react";

export function useAmenities() {
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const fetchAmenities = (lat, lon) => {
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
  };

  return { data, loading, error, fetchAmenities };
}
