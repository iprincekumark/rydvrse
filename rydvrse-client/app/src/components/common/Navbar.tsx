/**
 * Navbar Components
 * Navigation bars for different user roles
 */

import { useState } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import {
  Car,
  User,
  History,
  MapPin,
  Menu,
  LogOut,
  Wallet,
  Settings,
  Bell,
  Home,
  TrendingUp,
  Users,
  Shield,
  CheckCircle,
} from 'lucide-react';
import { Button } from '@/components/ui/button';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar';
import { Badge } from '@/components/ui/badge';
import { Sheet, SheetContent, SheetTrigger } from '@/components/ui/sheet';
import { useAuth } from '@/hooks';
import { useAuthStore } from '@/store';

// ============================================
// Customer Navbar
// ============================================
export const CustomerNavbar = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { logout } = useAuth();
  const { user } = useAuthStore();
  const [isOpen, setIsOpen] = useState(false);

  const navItems = [
    { label: 'Dashboard', href: '/customer/dashboard', icon: Home },
    { label: 'Book Ride', href: '/customer/book', icon: MapPin },
    { label: 'My Trips', href: '/customer/trips', icon: History },
    { label: 'Vehicles', href: '/customer/vehicles', icon: Car },
    { label: 'Wallet', href: '/customer/wallet', icon: Wallet },
  ];

  const isActive = (path: string) => location.pathname === path;

  return (
    <nav className="border-b bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60">
      <div className="container mx-auto px-4">
        <div className="flex h-16 items-center justify-between">
          {/* Logo */}
          <Link to="/customer/dashboard" className="flex items-center gap-2">
            <div className="w-8 h-8 rounded-full bg-primary flex items-center justify-center">
              <Car className="w-4 h-4 text-primary-foreground" />
            </div>
            <span className="text-xl font-bold">RYDVRSE</span>
          </Link>

          {/* Desktop Navigation */}
          <div className="hidden md:flex items-center gap-1">
            {navItems.map((item) => (
              <Button
                key={item.href}
                variant={isActive(item.href) ? 'default' : 'ghost'}
                size="sm"
                onClick={() => navigate(item.href)}
                className="gap-2"
              >
                <item.icon className="w-4 h-4" />
                {item.label}
              </Button>
            ))}
          </div>

          {/* Right Section */}
          <div className="flex items-center gap-2">
            {/* Notifications */}
            <Button variant="ghost" size="icon" className="relative">
              <Bell className="w-5 h-5" />
              <Badge className="absolute -top-1 -right-1 h-5 w-5 flex items-center justify-center p-0 text-xs">
                3
              </Badge>
            </Button>

            {/* Profile Dropdown */}
            <DropdownMenu>
              <DropdownMenuTrigger asChild>
                <Button variant="ghost" className="relative h-8 w-8 rounded-full">
                  <Avatar className="h-8 w-8">
                    <AvatarImage src={user?.profileImageUrl} alt={user?.firstName} />
                    <AvatarFallback>
                      {user?.firstName?.[0]}
                      {user?.lastName?.[0]}
                    </AvatarFallback>
                  </Avatar>
                </Button>
              </DropdownMenuTrigger>
              <DropdownMenuContent className="w-56" align="end" forceMount>
                <div className="flex items-center gap-2 p-2">
                  <Avatar className="h-8 w-8">
                    <AvatarImage src={user?.profileImageUrl} alt={user?.firstName} />
                    <AvatarFallback>
                      {user?.firstName?.[0]}
                      {user?.lastName?.[0]}
                    </AvatarFallback>
                  </Avatar>
                  <div className="flex flex-col">
                    <p className="text-sm font-medium">
                      {user?.firstName} {user?.lastName}
                    </p>
                    <p className="text-xs text-muted-foreground">{user?.phoneNumber}</p>
                  </div>
                </div>
                <DropdownMenuSeparator />
                <DropdownMenuItem onClick={() => navigate('/customer/profile')}>
                  <User className="mr-2 h-4 w-4" />
                  Profile
                </DropdownMenuItem>
                <DropdownMenuItem onClick={() => navigate('/customer/settings')}>
                  <Settings className="mr-2 h-4 w-4" />
                  Settings
                </DropdownMenuItem>
                <DropdownMenuSeparator />
                <DropdownMenuItem onClick={logout} className="text-destructive">
                  <LogOut className="mr-2 h-4 w-4" />
                  Logout
                </DropdownMenuItem>
              </DropdownMenuContent>
            </DropdownMenu>

            {/* Mobile Menu */}
            <Sheet open={isOpen} onOpenChange={setIsOpen}>
              <SheetTrigger asChild className="md:hidden">
                <Button variant="ghost" size="icon">
                  <Menu className="h-5 w-5" />
                </Button>
              </SheetTrigger>
              <SheetContent side="right" className="w-64">
                <div className="flex flex-col gap-2 mt-4">
                  {navItems.map((item) => (
                    <Button
                      key={item.href}
                      variant={isActive(item.href) ? 'default' : 'ghost'}
                      onClick={() => {
                        navigate(item.href);
                        setIsOpen(false);
                      }}
                      className="justify-start gap-2"
                    >
                      <item.icon className="w-4 h-4" />
                      {item.label}
                    </Button>
                  ))}
                </div>
              </SheetContent>
            </Sheet>
          </div>
        </div>
      </div>
    </nav>
  );
};

