/**
 * useTrip Hook
 * Handles trip operations and state management
 */

import { useState, useCallback } from 'react';
import { tripService } from '@/services';
import { useTripStore, useUIStore, useAuthStore } from '@/store';
import type {
  CreateTripRequest,
  Trip,
  PagedResponse,
} from '@/types';
import { TripStatus } from '@/types';

export const useTrip = () => {
  const [isLoading, setIsLoading] = useState(false);
  
  const {
    activeTrip,
    setActiveTrip,
    updateTripStatus: updateStoreTripStatus,
    addToHistory,
    setTripHistory,
    clearActiveTrip,
  } = useTripStore();
  
  const { addToast } = useUIStore();
  const { user } = useAuthStore();

  /**
   * Create a new trip
   */
  const createTrip = useCallback(async (request: CreateTripRequest): Promise<Trip | null> => {
    setIsLoading(true);
    try {
      const trip = await tripService.createTrip(request);
      setActiveTrip(trip);
      addToast({
        type: 'success',
        message: 'Trip booked successfully',
      });
      return trip;
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Failed to book trip';
      addToast({ type: 'error', message });
      return null;
    } finally {
      setIsLoading(false);
    }
  }, [setActiveTrip, addToast]);

  /**
   * Get trip details
   */
  const getTrip = useCallback(async (tripId: string): Promise<Trip | null> => {
    setIsLoading(true);
    try {
      const trip = await tripService.getTrip(tripId);
      return trip;
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Failed to fetch trip';
      addToast({ type: 'error', message });
      return null;
    } finally {
      setIsLoading(false);
    }
  }, [addToast]);

  /**
   * Cancel active trip
   */
  const cancelTrip = useCallback(async (reason: string): Promise<boolean> => {
    if (!activeTrip) return false;
    
    setIsLoading(true);
    try {
      const cancelledTrip = await tripService.cancelTrip(
        activeTrip.id,
        reason,
        user?.id || 'unknown'
      );
      
      updateStoreTripStatus(TripStatus.CANCELLED);
      addToHistory(cancelledTrip);
      clearActiveTrip();
      
      addToast({
        type: 'success',
        message: 'Trip cancelled successfully',
      });
      return true;
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Failed to cancel trip';
      addToast({ type: 'error', message });
      return false;
    } finally {
      setIsLoading(false);
    }
  }, [activeTrip, user, updateStoreTripStatus, addToHistory, clearActiveTrip, addToast]);

  /**
   * Rate completed trip
   */
  const rateTrip = useCallback(async (tripId: string, rating: number, feedback?: string): Promise<boolean> => {
    setIsLoading(true);
    try {
      await tripService.rateTrip(tripId, rating, feedback);
      addToast({
        type: 'success',
        message: 'Thank you for your feedback!',
      });
      return true;
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Failed to submit rating';
      addToast({ type: 'error', message });
      return false;
    } finally {
      setIsLoading(false);
    }
  }, [addToast]);

  /**
   * Get customer trip history
   */
  const getCustomerTrips = useCallback(async (
    customerId: string,
    page: number = 0,
    size: number = 20
  ): Promise<PagedResponse<Trip> | null> => {
    setIsLoading(true);
    try {
      const trips = await tripService.getCustomerTrips(customerId, page, size);
      setTripHistory(trips.content);
      return trips;
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Failed to fetch trips';
      addToast({ type: 'error', message });
      return null;
    } finally {
      setIsLoading(false);
    }
  }, [setTripHistory, addToast]);

  /**
   * Update trip status (for driver actions)
   */
  const updateTripStatus = useCallback(async (
    tripId: string,
    action: 'arrived' | 'start' | 'complete',
    data?: { otp?: string; actualDistanceKm?: number; actualDurationMin?: number }
  ): Promise<Trip | null> => {
    setIsLoading(true);
    try {
      let trip: Trip;
      
      switch (action) {
        case 'arrived':
          trip = await tripService.driverArrived(tripId);
          break;
        case 'start':
          if (!data?.otp) throw new Error('OTP required');
          trip = await tripService.startTrip(tripId, data.otp);
          break;
        case 'complete':
          trip = await tripService.completeTrip(
            tripId,
            data?.actualDistanceKm,
            data?.actualDurationMin
          );
          break;
        default:
          throw new Error('Invalid action');
      }
      
      setActiveTrip(trip);
      return trip;
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Failed to update trip';
      addToast({ type: 'error', message });
      return null;
    } finally {
      setIsLoading(false);
    }
  }, [setActiveTrip, addToast]);

  return {
    isLoading,
    activeTrip,
    createTrip,
    getTrip,
    cancelTrip,
    rateTrip,
    getCustomerTrips,
    updateTripStatus,
  };
};

export default useTrip;
