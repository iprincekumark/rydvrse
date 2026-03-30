/**
 * Main Layout Component
 * Provides the main application layout with navigation and content area
 */

import React from 'react';
import { Outlet } from 'react-router-dom';
import { useAuthStore } from '@/store';
import { CustomerNavbar, DriverNavbar, AdminNavbar } from '@/components/common/Navbar';
import { Toaster } from '@/components/ui/sonner';

export const MainLayout: React.FC = () => {
  const { isCustomer, isDriver, isAdmin } = useAuthStore();

  const renderNavbar = () => {
    if (isCustomer()) return <CustomerNavbar />;
    if (isDriver()) return <DriverNavbar />;
    if (isAdmin()) return <AdminNavbar />;
    return null;
  };

  return (
    <div className="min-h-screen bg-background">
      {renderNavbar()}
      <main className="container mx-auto px-4 py-6">
        <Outlet />
      </main>
      <Toaster position="top-right" richColors />
    </div>
  );
};

export default MainLayout;