// ============================================
// Driver Navbar
// ============================================
export const DriverNavbar = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { logout } = useAuth();
  const { user } = useAuthStore();
  const [isOpen, setIsOpen] = useState(false);

  const navItems = [
    { label: 'Dashboard', href: '/driver/dashboard', icon: Home },
    { label: 'My Trips', href: '/driver/trips', icon: History },
    { label: 'Earnings', href: '/driver/earnings', icon: TrendingUp },
    { label: 'Documents', href: '/driver/documents', icon: CheckCircle },
  ];

  const isActive = (path: string) => location.pathname === path;

  return (
    <nav className="border-b bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60">
      <div className="container mx-auto px-4">
        <div className="flex h-16 items-center justify-between">
          {/* Logo */}
          <Link to="/driver/dashboard" className="flex items-center gap-2">
            <div className="w-8 h-8 rounded-full bg-primary flex items-center justify-center">
              <Car className="w-4 h-4 text-primary-foreground" />
            </div>
            <span className="text-xl font-bold">RYDVRSE</span>
          </Link>

          {/* Desktop Navigation */}
          <div className="hidden md:flex items-center gap-1">
            {navItems.map((item) => (
              <Button
                key={item.href}
                variant={isActive(item.href) ? 'default' : 'ghost'}
                size="sm"
                onClick={() => navigate(item.href)}
                className="gap-2"
              >
                <item.icon className="w-4 h-4" />
                {item.label}
              </Button>
            ))}
          </div>

          {/* Right Section */}
          <div className="flex items-center gap-2">
            {/* Notifications */}
            <Button variant="ghost" size="icon" className="relative">
              <Bell className="w-5 h-5" />
              <Badge className="absolute -top-1 -right-1 h-5 w-5 flex items-center justify-center p-0 text-xs">
                2
              </Badge>
            </Button>

            {/* Profile Dropdown */}
            <DropdownMenu>
              <DropdownMenuTrigger asChild>
                <Button variant="ghost" className="relative h-8 w-8 rounded-full">
                  <Avatar className="h-8 w-8">
                    <AvatarImage src={user?.profileImageUrl} alt={user?.firstName} />
                    <AvatarFallback>
                      {user?.firstName?.[0]}
                      {user?.lastName?.[0]}
                    </AvatarFallback>
                  </Avatar>
                </Button>
              </DropdownMenuTrigger>
              <DropdownMenuContent className="w-56" align="end" forceMount>
                <div className="flex items-center gap-2 p-2">
                  <Avatar className="h-8 w-8">
                    <AvatarImage src={user?.profileImageUrl} alt={user?.firstName} />
                    <AvatarFallback>
                      {user?.firstName?.[0]}
                      {user?.lastName?.[0]}
                    </AvatarFallback>
                  </Avatar>
                  <div className="flex flex-col">
                    <p className="text-sm font-medium">
                      {user?.firstName} {user?.lastName}
                    </p>
                    <p className="text-xs text-muted-foreground">{user?.phoneNumber}</p>
                  </div>
                </div>
                <DropdownMenuSeparator />
                <DropdownMenuItem onClick={() => navigate('/driver/profile')}>
                  <User className="mr-2 h-4 w-4" />
                  Profile
                </DropdownMenuItem>
                <DropdownMenuItem onClick={() => navigate('/driver/settings')}>
                  <Settings className="mr-2 h-4 w-4" />
                  Settings
                </DropdownMenuItem>
                <DropdownMenuSeparator />
                <DropdownMenuItem onClick={logout} className="text-destructive">
                  <LogOut className="mr-2 h-4 w-4" />
                  Logout
                </DropdownMenuItem>
              </DropdownMenuContent>
            </DropdownMenu>

            {/* Mobile Menu */}
            <Sheet open={isOpen} onOpenChange={setIsOpen}>
              <SheetTrigger asChild className="md:hidden">
                <Button variant="ghost" size="icon">
                  <Menu className="h-5 w-5" />
                </Button>
              </SheetTrigger>
              <SheetContent side="right" className="w-64">
                <div className="flex flex-col gap-2 mt-4">
                  {navItems.map((item) => (
                    <Button
                      key={item.href}
                      variant={isActive(item.href) ? 'default' : 'ghost'}
                      onClick={() => {
                        navigate(item.href);
                        setIsOpen(false);
                      }}
                      className="justify-start gap-2"
                    >
                      <item.icon className="w-4 h-4" />
                      {item.label}
                    </Button>
                  ))}
                </div>
              </SheetContent>
            </Sheet>
          </div>
        </div>
      </div>
    </nav>
  );
};

