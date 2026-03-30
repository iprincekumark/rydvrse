/**
 * Map Container Component
 * Interactive map using Leaflet for location display and tracking
 */

import { useEffect, useRef } from 'react';
import { MapContainer as LeafletMap, TileLayer, Marker, Popup, useMap } from 'react-leaflet';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';
import type { GeoLocation, DriverLocation } from '@/types';

// Fix Leaflet default icons
import icon from 'leaflet/dist/images/marker-icon.png';
import iconShadow from 'leaflet/dist/images/marker-shadow.png';

const DefaultIcon = L.icon({
  iconUrl: icon,
  shadowUrl: iconShadow,
  iconSize: [25, 41],
  iconAnchor: [12, 41],
});

L.Marker.prototype.options.icon = DefaultIcon;

// Custom icons
const createCustomIcon = (color: string) => {
  return L.divIcon({
    className: 'custom-marker',
    html: `<div style="
      width: 24px;
      height: 24px;
      background-color: ${color};
      border: 3px solid white;
      border-radius: 50%;
      box-shadow: 0 2px 6px rgba(0,0,0,0.3);
    "></div>`,
    iconSize: [24, 24],
    iconAnchor: [12, 12],
  });
};

const pickupIcon = createCustomIcon('#22c55e');
const dropIcon = createCustomIcon('#ef4444');
const driverIcon = createCustomIcon('#3b82f6');

interface MapViewProps {
  center: [number, number];
  zoom?: number;
}

// Component to update map view
const MapView = ({ center, zoom = 15 }: MapViewProps) => {
  const map = useMap();
  
  useEffect(() => {
    map.setView(center, zoom);
  }, [center, zoom, map]);

  return null;
};

interface MapContainerProps {
  pickup?: GeoLocation;
  drop?: GeoLocation;
  driverLocation?: DriverLocation | null;
  userLocation?: GeoLocation | null;
  height?: string;
  onMapClick?: (lat: number, lng: number) => void;
  className?: string;
}

export const MapContainer = ({
  pickup,
  drop,
  driverLocation,
  userLocation,
  height = '400px',
  onMapClick,
  className = '',
}: MapContainerProps) => {
  const mapRef = useRef<L.Map | null>(null);
  
  // Default center (India)
  const defaultCenter: [number, number] = [20.5937, 78.9629];
  
  // Determine map center
  const getCenter = (): [number, number] => {
    if (userLocation) {
      return [userLocation.latitude, userLocation.longitude];
    }
    if (pickup) {
      return [pickup.latitude, pickup.longitude];
    }
    if (driverLocation) {
      return [driverLocation.latitude, driverLocation.longitude];
    }
    return defaultCenter;
  };

  // Handle map click
  useEffect(() => {
    if (mapRef.current && onMapClick) {
      const map = mapRef.current;
      const handleClick = (e: L.LeafletMouseEvent) => {
        onMapClick(e.latlng.lat, e.latlng.lng);
      };
      map.on('click', handleClick);
      return () => {
        map.off('click', handleClick);
      };
    }
  }, [onMapClick]);

  return (
    <div className={`rounded-lg overflow-hidden border ${className}`} style={{ height }}>
      <LeafletMap
        center={getCenter()}
        zoom={15}
        style={{ height: '100%', width: '100%' }}
        ref={mapRef}
      >
        <TileLayer
          attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        />
        
        <MapView center={getCenter()} />

        {/* Pickup Marker */}
        {pickup && (
          <Marker
            position={[pickup.latitude, pickup.longitude]}
            icon={pickupIcon}
          >
            <Popup>
              <div className="text-sm">
                <p className="font-semibold">Pickup Location</p>
                {pickup.address && <p>{pickup.address}</p>}
              </div>
            </Popup>
          </Marker>
        )}

        {/* Drop Marker */}
        {drop && (
          <Marker
            position={[drop.latitude, drop.longitude]}
            icon={dropIcon}
          >
            <Popup>
              <div className="text-sm">
                <p className="font-semibold">Drop Location</p>
                {drop.address && <p>{drop.address}</p>}
              </div>
            </Popup>
          </Marker>
        )}

        {/* Driver Marker */}
        {driverLocation && (
          <Marker
            position={[driverLocation.latitude, driverLocation.longitude]}
            icon={driverIcon}
          >
            <Popup>
              <div className="text-sm">
                <p className="font-semibold">Driver Location</p>
              </div>
            </Popup>
          </Marker>
        )}

        {/* User Location with accuracy circle */}
        {userLocation && (
          <>
            <Marker
              position={[userLocation.latitude, userLocation.longitude]}
              icon={createCustomIcon('#8b5cf6')}
            >
              <Popup>
                <div className="text-sm">
                  <p className="font-semibold">Your Location</p>
                </div>
              </Popup>
            </Marker>
          </>
        )}
      </LeafletMap>
    </div>
  );
};

export default MapContainer;
