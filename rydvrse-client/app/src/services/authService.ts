/**
 * Authentication Service
 * Handles OTP-based authentication, token refresh, and logout
 */

import { api } from './apiClient';
import type {
  AuthResponse,
  OtpRequest,
  OtpResponse,
  VerifyOtpRequest,
  RefreshTokenRequest,
} from '@/types';

const AUTH_BASE_URL = '/auth';

export const authService = {
  /**
   * Send OTP to phone number
   */
  sendOtp: async (request: OtpRequest): Promise<OtpResponse> => {
    const response = await api.post<OtpResponse>(`${AUTH_BASE_URL}/otp/send`, request);
    return response.data;
  },

  /**
   * Verify OTP and authenticate user
   */
  verifyOtp: async (request: VerifyOtpRequest): Promise<AuthResponse> => {
    const response = await api.post<AuthResponse>(`${AUTH_BASE_URL}/otp/verify`, request);
    return response.data;
  },

  /**
   * Refresh access token
   */
  refreshToken: async (request: RefreshTokenRequest): Promise<AuthResponse> => {
    const response = await api.post<AuthResponse>(`${AUTH_BASE_URL}/token/refresh`, request);
    return response.data;
  },

  /**
   * Logout user and revoke tokens
   */
  logout: async (): Promise<void> => {
    await api.post<void>(`${AUTH_BASE_URL}/logout`, {});
  },
};

export default authService;
