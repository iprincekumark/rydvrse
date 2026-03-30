/**
 * Trip Store
 * Manages trip state and active trip tracking
 */

import { create } from 'zustand';
import { persist, createJSONStorage } from 'zustand/middleware';
import type { Trip, DriverLocation } from '@/types';

interface TripState {
  // State
  activeTrip: Trip | null;
  tripHistory: Trip[];
  selectedTrip: Trip | null;
  driverLocation: DriverLocation | null;
  estimatedArrival: number | null;
  isLoading: boolean;
  error: string | null;

  // Actions
  setActiveTrip: (trip: Trip | null) => void;
  setSelectedTrip: (trip: Trip | null) => void;
  updateTripStatus: (status: string) => void;
  setDriverLocation: (location: DriverLocation | null) => void;
  setEstimatedArrival: (minutes: number | null) => void;
  addToHistory: (trip: Trip) => void;
  setTripHistory: (trips: Trip[]) => void;
  setLoading: (isLoading: boolean) => void;
  setError: (error: string | null) => void;
  clearActiveTrip: () => void;
  clearError: () => void;

  // Selectors
  hasActiveTrip: () => boolean;
  canCancelTrip: () => boolean;
  getTripStatus: () => string | null;
}

const COMPLETED_STATUSES = ['TRIP_COMPLETED', 'CANCELLED'];
const CANCELLABLE_STATUSES = ['REQUESTED', 'DRIVER_MATCHING', 'DRIVER_ASSIGNED', 'DRIVER_ARRIVING'];

export const useTripStore = create<TripState>()(
  persist(
    (set, get) => ({
      // Initial state
      activeTrip: null,
      tripHistory: [],
      selectedTrip: null,
      driverLocation: null,
      estimatedArrival: null,
      isLoading: false,
      error: null,

      // Actions
      setActiveTrip: (trip) => set({ activeTrip: trip }),

      setSelectedTrip: (trip) => set({ selectedTrip: trip }),

      updateTripStatus: (status) =>
        set((state) => ({
          activeTrip: state.activeTrip
            ? { ...state.activeTrip, status: status as Trip['status'] }
            : null,
        })),

      setDriverLocation: (location) => set({ driverLocation: location }),

      setEstimatedArrival: (minutes) => set({ estimatedArrival: minutes }),

      addToHistory: (trip) =>
        set((state) => ({
          tripHistory: [trip, ...state.tripHistory],
        })),

      setTripHistory: (trips) => set({ tripHistory: trips }),

      setLoading: (isLoading) => set({ isLoading }),

      setError: (error) => set({ error }),

      clearActiveTrip: () =>
        set({
          activeTrip: null,
          driverLocation: null,
          estimatedArrival: null,
        }),

      clearError: () => set({ error: null }),

      // Selectors
      hasActiveTrip: () => {
        const trip = get().activeTrip;
        if (!trip) return false;
        return !COMPLETED_STATUSES.includes(trip.status);
      },

      canCancelTrip: () => {
        const trip = get().activeTrip;
        if (!trip) return false;
        return CANCELLABLE_STATUSES.includes(trip.status);
      },

      getTripStatus: () => get().activeTrip?.status ?? null,
    }),
    {
      name: 'rydvrse-trip-storage',
      storage: createJSONStorage(() => localStorage),
      partialize: (state) => ({
        activeTrip: state.activeTrip,
        tripHistory: state.tripHistory,
      }),
    }
  )
);

export default useTripStore;
