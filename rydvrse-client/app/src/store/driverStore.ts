/**
 * Driver Store
 * Manages driver state, availability, and earnings
 */

import { create } from 'zustand';
import { persist, createJSONStorage } from 'zustand/middleware';
import type { Driver, DriverDocument, DriverEarnings, DriverLocation } from '@/types';
import { DriverAvailability } from '@/types';

interface DriverState {
  // State
  driver: Driver | null;
  documents: DriverDocument[];
  earnings: DriverEarnings | null;
  currentLocation: DriverLocation | null;
  isOnline: boolean;
  incomingTripRequest: unknown | null;
  isLoading: boolean;
  error: string | null;

  // Actions
  setDriver: (driver: Driver | null) => void;
  setDocuments: (documents: DriverDocument[]) => void;
  addDocument: (document: DriverDocument) => void;
  setEarnings: (earnings: DriverEarnings | null) => void;
  setCurrentLocation: (location: DriverLocation | null) => void;
  setIsOnline: (isOnline: boolean) => void;
  setAvailability: (availability: string) => void;
  setIncomingTripRequest: (request: unknown | null) => void;
  updateDriverStatus: (status: string) => void;
  setLoading: (isLoading: boolean) => void;
  setError: (error: string | null) => void;
  clearError: () => void;
  reset: () => void;

  // Selectors
  isAvailable: () => boolean;
  isVerified: () => boolean;
  hasPendingDocuments: () => boolean;
}

export const useDriverStore = create<DriverState>()(
  persist(
    (set, get) => ({
      // Initial state
      driver: null,
      documents: [],
      earnings: null,
      currentLocation: null,
      isOnline: false,
      incomingTripRequest: null,
      isLoading: false,
      error: null,

      // Actions
      setDriver: (driver) => set({ driver }),

      setDocuments: (documents) => set({ documents }),

      addDocument: (document) =>
        set((state) => ({
          documents: [...state.documents, document],
        })),

      setEarnings: (earnings) => set({ earnings }),

      setCurrentLocation: (location) => set({ currentLocation: location }),

      setIsOnline: (isOnline) => set({ isOnline }),

      setAvailability: (availability) =>
        set((state) => ({
          driver: state.driver
            ? { ...state.driver, availability: availability as Driver['availability'] }
            : null,
        })),

      setIncomingTripRequest: (request) =>
        set({ incomingTripRequest: request }),

      updateDriverStatus: (status) =>
        set((state) => ({
          driver: state.driver ? { ...state.driver, status: status as Driver['status'] } : null,
        })),

      setLoading: (isLoading) => set({ isLoading }),

      setError: (error) => set({ error }),

      clearError: () => set({ error: null }),

      reset: () =>
        set({
          driver: null,
          documents: [],
          earnings: null,
          currentLocation: null,
          isOnline: false,
          incomingTripRequest: null,
          isLoading: false,
          error: null,
        }),

      // Selectors
      isAvailable: () =>
        get().driver?.availability === DriverAvailability.ONLINE,

      isVerified: () =>
        get().driver?.status === 'APPROVED' ||
        get().driver?.status === 'ACTIVE',

      hasPendingDocuments: () =>
        get().documents.some((doc) => doc.status === 'PENDING'),
    }),
    {
      name: 'rydvrse-driver-storage',
      storage: createJSONStorage(() => localStorage),
      partialize: (state) => ({
        driver: state.driver,
        documents: state.documents,
        isOnline: state.isOnline,
      }),
    }
  )
);

export default useDriverStore;
