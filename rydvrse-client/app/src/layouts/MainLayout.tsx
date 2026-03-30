/**
 * Main Layout — Glassmorphism navbar + mobile floating dock + page transitions
 */

import React from 'react';
import { Outlet, useLocation } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import { useAuthStore } from '@/store';
import { Toaster } from '@/components/ui/sonner';
import { AppNavbar } from '@/components/common/Navbar';
import {
  Home,
  Car,
  Clock,
  User,
  Shield,
  BarChart3,
  Users,
  MapPin,
  Wallet,
  FileText,
} from 'lucide-react';
import { FloatingDock } from '@/components/aceternity/floating-dock';

export const MainLayout: React.FC = () => {
  const { isCustomer, isDriver } = useAuthStore();
  const location = useLocation();

  const customerDockItems = [
    { title: 'Home', icon: <Home className="h-5 w-5" />, href: '/customer/dashboard' },
    { title: 'Book', icon: <Car className="h-5 w-5" />, href: '/customer/book' },
    { title: 'Trips', icon: <Clock className="h-5 w-5" />, href: '/customer/trips' },
    { title: 'Wallet', icon: <Wallet className="h-5 w-5" />, href: '/customer/wallet' },
    { title: 'Profile', icon: <User className="h-5 w-5" />, href: '/customer/profile' },
  ];

  const driverDockItems = [
    { title: 'Home', icon: <Home className="h-5 w-5" />, href: '/driver/dashboard' },
    { title: 'Trips', icon: <MapPin className="h-5 w-5" />, href: '/driver/trips' },
    { title: 'Earnings', icon: <Wallet className="h-5 w-5" />, href: '/driver/earnings' },
    { title: 'Documents', icon: <FileText className="h-5 w-5" />, href: '/driver/documents' },
    { title: 'Profile', icon: <User className="h-5 w-5" />, href: '/driver/profile' },
  ];

  const adminDockItems = [
    { title: 'Dashboard', icon: <BarChart3 className="h-5 w-5" />, href: '/admin/dashboard' },
    { title: 'Drivers', icon: <Users className="h-5 w-5" />, href: '/admin/drivers' },
    { title: 'Trips', icon: <Car className="h-5 w-5" />, href: '/admin/trips' },
    { title: 'Safety', icon: <Shield className="h-5 w-5" />, href: '/admin/safety' },
    { title: 'Settings', icon: <User className="h-5 w-5" />, href: '/admin/settings' },
  ];

  const dockItems = isCustomer()
    ? customerDockItems
    : isDriver()
    ? driverDockItems
    : adminDockItems;

  return (
    <div className="min-h-screen bg-background relative">
      {/* Subtle background */}
      <div className="fixed inset-0 gradient-mesh pointer-events-none" />

      {/* Desktop Navbar */}
      <AppNavbar />

      {/* Main Content with page transitions */}
      <main className="relative z-10 container mx-auto px-4 py-6 pb-24 md:pb-6">
        <AnimatePresence mode="wait">
          <motion.div
            key={location.pathname}
            initial={{ opacity: 0, y: 8 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -8 }}
            transition={{ duration: 0.2, ease: "easeInOut" }}
          >
            <Outlet />
          </motion.div>
        </AnimatePresence>
      </main>

      {/* Mobile Floating Dock */}
      <div className="md:hidden">
        <FloatingDock items={dockItems} />
      </div>

      <Toaster position="top-right" richColors />
    </div>
  );
};

export default MainLayout;
