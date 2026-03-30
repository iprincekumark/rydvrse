/**
 * Authentication Store
 * Manages user authentication state using Zustand
 */

import { create } from 'zustand';
import { persist, createJSONStorage } from 'zustand/middleware';
import type { User, AuthTokens } from '@/types';

interface AuthState {
  // State
  user: User | null;
  accessToken: string | null;
  refreshToken: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  error: string | null;

  // Actions
  setUser: (user: User | null) => void;
  setTokens: (accessToken: string, refreshToken: string) => void;
  setAuthenticated: (isAuthenticated: boolean) => void;
  setLoading: (isLoading: boolean) => void;
  setError: (error: string | null) => void;
  login: (user: User, tokens: AuthTokens) => void;
  logout: () => void;
  updateUser: (updates: Partial<User>) => void;
  clearError: () => void;

  // Selectors
  isCustomer: () => boolean;
  isDriver: () => boolean;
  isAdmin: () => boolean;
  getUserRole: () => string | null;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      // Initial state
      user: null,
      accessToken: null,
      refreshToken: null,
      isAuthenticated: false,
      isLoading: false,
      error: null,

      // Actions
      setUser: (user) => set({ user }),

      setTokens: (accessToken, refreshToken) =>
        set({ accessToken, refreshToken }),

      setAuthenticated: (isAuthenticated) => set({ isAuthenticated }),

      setLoading: (isLoading) => set({ isLoading }),

      setError: (error) => set({ error }),

      login: (user, tokens) =>
        set({
          user,
          accessToken: tokens.accessToken,
          refreshToken: tokens.refreshToken,
          isAuthenticated: true,
          error: null,
        }),

      logout: () =>
        set({
          user: null,
          accessToken: null,
          refreshToken: null,
          isAuthenticated: false,
          error: null,
        }),

      updateUser: (updates) =>
        set((state) => ({
          user: state.user ? { ...state.user, ...updates } : null,
        })),

      clearError: () => set({ error: null }),

      // Selectors
      isCustomer: () => get().user?.role === 'CUSTOMER',
      isDriver: () => get().user?.role === 'DRIVER',
      isAdmin: () => get().user?.role === 'ADMIN',
      getUserRole: () => get().user?.role ?? null,
    }),
    {
      name: 'rydvrse-auth-storage',
      storage: createJSONStorage(() => localStorage),
      partialize: (state) => ({
        user: state.user,
        accessToken: state.accessToken,
        refreshToken: state.refreshToken,
        isAuthenticated: state.isAuthenticated,
      }),
    }
  )
);

export default useAuthStore;
