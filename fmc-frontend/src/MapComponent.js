import React from 'react';
import { MapContainer, TileLayer, Marker, Popup } from 'react-leaflet';

const MapComponent = () => {
  return (
    <MapContainer
      center={[54.352, 18.646]} // Ustawia środek mapy na Gdańsk
      zoom={13}
      style={{ height: '500px', width: '800px' }} // Mniejsze wymiary mapy
    >
      <TileLayer
        attribution='&copy; <a href="http://osm.org/copyright">OpenStreetMap</a> contributors'
        url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
      />
      <Marker position={[54.352, 18.646]}>
        <Popup>
          Witaj w Gdańsku!
        </Popup>
      </Marker>
    </MapContainer>
  );
};

export default MapComponent;