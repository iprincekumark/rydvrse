/**
 * useLocation Hook
 * Handles geolocation and location tracking
 */

import { useState, useEffect, useCallback, useRef } from 'react';
import { locationService } from '@/services';
import type { GeoLocation } from '@/types';

interface UseLocationOptions {
  watch?: boolean;
}

export const useLocation = (options: UseLocationOptions = {}) => {
  const { watch = false } = options;

  const [location, setLocation] = useState<GeoLocation | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const watchIdRef = useRef<number | null>(null);

  /**
   * Get current position
   */
  const getCurrentPosition = useCallback(async (): Promise<GeoLocation | null> => {
    setIsLoading(true);
    setError(null);

    try {
      const position = await locationService.getCurrentPosition();
      const geoLocation: GeoLocation = {
        latitude: position.coords.latitude,
        longitude: position.coords.longitude,
      };
      setLocation(geoLocation);
      return geoLocation;
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Failed to get location';
      setError(message);
      return null;
    } finally {
      setIsLoading(false);
    }
  }, []);

  /**
   * Start watching position
   */
  const startWatching = useCallback(() => {
    if (!navigator.geolocation) {
      setError('Geolocation is not supported by your browser');
      return;
    }

    if (watchIdRef.current !== null) {
      return; // Already watching
    }

    const watchId = locationService.watchPosition(
      (position) => {
        setLocation({
          latitude: position.coords.latitude,
          longitude: position.coords.longitude,
        });
        setError(null);
      },
      (err) => {
        setError(err.message);
      }
    );

    watchIdRef.current = watchId;
  }, []);

  /**
   * Stop watching position
   */
  const stopWatching = useCallback((): void => {
    if (watchIdRef.current !== null) {
      locationService.clearWatch(watchIdRef.current);
      watchIdRef.current = null;
    }
  }, []);

  /**
   * Calculate distance to another point
   */
  const calculateDistance = useCallback(
    (lat: number, lng: number): number => {
      if (!location) return 0;
      return locationService.calculateDistance(
        location.latitude,
        location.longitude,
        lat,
        lng
      );
    },
    [location]
  );

  /**
   * Format current location for display
   */
  const formatLocation = useCallback((): string => {
    if (!location) return 'Location unavailable';
    return locationService.formatCoordinates(location.latitude, location.longitude);
  }, [location]);

  // Auto-start watching if watch option is true
  useEffect(() => {
    if (watch) {
      startWatching();
    }

    return () => {
      stopWatching();
    };
  }, [watch, startWatching, stopWatching]);

  return {
    location,
    error,
    isLoading,
    getCurrentPosition,
    startWatching,
    stopWatching,
    calculateDistance,
    formatLocation,
  };
};

export default useLocation;
