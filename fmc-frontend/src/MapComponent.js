import React from "react";
import { MapContainer, TileLayer, Marker, Popup, useMapEvents } from "react-leaflet";
import "leaflet/dist/leaflet.css";
import L from "leaflet";
import markerShadow from "leaflet/dist/images/marker-shadow.png";
import { ICON_BASE_URL } from "./config";

const MapComponent = ({ markerPosition, setMarkerPosition, amenities, loading }) => {
  const MapClickHandler = () => {
    useMapEvents({
      click(e) {
        setMarkerPosition([e.latlng.lat, e.latlng.lng]);
      },
    });
    return null;
  };

  return (
    <div className="map-container">
      <MapContainer
        center={markerPosition}
        zoom={13}
        style={{ height: "500px", width: "800px", marginTop: "30px" }}
      >
        <TileLayer
          attribution='&copy; <a href="http://osm.org/copyright">OpenStreetMap</a> contributors'
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        />

        {/* Main marker for selected position */}
        <Marker position={markerPosition} icon={getIcon("blue")}>
          <Popup>
            Współrzędne: {markerPosition[0].toFixed(5)}, {markerPosition[1].toFixed(5)}<br />
            Kliknij w inne miejsce, aby przesunąć marker!
          </Popup>
        </Marker>

        {/* Markers for points returned by API */}
        
        {!loading && amenities
          .filter(point => point.place && point.place.lat && point.place.lon)
          .map((point, index) => {
            const color = getMarkerColor(point.type);
            return (
              <Marker
                key={index}
                position={[point.place.lat, point.place.lon]}
                icon={getIcon(color)}
              >
                <Popup>
                  {point.place.tags?.name || point.displayType}
                </Popup>
              </Marker>
            );
          })
        }

        <MapClickHandler />
      </MapContainer>
    </div>
  );
};
  const getIcon = (color) => new L.Icon({
    iconUrl: `${ICON_BASE_URL}/marker-icon-${color}.png`,
    shadowUrl: markerShadow,
    iconSize: [25, 41],
    iconAnchor: [12, 41],
    popupAnchor: [1, -34],
    shadowSize: [41, 41],
  });

  const getMarkerColor = (type) => {
    switch(type) {
      case "atm":
        return "orange";
      case "hospital":
        return "red";
      case "school":
        return "green";
      case "place_of_worship":
        return "grey";
      case "library":
        return "violet";
      case "cinema":
        return "yellow";
      case "restaurant":
        return "black";
      default:
        return "blue";
    }
  };

export default MapComponent;