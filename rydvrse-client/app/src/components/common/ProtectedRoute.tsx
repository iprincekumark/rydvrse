/**
 * Protected Route Components
 * Route guards for authentication and role-based access control
 */

import type { ReactNode } from 'react';
import { Navigate } from 'react-router-dom';
import { useAuthStore } from '@/store/authStore';
import { UserRole } from '@/types';

// Loading Spinner component
const LoadingSpinner = () => (
  <div className="min-h-screen flex items-center justify-center">
    <div className="flex flex-col items-center gap-3">
      <div className="w-8 h-8 border-2 border-muted border-t-primary rounded-full animate-spin" />
      <p className="text-sm text-muted-foreground">Loading...</p>
    </div>
  </div>
);

// Helper to get the role-based dashboard path
const getDashboardPath = (role?: string): string => {
  switch (role) {
    case UserRole.CUSTOMER:
      return '/customer/dashboard';
    case UserRole.DRIVER:
      return '/driver/dashboard';
    case UserRole.ADMIN:
    case UserRole.OPERATIONS:
      return '/admin/dashboard';
    default:
      return '/login';
  }
};

interface ProtectedRouteProps {
  children: ReactNode;
  allowedRoles?: string[];
}

/**
 * ProtectedRoute - Requires authentication and optionally specific roles
 */
export const ProtectedRoute = ({ children, allowedRoles }: ProtectedRouteProps) => {
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);
  const isLoading = useAuthStore((state) => state.isLoading);
  const user = useAuthStore((state) => state.user);

  // Show loading spinner while auth state is being determined
  if (isLoading) {
    return <LoadingSpinner />;
  }

  // Redirect to login if not authenticated
  if (!isAuthenticated || !user) {
    return <Navigate to="/login" replace />;
  }

  // Check role-based access
  if (allowedRoles && allowedRoles.length > 0 && !allowedRoles.includes(user.role)) {
    // Redirect to appropriate dashboard for their role
    return <Navigate to={getDashboardPath(user.role)} replace />;
  }

  return <>{children}</>;
};

/**
 * PublicRoute - Only accessible when NOT authenticated
 */
export const PublicRoute = ({ children }: { children: ReactNode }) => {
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);
  const isLoading = useAuthStore((state) => state.isLoading);
  const user = useAuthStore((state) => state.user);

  // Show loading spinner while auth state is being determined
  if (isLoading) {
    return <LoadingSpinner />;
  }

  // If authenticated, redirect to appropriate dashboard
  if (isAuthenticated && user) {
    return <Navigate to={getDashboardPath(user.role)} replace />;
  }

  return <>{children}</>;
};

/**
 * Pre-configured route guards for specific roles
 */
export const CustomerRoute = ({ children }: { children: ReactNode }) => (
  <ProtectedRoute allowedRoles={[UserRole.CUSTOMER]}>
    {children}
  </ProtectedRoute>
);

export const DriverRoute = ({ children }: { children: ReactNode }) => (
  <ProtectedRoute allowedRoles={[UserRole.DRIVER]}>
    {children}
  </ProtectedRoute>
);

export const AdminRoute = ({ children }: { children: ReactNode }) => (
  <ProtectedRoute allowedRoles={[UserRole.ADMIN, UserRole.OPERATIONS]}>
    {children}
  </ProtectedRoute>
);

export default ProtectedRoute;
