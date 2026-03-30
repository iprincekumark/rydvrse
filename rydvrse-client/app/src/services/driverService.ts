/**
 * Driver Service
 * Handles driver profile, availability, and document management
 */

import { api } from './apiClient';
import type {
  Driver,
  DriverDocument,
  DriverEarnings,
  DocumentUploadRequest,
  PagedResponse,
  Trip,
} from '@/types';

const DRIVERS_BASE_URL = '/drivers';

export const driverService = {
  /**
   * Get current driver's profile
   */
  getMyProfile: async (): Promise<Driver> => {
    const response = await api.get<Driver>(`${DRIVERS_BASE_URL}/me`);
    return response.data;
  },

  /**
   * Get driver profile by ID
   */
  getProfile: async (driverId: string): Promise<Driver> => {
    const response = await api.get<Driver>(`${DRIVERS_BASE_URL}/${driverId}`);
    return response.data;
  },

  /**
   * Toggle driver availability (online/offline)
   */
  toggleAvailability: async (driverId: string, available: boolean): Promise<void> => {
    await api.put<void>(`${DRIVERS_BASE_URL}/${driverId}/availability`, { available });
  },

  /**
   * Upload driver document
   */
  uploadDocument: async (
    driverId: string,
    request: DocumentUploadRequest
  ): Promise<DriverDocument> => {
    const response = await api.post<DriverDocument>(
      `${DRIVERS_BASE_URL}/${driverId}/documents`,
      request
    );
    return response.data;
  },

  /**
   * Get driver's documents
   */
  getDocuments: async (driverId: string): Promise<DriverDocument[]> => {
    const response = await api.get<DriverDocument[]>(
      `${DRIVERS_BASE_URL}/${driverId}/documents`
    );
    return response.data;
  },

  /**
   * Get driver's earnings
   */
  getEarnings: async (driverId: string): Promise<DriverEarnings> => {
    const response = await api.get<DriverEarnings>(
      `${DRIVERS_BASE_URL}/${driverId}/earnings`
    );
    return response.data;
  },

  /**
   * Get driver's trip history
   */
  getTripHistory: async (
    driverId: string,
    page: number = 0,
    size: number = 20
  ): Promise<PagedResponse<Trip>> => {
    const response = await api.get<PagedResponse<Trip>>(
      `${DRIVERS_BASE_URL}/${driverId}/trips`,
      { page, size }
    );
    return response.data;
  },

  /**
   * Update driver location
   */
  updateLocation: async (
    driverId: string,
    latitude: number,
    longitude: number
  ): Promise<void> => {
    await api.put<void>(`${DRIVERS_BASE_URL}/${driverId}/location`, {
      latitude,
      longitude,
    });
  },
};

export default driverService;
