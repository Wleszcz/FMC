import React, { useState } from "react";
import MapComponent from "./MapComponent";
import LocationInfo from "./LocationInfo";
import { useAmenities } from "./hooks/useAmenities";
import { useHeatmapPoints } from "./hooks/useHeatmapPoints";

function App() {
  const [markerPosition, setMarkerPosition] = useState([54.352, 18.646]);
  const { data: amenities, loading, error, fetchAmenities } = useAmenities();
  const { heatmapData, loadingHeatmap } = useHeatmapPoints();

  const handleFetchClick = () => {
    fetchAmenities(markerPosition[0], markerPosition[1]);
  };

  return (
    <div className="App">
      <div className="App-body">
        <div className="main-container">
          <div>
            <MapComponent
              markerPosition={markerPosition}
              setMarkerPosition={setMarkerPosition}
              amenities={amenities}
              loading={loading}
              loadingHeatmap={loadingHeatmap}
              heatmapData={heatmapData}
            />
          </div>
          
          <div className="location-info-box">
          <button
              onClick={handleFetchClick}
              style={{
                padding: "10px 20px",
                marginBottom: "20px",
                fontSize: "15px",
                cursor: "pointer",
              }}
            >
              🔍 Pobierz udogodnienia
            </button>
            <LocationInfo amenities={amenities} loading={loading} error={error} />
          </div>
        </div>
      </div>
    </div>
  );
}

export default App;
