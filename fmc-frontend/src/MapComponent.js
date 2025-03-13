import React, { useState } from "react";
import { MapContainer, TileLayer, Marker, Popup, useMapEvents } from "react-leaflet";
import "leaflet/dist/leaflet.css";
import L from "leaflet";
import markerIcon from "leaflet/dist/images/marker-icon.png";
import markerShadow from "leaflet/dist/images/marker-shadow.png";
import LocationInfo from "./LocationInfo";

const customIcon = new L.Icon({
  iconUrl: markerIcon,
  shadowUrl: markerShadow,
  iconSize: [25, 41],
  iconAnchor: [12, 41],
  popupAnchor: [1, -34],
  shadowSize: [41, 41],
});

const MapComponent = () => {
  const [markerPosition, setMarkerPosition] = useState([54.352, 18.646]);

  const MapClickHandler = () => {
    useMapEvents({
      click(e) {
        setMarkerPosition([e.latlng.lat, e.latlng.lng]);
      },
    });
    return null;
  };

  return (
    <div className="main-container">
      <div className="map-container">
        <MapContainer
          center={markerPosition}
          zoom={13}
          style={{ height: "500px", width: "800px", margintop: "50px" }}
        >
          <TileLayer
            attribution='&copy; <a href="http://osm.org/copyright">OpenStreetMap</a> contributors'
            url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
          />
  
          <Marker position={markerPosition} icon={customIcon}>
            <Popup>
              Współrzędne: {markerPosition[0].toFixed(5)}, {markerPosition[1].toFixed(5)}
              <br />
              Kliknij w inne miejsce, aby przesunąć marker!
            </Popup>
          </Marker>
    
          <MapClickHandler />
        </MapContainer>
      </div>
      <LocationInfo lat={markerPosition[0]} lon={markerPosition[1]} />
    </div>
  );
};

export default MapComponent;
