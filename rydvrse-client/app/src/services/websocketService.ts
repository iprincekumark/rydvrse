/**
 * WebSocket Service
 * Handles real-time communication using STOMP over SockJS
 * For live driver tracking and trip updates
 */

import { Client, type IMessage, type StompSubscription } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import type { DriverLocation, TripLocationUpdate } from '@/types';

const WS_BASE_URL = import.meta.env.VITE_WS_BASE_URL || 'http://localhost:8080';

class WebSocketService {
  private client: Client | null = null;
  private subscriptions: Map<string, StompSubscription> = new Map();

  /**
   * Initialize WebSocket connection
   */
  connect(token?: string): Promise<void> {
    return new Promise((resolve, reject) => {
      if (this.client?.connected) {
        resolve();
        return;
      }

      this.client = new Client({
        webSocketFactory: () => new SockJS(`${WS_BASE_URL}/ws`),
        connectHeaders: token ? { Authorization: `Bearer ${token}` } : {},
        debug: import.meta.env.DEV ? console.log : () => {},
        reconnectDelay: 3000,
        heartbeatIncoming: 4000,
        heartbeatOutgoing: 4000,
      });

      this.client.onConnect = () => {
        console.log('WebSocket connected');
        resolve();
      };

      this.client.onDisconnect = () => {
        console.log('WebSocket disconnected');
      };

      this.client.onStompError = (frame) => {
        console.error('STOMP error:', frame.headers.message);
        reject(new Error(frame.headers.message || 'WebSocket connection failed'));
      };

      this.client.activate();
    });
  }

  /**
   * Disconnect WebSocket
   */
  disconnect(): void {
    this.subscriptions.forEach((subscription) => subscription.unsubscribe());
    this.subscriptions.clear();
    this.client?.deactivate();
    this.client = null;
  }

  /**
   * Subscribe to trip location updates
   */
  subscribeToTripLocation(
    tripId: string,
    callback: (location: DriverLocation) => void
  ): void {
    if (!this.client?.connected) {
      console.error('WebSocket not connected');
      return;
    }

    const topic = `/topic/trip/${tripId}/location`;
    
    const subscription = this.client.subscribe(topic, (message: IMessage) => {
      try {
        const locationUpdate: TripLocationUpdate = JSON.parse(message.body);
        callback(locationUpdate.location);
      } catch (error) {
        console.error('Failed to parse location update:', error);
      }
    });

    this.subscriptions.set(`trip-${tripId}`, subscription);
  }

  /**
   * Subscribe to driver location updates
   */
  subscribeToDriverLocation(
    driverId: string,
    callback: (location: DriverLocation) => void
  ): void {
    if (!this.client?.connected) {
      console.error('WebSocket not connected');
      return;
    }

    const topic = `/topic/driver/${driverId}/location`;
    
    const subscription = this.client.subscribe(topic, (message: IMessage) => {
      try {
        const location: DriverLocation = JSON.parse(message.body);
        callback(location);
      } catch (error) {
        console.error('Failed to parse location update:', error);
      }
    });

    this.subscriptions.set(`driver-${driverId}`, subscription);
  }

  /**
   * Unsubscribe from a topic
   */
  unsubscribe(key: string): void {
    const subscription = this.subscriptions.get(key);
    if (subscription) {
      subscription.unsubscribe();
      this.subscriptions.delete(key);
    }
  }

  /**
   * Send driver location update
   */
  sendDriverLocation(_driverId: string, location: DriverLocation): void {
    if (!this.client?.connected) {
      console.error('WebSocket not connected');
      return;
    }

    this.client.publish({
      destination: '/app/driver/location',
      body: JSON.stringify(location),
    });
  }

  /**
   * Check if connected
   */
  isConnected(): boolean {
    return this.client?.connected ?? false;
  }
}

export const websocketService = new WebSocketService();
export default websocketService;
