import React from "react";
import './App.css';

const LocationInfo = ({ amenities, loading, error }) => {
  if (loading) return <p>Ładowanie...</p>;
  if (error) return <p style={{ color: "red" }}>Błąd: {error}</p>;
  if (amenities.length === 0) return <p>Brak wyników.</p>;

  const foundAmenities = amenities.filter(item => item.place && item.travel);
  const allAmenitiesNumber = amenities.length;

  return (
    <>
      <h3>Udogodnienia w pobliżu ({foundAmenities.length}/{allAmenitiesNumber}):</h3><ul>
      {foundAmenities.map((item, i) => (
        <li key={i}>
          <strong>{item.place.tags?.name || item.displayType}</strong> ({item.displayType})<br />
          📌 {(item.travel.distance / 1000).toFixed(2)} km | ⏳ {Math.round(item.travel.duration / 60)} min.
        </li>
      ))}
    </ul><h3>Brakujące Udogodnienia:</h3><ul>
        {amenities.filter(item => !item.place || !item.travel).map((item, i) => (
          <li key={`missing-${i}`}>
            <strong>{item.displayType}</strong>
          </li>
        ))}
      </ul>
    </>
  );
};

export default LocationInfo;
