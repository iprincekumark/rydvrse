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
      const response = await authService.sendOtp({ phoneNumber });
      addToast({
        type: 'success',
        message: response.message || 'OTP sent successfully',
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
      const response = await authService.verifyOtp({ phoneNumber, otp });
      
      storeLogin(response.user, response.tokens);
      
      addToast({
        type: 'success',
        message: 'Login successful',
      });

      // Redirect based on role
      const role = response.user.role;
      if (response.isNewUser) {
        navigate('/profile/setup');
      } else if (role === UserRole.CUSTOMER) {
        navigate('/customer/dashboard');
      } else if (role === UserRole.DRIVER) {
        navigate('/driver/dashboard');
      } else if (role === UserRole.ADMIN) {
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
  const hasRole = useCallback((roles: UserRole[]): boolean => {
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
