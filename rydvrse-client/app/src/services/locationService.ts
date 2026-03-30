/**
 * Location Service
 * Handles geolocation, address lookup, and location tracking
 */

import type { GeoLocation } from '@/types';

export const locationService = {
  /**
   * Get current geolocation from browser
   */
  getCurrentPosition: (): Promise<GeolocationPosition> => {
    return new Promise((resolve, reject) => {
      if (!navigator.geolocation) {
        reject(new Error('Geolocation is not supported by your browser'));
        return;
      }

      navigator.geolocation.getCurrentPosition(
        (position) => resolve(position),
        (error) => reject(error),
        {
          enableHighAccuracy: true,
          timeout: 10000,
          maximumAge: 60000,
        }
      );
    });
  },

  /**
   * Watch position changes
   */
  watchPosition: (
    callback: (position: GeolocationPosition) => void,
    errorCallback?: (error: GeolocationPositionError) => void
  ): number => {
    if (!navigator.geolocation) {
      throw new Error('Geolocation is not supported by your browser');
    }

    return navigator.geolocation.watchPosition(
      callback,
      errorCallback,
      {
        enableHighAccuracy: true,
        timeout: 10000,
        maximumAge: 60000,
      }
    );
  },

  /**
   * Clear position watch
   */
  clearWatch: (watchId: number): void => {
    navigator.geolocation.clearWatch(watchId);
  },

  /**
   * Reverse geocode - Get address from coordinates
   */
  reverseGeocode: async (lat: number, lng: number): Promise<string> => {
    // This would typically call a geocoding API
    // For now, return a placeholder
    return `${lat.toFixed(6)}, ${lng.toFixed(6)}`;
  },

  /**
   * Geocode - Get coordinates from address
   */
  geocode: async (_address: string): Promise<GeoLocation | null> => {
    // This would typically call a geocoding API
    // For now, return null
    return null;
  },

  /**
   * Calculate distance between two points (Haversine formula)
   */
  calculateDistance: (
    lat1: number,
    lng1: number,
    lat2: number,
    lng2: number
  ): number => {
    const R = 6371; // Earth's radius in kilometers
    const dLat = (lat2 - lat1) * (Math.PI / 180);
    const dLng = (lng2 - lng1) * (Math.PI / 180);
    const a =
      Math.sin(dLat / 2) * Math.sin(dLat / 2) +
      Math.cos(lat1 * (Math.PI / 180)) *
        Math.cos(lat2 * (Math.PI / 180)) *
        Math.sin(dLng / 2) *
        Math.sin(dLng / 2);
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    return R * c;
  },

  /**
   * Format coordinates for display
   */
  formatCoordinates: (lat: number, lng: number): string => {
    return `${lat.toFixed(6)}, ${lng.toFixed(6)}`;
  },

  /**
   * Check if location is within bounds
   */
  isWithinBounds: (
    lat: number,
    lng: number,
    bounds: { north: number; south: number; east: number; west: number }
  ): boolean => {
    return (
      lat >= bounds.south &&
      lat <= bounds.north &&
      lng >= bounds.west &&
      lng <= bounds.east
    );
  },
};

export default locationService;
