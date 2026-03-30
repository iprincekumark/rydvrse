/**
 * useWebSocket Hook
 * Handles WebSocket connection for real-time location tracking
 */

import { useState, useEffect, useCallback } from 'react';
import { websocketService } from '@/services';
import type { DriverLocation } from '@/types';
import { useAuthStore } from '@/store';

export const useWebSocket = () => {
  const [isConnected, setIsConnected] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const { accessToken } = useAuthStore();

  /**
   * Connect to WebSocket
   */
  const connect = useCallback(async (): Promise<boolean> => {
    try {
      await websocketService.connect(accessToken || undefined);
      setIsConnected(true);
      setError(null);
      return true;
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Failed to connect';
      setError(message);
      setIsConnected(false);
      return false;
    }
  }, [accessToken]);

  /**
   * Disconnect from WebSocket
   */
  const disconnect = useCallback((): void => {
    websocketService.disconnect();
    setIsConnected(false);
  }, []);

  /**
   * Subscribe to trip location updates
   */
  const subscribeToTripLocation = useCallback(
    (tripId: string, callback: (location: DriverLocation) => void): void => {
      if (!isConnected) {
        console.warn('WebSocket not connected');
        return;
      }
      websocketService.subscribeToTripLocation(tripId, callback);
    },
    [isConnected]
  );

  /**
   * Subscribe to driver location updates
   */
  const subscribeToDriverLocation = useCallback(
    (driverId: string, callback: (location: DriverLocation) => void): void => {
      if (!isConnected) {
        console.warn('WebSocket not connected');
        return;
      }
      websocketService.subscribeToDriverLocation(driverId, callback);
    },
    [isConnected]
  );

  /**
   * Send driver location update
   */
  const sendDriverLocation = useCallback(
    (driverId: string, location: DriverLocation): void => {
      if (!isConnected) {
        console.warn('WebSocket not connected');
        return;
      }
      websocketService.sendDriverLocation(driverId, location);
    },
    [isConnected]
  );

  // Auto-connect on mount
  useEffect(() => {
    if (accessToken) {
      connect();
    }

    return () => {
      disconnect();
    };
  }, [accessToken, connect, disconnect]);

  return {
    isConnected,
    error,
    connect,
    disconnect,
    subscribeToTripLocation,
    subscribeToDriverLocation,
    sendDriverLocation,
  };
};

export default useWebSocket;
