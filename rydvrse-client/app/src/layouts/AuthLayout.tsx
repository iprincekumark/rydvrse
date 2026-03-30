/**
 * Auth Layout Component
 * Layout for authentication pages (login, register, etc.)
 */

import React from 'react';
import { Outlet } from 'react-router-dom';
import { Car } from 'lucide-react';

export const AuthLayout: React.FC = () => {
  return (
    <div className="min-h-screen bg-gradient-to-br from-primary/5 via-background to-primary/10 flex items-center justify-center p-4">
      <div className="w-full max-w-md">
        {/* Logo and Brand */}
        <div className="text-center mb-8">
          <div className="inline-flex items-center justify-center w-16 h-16 rounded-full bg-primary mb-4">
            <Car className="w-8 h-8 text-primary-foreground" />
          </div>
          <h1 className="text-3xl font-bold text-foreground">RYDVRSE</h1>
          <p className="text-muted-foreground mt-2">
            Your Car. Our Driver. Your Destination.
          </p>
        </div>

        {/* Content */}
        <div className="bg-card rounded-xl shadow-lg border p-6">
          <Outlet />
        </div>

        {/* Footer */}
        <p className="text-center text-sm text-muted-foreground mt-6">
          By continuing, you agree to our Terms of Service and Privacy Policy
        </p>
      </div>
    </div>
  );
};

export default AuthLayout;
