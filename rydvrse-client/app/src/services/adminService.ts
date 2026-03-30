/**
 * Admin Service
 * Handles admin dashboard, driver approvals, and platform management
 */

import { api } from './apiClient';
import type {
  AdminDashboardStats,
  Driver,
  DriverStatus,
  Trip,
  PagedResponse,
  User,
} from '@/types';

const ADMIN_BASE_URL = '/admin';

export const adminService = {
  /**
   * Get dashboard statistics
   */
  getDashboardStats: async (): Promise<AdminDashboardStats> => {
    const response = await api.get<AdminDashboardStats>(`${ADMIN_BASE_URL}/dashboard/stats`);
    return response.data;
  },

  /**
   * Get pending driver approvals
   */
  getPendingDriverApprovals: async (
    page: number = 0,
    size: number = 20
  ): Promise<PagedResponse<Driver>> => {
    const response = await api.get<PagedResponse<Driver>>(
      `${ADMIN_BASE_URL}/drivers/pending`,
      { page, size }
    );
    return response.data;
  },

  /**
   * Approve or reject driver
   */
  updateDriverStatus: async (
    driverId: string,
    status: DriverStatus,
    rejectionReason?: string
  ): Promise<Driver> => {
    const response = await api.post<Driver>(
      `${ADMIN_BASE_URL}/drivers/${driverId}/status`,
      { status, rejectionReason }
    );
    return response.data;
  },

  /**
   * Get all drivers
   */
  getAllDrivers: async (
    page: number = 0,
    size: number = 20,
    status?: DriverStatus
  ): Promise<PagedResponse<Driver>> => {
    const response = await api.get<PagedResponse<Driver>>(
      `${ADMIN_BASE_URL}/drivers`,
      { page, size, status }
    );
    return response.data;
  },

  /**
   * Get all customers
   */
  getAllCustomers: async (
    page: number = 0,
    size: number = 20
  ): Promise<PagedResponse<User>> => {
    const response = await api.get<PagedResponse<User>>(
      `${ADMIN_BASE_URL}/customers`,
      { page, size }
    );
    return response.data;
  },

  /**
   * Get all trips
   */
  getAllTrips: async (
    page: number = 0,
    size: number = 20,
    status?: string
  ): Promise<PagedResponse<Trip>> => {
    const response = await api.get<PagedResponse<Trip>>(
      `${ADMIN_BASE_URL}/trips`,
      { page, size, status }
    );
    return response.data;
  },

  /**
   * Get trip details
   */
  getTripDetails: async (tripId: string): Promise<Trip> => {
    const response = await api.get<Trip>(`${ADMIN_BASE_URL}/trips/${tripId}`);
    return response.data;
  },

  /**
   * Resolve trip dispute
   */
  resolveDispute: async (
    tripId: string,
    resolution: string,
    refundAmount?: number
  ): Promise<Trip> => {
    const response = await api.post<Trip>(
      `${ADMIN_BASE_URL}/trips/${tripId}/resolve`,
      { resolution, refundAmount }
    );
    return response.data;
  },

  /**
   * Suspend user
   */
  suspendUser: async (userId: string, reason: string): Promise<void> => {
    await api.post<void>(`${ADMIN_BASE_URL}/users/${userId}/suspend`, { reason });
  },

  /**
   * Reactivate user
   */
  reactivateUser: async (userId: string): Promise<void> => {
    await api.post<void>(`${ADMIN_BASE_URL}/users/${userId}/reactivate`, {});
  },
};

export default adminService;
