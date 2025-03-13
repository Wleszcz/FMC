import './App.css';
import React, { useState } from "react";
import MapComponent from "./MapComponent";
import LocationInfo from "./LocationInfo";
import { useAmenities } from "./useAmenities";

function App() {
  const [markerPosition, setMarkerPosition] = useState([54.352, 18.646]);
  const { data: amenities, loading, error } = useAmenities(markerPosition[0], markerPosition[1]);

  return (
    <div className="App">
      <body className="App-body">
          <div className='main-container'>
            <MapComponent 
              markerPosition={markerPosition} 
              setMarkerPosition={setMarkerPosition} 
              amenities={amenities}
              loading={loading}
            />
            <div className='location-info-box'>
              <LocationInfo amenities={amenities} loading={loading} error={error}/>
            </div>
          </div>
      </body>
    </div>
  );
}

export default App;
