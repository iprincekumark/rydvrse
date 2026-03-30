/**
 * useAuth Hook
 * Handles authentication logic and operations
 */

import { useState, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { authService } from '@/services';
import { useAuthStore, useUIStore } from '@/store';
import { UserRole } from '@/types';

export const useAuth = () => {
  const navigate = useNavigate();
  const [isLoading, setIsLoading] = useState(false);
  
  const { login: storeLogin, logout: storeLogout, isAuthenticated, user } = useAuthStore();
  const { addToast } = useUIStore();

  /**
   * Send OTP to phone number
   */
  const sendOtp = useCallback(async (phoneNumber: string): Promise<boolean> => {
    setIsLoading(true);
    try {
      const phone = phoneNumber.startsWith('+91') ? phoneNumber : `+91${phoneNumber}`;
      const response = await authService.sendOtp({ phone, userType: 'CUSTOMER' });
      // response is ApiResponse wrapper: { success, message, data }
      addToast({
        type: 'success',
        message: response?.message || 'OTP sent successfully',
      });
      return true;
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Failed to send OTP';
      addToast({ type: 'error', message });
      return false;
    } finally {
      setIsLoading(false);
    }
  }, [addToast]);

  /**
   * Verify OTP and login
   */
  const verifyOtp = useCallback(async (phoneNumber: string, otp: string): Promise<boolean> => {
    setIsLoading(true);
    try {
      const phone = phoneNumber.startsWith('+91') ? phoneNumber : `+91${phoneNumber}`;
      const response = await authService.verifyOtp({ phone, otp, userType: 'CUSTOMER' });
      // response is ServerAuthResponse: { accessToken, refreshToken, isNewUser, userType }
      
      // Build user and tokens for the store from the flat server response
      const tokens = {
        accessToken: response.accessToken,
        refreshToken: response.refreshToken,
        expiresIn: 900, // 15 min (from server config)
      };

      // For the auth store we create a minimal user object — full profile loads on dashboard
      const userObj = {
        id: '',
        phoneNumber: phone,
        firstName: '',
        lastName: '',
        role: response.userType as string as typeof UserRole[keyof typeof UserRole],
        status: 'ACTIVE' as const,
        email: undefined,
        profileImageUrl: undefined,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      };

      storeLogin(userObj, tokens);
      
      addToast({
        type: 'success',
        message: 'Login successful',
      });

      // Redirect based on role
      const role = response.userType;
      if (response.isNewUser) {
        navigate('/profile/setup');
      } else if (role === 'CUSTOMER') {
        navigate('/customer/dashboard');
      } else if (role === 'DRIVER') {
        navigate('/driver/dashboard');
      } else if (role === 'ADMIN') {
        navigate('/admin/dashboard');
      }

      return true;
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Invalid OTP';
      addToast({ type: 'error', message });
      return false;
    } finally {
      setIsLoading(false);
    }
  }, [storeLogin, navigate, addToast]);

  /**
   * Logout user
   */
  const logout = useCallback(async (): Promise<void> => {
    setIsLoading(true);
    try {
      await authService.logout();
      storeLogout();
      addToast({
        type: 'success',
        message: 'Logged out successfully',
      });
      navigate('/login');
    } catch (error) {
      // Even if logout fails on server, clear local state
      storeLogout();
      navigate('/login');
    } finally {
      setIsLoading(false);
    }
  }, [storeLogout, navigate, addToast]);

  /**
   * Check if user has required role
   */
  const hasRole = useCallback((roles: (typeof UserRole[keyof typeof UserRole])[]): boolean => {
    if (!user) return false;
    return roles.includes(user.role);
  }, [user]);

  return {
    isLoading,
    isAuthenticated,
    user,
    sendOtp,
    verifyOtp,
    logout,
    hasRole,
  };
};

export default useAuth;
