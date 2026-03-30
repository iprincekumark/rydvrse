/**
 * Customer Service
 * Handles customer profile, vehicles, and saved locations
 */

import { api } from './apiClient';
import type {
  Customer,
  CustomerProfile,
  Vehicle,
  SavedLocation,
  GeoLocation,
  PagedResponse,
  Trip,
} from '@/types';

const CUSTOMERS_BASE_URL = '/customers';

export const customerService = {
  /**
   * Get current customer's profile
   */
  getMyProfile: async (): Promise<Customer> => {
    const response = await api.get<Customer>(`${CUSTOMERS_BASE_URL}/me`);
    return response.data;
  },

  /**
   * Get customer profile by ID
   */
  getProfile: async (customerId: string): Promise<CustomerProfile> => {
    const response = await api.get<CustomerProfile>(`${CUSTOMERS_BASE_URL}/${customerId}`);
    return response.data;
  },

  /**
   * Update customer profile
   */
  updateProfile: async (
    customerId: string,
    data: Partial<Customer>
  ): Promise<Customer> => {
    const response = await api.put<Customer>(`${CUSTOMERS_BASE_URL}/${customerId}`, data);
    return response.data;
  },

  /**
   * Get customer's vehicles
   */
  getVehicles: async (customerId: string): Promise<Vehicle[]> => {
    const response = await api.get<Vehicle[]>(
      `${CUSTOMERS_BASE_URL}/${customerId}/vehicles`
    );
    return response.data;
  },

  /**
   * Add vehicle to customer profile
   */
  addVehicle: async (customerId: string, vehicle: Omit<Vehicle, 'id' | 'createdAt' | 'updatedAt'>): Promise<Vehicle> => {
    const response = await api.post<Vehicle>(
      `${CUSTOMERS_BASE_URL}/${customerId}/vehicles`,
      vehicle
    );
    return response.data;
  },

  /**
   * Update vehicle
   */
  updateVehicle: async (
    customerId: string,
    vehicleId: string,
    data: Partial<Vehicle>
  ): Promise<Vehicle> => {
    const response = await api.put<Vehicle>(
      `${CUSTOMERS_BASE_URL}/${customerId}/vehicles/${vehicleId}`,
      data
    );
    return response.data;
  },

  /**
   * Delete vehicle
   */
  deleteVehicle: async (customerId: string, vehicleId: string): Promise<void> => {
    await api.delete<void>(
      `${CUSTOMERS_BASE_URL}/${customerId}/vehicles/${vehicleId}`
    );
  },

  /**
   * Get customer's saved locations
   */
  getSavedLocations: async (customerId: string): Promise<SavedLocation[]> => {
    const response = await api.get<SavedLocation[]>(
      `${CUSTOMERS_BASE_URL}/${customerId}/locations`
    );
    return response.data;
  },

  /**
   * Add saved location
   */
  addSavedLocation: async (
    customerId: string,
    name: string,
    location: GeoLocation,
    type: 'HOME' | 'WORK' | 'OTHER'
  ): Promise<SavedLocation> => {
    const response = await api.post<SavedLocation>(
      `${CUSTOMERS_BASE_URL}/${customerId}/locations`,
      { name, location, type }
    );
    return response.data;
  },

  /**
   * Delete saved location
   */
  deleteSavedLocation: async (customerId: string, locationId: string): Promise<void> => {
    await api.delete<void>(
      `${CUSTOMERS_BASE_URL}/${customerId}/locations/${locationId}`
    );
  },

  /**
   * Get customer's trip history
   */
  getTripHistory: async (
    customerId: string,
    page: number = 0,
    size: number = 20
  ): Promise<PagedResponse<Trip>> => {
    const response = await api.get<PagedResponse<Trip>>(
      `${CUSTOMERS_BASE_URL}/${customerId}/trips`,
      { page, size }
    );
    return response.data;
  },
};

export default customerService;
