/**
 * Navbar — Glassmorphism desktop navigation with role-based menus
 */

import React from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { motion } from 'framer-motion';
import {
  Home, Car, Clock, User, Settings, Shield,
  BarChart3, Users, LogOut, Wallet, FileText, MapPin, Bell,
} from 'lucide-react';
import { Button } from '@/components/ui/button';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';
import { Avatar, AvatarFallback } from '@/components/ui/avatar';
import { useAuthStore } from '@/store';
import { cn } from '@/lib/utils';

type NavItem = {
  label: string;
  href: string;
  icon: React.ReactNode;
};

const NavLink = ({ item, isActive }: { item: NavItem; isActive: boolean }) => {
  const navigate = useNavigate();
  return (
    <button
      onClick={() => navigate(item.href)}
      className={cn(
        "relative flex items-center gap-2 px-3 py-2 rounded-lg text-sm font-medium transition-colors",
        isActive
          ? "text-primary"
          : "text-muted-foreground hover:text-foreground hover:bg-white/5"
      )}
    >
      {item.icon}
      <span className="hidden lg:inline">{item.label}</span>
      {isActive && (
        <motion.div
          layoutId="activeNav"
          className="absolute inset-0 bg-primary/10 rounded-lg border border-primary/20"
          transition={{ type: "spring", bounce: 0.2, duration: 0.6 }}
        />
      )}
    </button>
  );
};

export const AppNavbar: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { user, logout, isCustomer, isDriver, isAdmin } = useAuthStore();

  const customerLinks: NavItem[] = [
    { label: 'Dashboard', href: '/customer/dashboard', icon: <Home className="h-4 w-4" /> },
    { label: 'Book Ride', href: '/customer/book', icon: <Car className="h-4 w-4" /> },
    { label: 'My Trips', href: '/customer/trips', icon: <Clock className="h-4 w-4" /> },
    { label: 'Wallet', href: '/customer/wallet', icon: <Wallet className="h-4 w-4" /> },
  ];

  const driverLinks: NavItem[] = [
    { label: 'Dashboard', href: '/driver/dashboard', icon: <Home className="h-4 w-4" /> },
    { label: 'Trips', href: '/driver/trips', icon: <MapPin className="h-4 w-4" /> },
    { label: 'Earnings', href: '/driver/earnings', icon: <Wallet className="h-4 w-4" /> },
    { label: 'Documents', href: '/driver/documents', icon: <FileText className="h-4 w-4" /> },
  ];

  const adminLinks: NavItem[] = [
    { label: 'Dashboard', href: '/admin/dashboard', icon: <BarChart3 className="h-4 w-4" /> },
    { label: 'Drivers', href: '/admin/drivers', icon: <Users className="h-4 w-4" /> },
    { label: 'Trips', href: '/admin/trips', icon: <Car className="h-4 w-4" /> },
    { label: 'Safety', href: '/admin/safety', icon: <Shield className="h-4 w-4" /> },
  ];

  const navLinks = isCustomer()
    ? customerLinks
    : isDriver()
    ? driverLinks
    : adminLinks;

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <header className="sticky top-0 z-40 hidden md:block">
      <div className="glass border-b border-white/[0.08]">
        <div className="container mx-auto px-4">
          <div className="flex items-center justify-between h-16">
            {/* Logo */}
            <button
              onClick={() => navigate(navLinks[0]?.href || '/')}
              className="flex items-center gap-2"
            >
              <span className="text-xl font-black gradient-text tracking-tight">RYDVRSE</span>
            </button>

            {/* Nav Links */}
            <nav className="flex items-center gap-1">
              {navLinks.map((item) => (
                <NavLink
                  key={item.href}
                  item={item}
                  isActive={
                    location.pathname === item.href ||
                    (item.href !== navLinks[0]?.href && location.pathname.startsWith(item.href))
                  }
                />
              ))}
            </nav>

            {/* Right side */}
            <div className="flex items-center gap-2">
              <Button variant="ghost" size="icon" className="text-muted-foreground hover:text-foreground">
                <Bell className="h-4 w-4" />
              </Button>

              <DropdownMenu>
                <DropdownMenuTrigger asChild>
                  <Button variant="ghost" className="relative h-9 w-9 rounded-full">
                    <Avatar className="h-9 w-9 border border-white/10">
                      <AvatarFallback className="bg-primary/20 text-primary text-sm font-semibold">
                        {user?.firstName?.[0]}{user?.lastName?.[0]}
                      </AvatarFallback>
                    </Avatar>
                  </Button>
                </DropdownMenuTrigger>
                <DropdownMenuContent className="w-56 glass" align="end" forceMount>
                  <DropdownMenuLabel className="font-normal">
                    <div className="flex flex-col space-y-1">
                      <p className="text-sm font-medium">{user?.firstName} {user?.lastName}</p>
                      <p className="text-xs text-muted-foreground">{user?.phoneNumber}</p>
                    </div>
                  </DropdownMenuLabel>
                  <DropdownMenuSeparator />
                  <DropdownMenuItem onClick={() => {
                    const profilePath = isCustomer() ? '/customer/profile' : isDriver() ? '/driver/profile' : '/admin/settings';
                    navigate(profilePath);
                  }}>
                    <User className="mr-2 h-4 w-4" />
                    Profile
                  </DropdownMenuItem>
                  <DropdownMenuItem onClick={() => navigate(isAdmin() ? '/admin/settings' : '/customer/profile')}>
                    <Settings className="mr-2 h-4 w-4" />
                    Settings
                  </DropdownMenuItem>
                  <DropdownMenuSeparator />
                  <DropdownMenuItem onClick={handleLogout} className="text-destructive focus:text-destructive">
                    <LogOut className="mr-2 h-4 w-4" />
                    Log out
                  </DropdownMenuItem>
                </DropdownMenuContent>
              </DropdownMenu>
            </div>
          </div>
        </div>
      </div>
    </header>
  );
};

// Legacy exports for backward compatibility
export const CustomerNavbar = AppNavbar;
export const DriverNavbar = AppNavbar;
export const AdminNavbar = AppNavbar;
