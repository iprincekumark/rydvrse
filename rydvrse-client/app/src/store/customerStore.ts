/**
 * Customer Store
 * Manages customer profile, vehicles, and saved locations
 */

import { create } from 'zustand';
import { persist, createJSONStorage } from 'zustand/middleware';
import type { Customer, Vehicle, SavedLocation } from '@/types';

interface CustomerState {
  // State
  customer: Customer | null;
  vehicles: Vehicle[];
  savedLocations: SavedLocation[];
  defaultVehicle: Vehicle | null;
  isLoading: boolean;
  error: string | null;

  // Actions
  setCustomer: (customer: Customer | null) => void;
  setVehicles: (vehicles: Vehicle[]) => void;
  addVehicle: (vehicle: Vehicle) => void;
  updateVehicle: (vehicleId: string, updates: Partial<Vehicle>) => void;
  removeVehicle: (vehicleId: string) => void;
  setDefaultVehicle: (vehicle: Vehicle | null) => void;
  setSavedLocations: (locations: SavedLocation[]) => void;
  addSavedLocation: (location: SavedLocation) => void;
  removeSavedLocation: (locationId: string) => void;
  setLoading: (isLoading: boolean) => void;
  setError: (error: string | null) => void;
  clearError: () => void;
  reset: () => void;

  // Selectors
  getDefaultVehicle: () => Vehicle | null;
  hasVehicles: () => boolean;
  getVehicleById: (id: string) => Vehicle | undefined;
}

export const useCustomerStore = create<CustomerState>()(
  persist(
    (set, get) => ({
      // Initial state
      customer: null,
      vehicles: [],
      savedLocations: [],
      defaultVehicle: null,
      isLoading: false,
      error: null,

      // Actions
      setCustomer: (customer) => set({ customer }),

      setVehicles: (vehicles) => {
        set({ vehicles });
        // Update default vehicle
        const defaultV = vehicles.find((v) => v.isDefault) || vehicles[0] || null;
        set({ defaultVehicle: defaultV });
      },

      addVehicle: (vehicle) =>
        set((state) => {
          const newVehicles = [...state.vehicles, vehicle];
          // If this is the first vehicle or isDefault, update default
          const defaultV =
            newVehicles.find((v) => v.isDefault) || newVehicles[0] || null;
          return { vehicles: newVehicles, defaultVehicle: defaultV };
        }),

      updateVehicle: (vehicleId, updates) =>
        set((state) => {
          const newVehicles = state.vehicles.map((v) =>
            v.id === vehicleId ? { ...v, ...updates } : v
          );
          // Update default vehicle if needed
          const defaultV =
            newVehicles.find((v) => v.isDefault) || newVehicles[0] || null;
          return { vehicles: newVehicles, defaultVehicle: defaultV };
        }),

      removeVehicle: (vehicleId) =>
        set((state) => {
          const newVehicles = state.vehicles.filter((v) => v.id !== vehicleId);
          const defaultV =
            newVehicles.find((v) => v.isDefault) || newVehicles[0] || null;
          return { vehicles: newVehicles, defaultVehicle: defaultV };
        }),

      setDefaultVehicle: (vehicle) => set({ defaultVehicle: vehicle }),

      setSavedLocations: (locations) => set({ savedLocations: locations }),

      addSavedLocation: (location) =>
        set((state) => ({
          savedLocations: [...state.savedLocations, location],
        })),

      removeSavedLocation: (locationId) =>
        set((state) => ({
          savedLocations: state.savedLocations.filter(
            (l) => l.id !== locationId
          ),
        })),

      setLoading: (isLoading) => set({ isLoading }),

      setError: (error) => set({ error }),

      clearError: () => set({ error: null }),

      reset: () =>
        set({
          customer: null,
          vehicles: [],
          savedLocations: [],
          defaultVehicle: null,
          isLoading: false,
          error: null,
        }),

      // Selectors
      getDefaultVehicle: () => {
        const state = get();
        return (
          state.vehicles.find((v) => v.isDefault) || state.vehicles[0] || null
        );
      },

      hasVehicles: () => get().vehicles.length > 0,

      getVehicleById: (id) => get().vehicles.find((v) => v.id === id),
    }),
    {
      name: 'rydvrse-customer-storage',
      storage: createJSONStorage(() => localStorage),
      partialize: (state) => ({
        customer: state.customer,
        vehicles: state.vehicles,
        savedLocations: state.savedLocations,
        defaultVehicle: state.defaultVehicle,
      }),
    }
  )
);

export default useCustomerStore;
