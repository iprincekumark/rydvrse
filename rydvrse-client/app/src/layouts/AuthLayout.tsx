/**
 * Auth Layout — Full-screen dark layout with Spotlight background
 */

import React from 'react';
import { Outlet, Navigate } from 'react-router-dom';
import { useAuthStore } from '@/store';
import { Spotlight } from '@/components/aceternity/spotlight';
import { Toaster } from '@/components/ui/sonner';

export const AuthLayout: React.FC = () => {
  const { isAuthenticated, isCustomer, isDriver, isAdmin } = useAuthStore();

  // Redirect if already authenticated
  if (isAuthenticated) {
    if (isCustomer()) return <Navigate to="/customer/dashboard" replace />;
    if (isDriver()) return <Navigate to="/driver/dashboard" replace />;
    if (isAdmin()) return <Navigate to="/admin/dashboard" replace />;
  }

  return (
    <div className="min-h-screen relative flex items-center justify-center overflow-hidden">
      {/* Background layers */}
      <div className="absolute inset-0 gradient-mesh" />
      <div className="absolute inset-0 dot-pattern opacity-30" />

      {/* Spotlight effect */}
      <Spotlight
        className="-top-40 left-0 md:left-60 md:-top-20"
        fill="hsl(217 91% 60%)"
      />

      {/* Content */}
      <div className="relative z-10 w-full max-w-md mx-auto px-4">
        {/* Logo */}
        <div className="text-center mb-8">
          <h1 className="text-4xl font-black tracking-tight">
            <span className="gradient-text">RYDVRSE</span>
          </h1>
          <p className="text-sm text-muted-foreground mt-1 tracking-widest uppercase">
            Your Car. Our Driver.
          </p>
        </div>

        {/* Page content */}
        <Outlet />
      </div>

      <Toaster position="top-right" richColors />
    </div>
  );
};

export default AuthLayout;