// ============================================
// Admin Navbar
// ============================================
export const AdminNavbar = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { logout } = useAuth();
  const { user } = useAuthStore();
  const [isOpen, setIsOpen] = useState(false);

  const navItems = [
    { label: 'Dashboard', href: '/admin/dashboard', icon: Home },
    { label: 'Drivers', href: '/admin/drivers', icon: Users },
    { label: 'Trips', href: '/admin/trips', icon: History },
    { label: 'Safety', href: '/admin/safety', icon: Shield },
  ];

  const isActive = (path: string) => location.pathname === path;

  return (
    <nav className="border-b bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60">
      <div className="container mx-auto px-4">
        <div className="flex h-16 items-center justify-between">
          {/* Logo */}
          <Link to="/admin/dashboard" className="flex items-center gap-2">
            <div className="w-8 h-8 rounded-full bg-primary flex items-center justify-center">
              <Car className="w-4 h-4 text-primary-foreground" />
            </div>
            <span className="text-xl font-bold">RYDVRSE Admin</span>
          </Link>

          {/* Desktop Navigation */}
          <div className="hidden md:flex items-center gap-1">
            {navItems.map((item) => (
              <Button
                key={item.href}
                variant={isActive(item.href) ? 'default' : 'ghost'}
                size="sm"
                onClick={() => navigate(item.href)}
                className="gap-2"
              >
                <item.icon className="w-4 h-4" />
                {item.label}
              </Button>
            ))}
          </div>

          {/* Right Section */}
          <div className="flex items-center gap-2">
            {/* Notifications */}
            <Button variant="ghost" size="icon" className="relative">
              <Bell className="w-5 h-5" />
              <Badge className="absolute -top-1 -right-1 h-5 w-5 flex items-center justify-center p-0 text-xs">
                5
              </Badge>
            </Button>

            {/* Profile Dropdown */}
            <DropdownMenu>
              <DropdownMenuTrigger asChild>
                <Button variant="ghost" className="relative h-8 w-8 rounded-full">
                  <Avatar className="h-8 w-8">
                    <AvatarImage src={user?.profileImageUrl} alt={user?.firstName} />
                    <AvatarFallback>
                      {user?.firstName?.[0]}
                      {user?.lastName?.[0]}
                    </AvatarFallback>
                  </Avatar>
                </Button>
              </DropdownMenuTrigger>
              <DropdownMenuContent className="w-56" align="end" forceMount>
                <div className="flex items-center gap-2 p-2">
                  <Avatar className="h-8 w-8">
                    <AvatarImage src={user?.profileImageUrl} alt={user?.firstName} />
                    <AvatarFallback>
                      {user?.firstName?.[0]}
                      {user?.lastName?.[0]}
                    </AvatarFallback>
                  </Avatar>
                  <div className="flex flex-col">
                    <p className="text-sm font-medium">
                      {user?.firstName} {user?.lastName}
                    </p>
                    <p className="text-xs text-muted-foreground">{user?.phoneNumber}</p>
                  </div>
                </div>
                <DropdownMenuSeparator />
                <DropdownMenuItem onClick={() => navigate('/admin/settings')}>
                  <Settings className="mr-2 h-4 w-4" />
                  Settings
                </DropdownMenuItem>
                <DropdownMenuSeparator />
                <DropdownMenuItem onClick={logout} className="text-destructive">
                  <LogOut className="mr-2 h-4 w-4" />
                  Logout
                </DropdownMenuItem>
              </DropdownMenuContent>
            </DropdownMenu>

            {/* Mobile Menu */}
            <Sheet open={isOpen} onOpenChange={setIsOpen}>
              <SheetTrigger asChild className="md:hidden">
                <Button variant="ghost" size="icon">
                  <Menu className="h-5 w-5" />
                </Button>
              </SheetTrigger>
              <SheetContent side="right" className="w-64">
                <div className="flex flex-col gap-2 mt-4">
                  {navItems.map((item) => (
                    <Button
                      key={item.href}
                      variant={isActive(item.href) ? 'default' : 'ghost'}
                      onClick={() => {
                        navigate(item.href);
                        setIsOpen(false);
                      }}
                      className="justify-start gap-2"
                    >
                      <item.icon className="w-4 h-4" />
                      {item.label}
                    </Button>
                  ))}
                </div>
              </SheetContent>
            </Sheet>
          </div>
        </div>
      </div>
    </nav>
  );
};

export default { CustomerNavbar, DriverNavbar, AdminNavbar };
