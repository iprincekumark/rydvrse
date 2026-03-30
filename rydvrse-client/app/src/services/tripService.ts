/**
 * Trip Service
 * Handles trip creation, management, and retrieval
 */

import { api } from './apiClient';
import type {
  Trip,
  CreateTripRequest,
  PagedResponse,
} from '@/types';

const TRIPS_BASE_URL = '/trips';

export const tripService = {
  /**
   * Create a new trip
   */
  createTrip: async (request: CreateTripRequest): Promise<Trip> => {
    const response = await api.post<Trip>(TRIPS_BASE_URL, request);
    return response.data;
  },

  /**
   * Get trip by ID
   */
  getTrip: async (tripId: string): Promise<Trip> => {
    const response = await api.get<Trip>(`${TRIPS_BASE_URL}/${tripId}`);
    return response.data;
  },

  /**
   * Assign driver to trip
   */
  assignDriver: async (tripId: string, driverId: string): Promise<Trip> => {
    const response = await api.post<Trip>(`${TRIPS_BASE_URL}/${tripId}/assign-driver`, {
      driverId,
    });
    return response.data;
  },

  /**
   * Mark driver as arrived
   */
  driverArrived: async (tripId: string): Promise<Trip> => {
    const response = await api.post<Trip>(`${TRIPS_BASE_URL}/${tripId}/driver-arrived`, {});
    return response.data;
  },

  /**
   * Start trip with OTP verification
   */
  startTrip: async (tripId: string, otp: string): Promise<Trip> => {
    const response = await api.post<Trip>(`${TRIPS_BASE_URL}/${tripId}/start`, { otp });
    return response.data;
  },

  /**
   * Complete trip
   */
  completeTrip: async (
    tripId: string,
    actualDistanceKm?: number,
    actualDurationMin?: number
  ): Promise<Trip> => {
    const response = await api.post<Trip>(`${TRIPS_BASE_URL}/${tripId}/complete`, {
      actualDistanceKm,
      actualDurationMin,
    });
    return response.data;
  },

  /**
   * Cancel trip
   */
  cancelTrip: async (tripId: string, reason: string, cancelledBy: string): Promise<Trip> => {
    const response = await api.post<Trip>(`${TRIPS_BASE_URL}/${tripId}/cancel`, {
      reason,
      cancelledBy,
    });
    return response.data;
  },

  /**
   * Rate trip
   */
  rateTrip: async (tripId: string, rating: number, feedback?: string): Promise<void> => {
    await api.post<void>(`${TRIPS_BASE_URL}/${tripId}/rate`, {
      rating,
      feedback,
    });
  },

  /**
   * Get customer's trip history with pagination
   */
  getCustomerTrips: async (
    customerId: string,
    page: number = 0,
    size: number = 20
  ): Promise<PagedResponse<Trip>> => {
    const response = await api.get<PagedResponse<Trip>>(
      `${TRIPS_BASE_URL}/customer/${customerId}`,
      { page, size }
    );
    return response.data;
  },

  /**
   * Get driver's trip history with pagination
   */
  getDriverTrips: async (
    driverId: string,
    page: number = 0,
    size: number = 20
  ): Promise<PagedResponse<Trip>> => {
    const response = await api.get<PagedResponse<Trip>>(
      `${TRIPS_BASE_URL}/driver/${driverId}`,
      { page, size }
    );
    return response.data;
  },
};

export default tripService;
