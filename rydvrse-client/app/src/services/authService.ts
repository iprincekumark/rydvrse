/**
 * Authentication Service
 * Handles OTP-based authentication, token refresh, and logout
 */

import { api } from './apiClient';
import type {
  OtpRequest,
  OtpResponse,
  VerifyOtpRequest,
  RefreshTokenRequest,
  ApiResponse,
} from '@/types';

const AUTH_BASE_URL = '/auth';

/**
 * Server auth response DTO — flat structure with tokens directly.
 * Wrapped in ApiResponse<ServerAuthResponse> by the server.
 */
interface ServerAuthResponse {
  accessToken: string;
  refreshToken: string;
  isNewUser: boolean;
  userType: 'CUSTOMER' | 'DRIVER' | 'ADMIN';
}

export const authService = {
  /**
   * Send OTP to phone number.
   * Server returns ApiResponse<Void> — data is null, message has the status.
   */
  sendOtp: async (request: OtpRequest): Promise<ApiResponse<OtpResponse>> => {
    const response = await api.post<OtpResponse>(`${AUTH_BASE_URL}/otp/send`, request);
    // api.post returns the ApiResponse wrapper: { success, message, data }
    return response;
  },

  /**
   * Verify OTP and authenticate user
   * Server returns ApiResponse<ServerAuthResponse>
   */
  verifyOtp: async (request: VerifyOtpRequest): Promise<ServerAuthResponse> => {
    const response = await api.post<ServerAuthResponse>(`${AUTH_BASE_URL}/otp/verify`, request);
    // response = { success, message, data: { accessToken, refreshToken, isNewUser, userType } }
    return response.data;
  },

  /**
   * Refresh access token
   */
  refreshToken: async (request: RefreshTokenRequest): Promise<ServerAuthResponse> => {
    const response = await api.post<ServerAuthResponse>(`${AUTH_BASE_URL}/refresh`, request);
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
