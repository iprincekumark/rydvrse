/**
 * Driver Dashboard Page
 * Main dashboard for drivers to manage availability and view earnings
 */

import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Power,
  TrendingUp,
  Star,
  Car,
  AlertCircle,
  Loader2,
  IndianRupee,
} from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar';
import { Badge } from '@/components/ui/badge';
import { useAuthStore, useDriverStore } from '@/store';
import { useDriver, useLocation } from '@/hooks';
import { MapContainer } from '@/components/maps';
import { formatCurrency } from '@/utils/formatters';
import { DriverStatus } from '@/types';

export const DriverDashboardPage = () => {
  const navigate = useNavigate();
  const { user } = useAuthStore();
  const { driver, isOnline, setIsOnline } = useDriverStore();
  const { getProfile, toggleAvailability, updateLocation } = useDriver();
  const { location, getCurrentPosition, startWatching, stopWatching } = useLocation({ watch: true });

  const [isLoading, setIsLoading] = useState(true);

  // Load driver profile
  useEffect(() => {
    const loadProfile = async () => {
      setIsLoading(true);
      await getProfile();
      setIsLoading(false);
    };

    loadProfile();
  }, [getProfile]);

  // Get current location
  useEffect(() => {
    getCurrentPosition();
    if (isOnline) {
      startWatching();
    }
    return () => stopWatching();
  }, [getCurrentPosition, startWatching, stopWatching, isOnline]);

  // Update driver location periodically
  useEffect(() => {
    if (isOnline && location && driver) {
      updateLocation(location.latitude, location.longitude);
    }
  }, [location, isOnline, driver, updateLocation]);

  const handleToggleOnline = async () => {
    const newStatus = !isOnline;
    const success = await toggleAvailability(newStatus);
    if (success) {
      setIsOnline(newStatus);
    }
  };

  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <Loader2 className="w-8 h-8 animate-spin text-primary" />
      </div>
    );
  }

  // Check if driver is approved
  const isApproved = driver?.status === DriverStatus.APPROVED || driver?.status === DriverStatus.ACTIVE;

  if (!isApproved) {
    return (
      <div className="min-h-screen p-4">
        <Card className="max-w-md mx-auto mt-8">
          <CardContent className="p-6 text-center">
            <AlertCircle className="w-16 h-16 text-yellow-500 mx-auto mb-4" />
            <h2 className="text-xl font-semibold mb-2">Account Under Review</h2>
            <p className="text-muted-foreground mb-4">
              Your driver account is currently being reviewed. We will notify you once it is approved.
            </p>
            <Button onClick={() => navigate('/driver/documents')}>
              View Documents
            </Button>
          </CardContent>
        </Card>
      </div>
    );
  }

  const stats = [
    {
      label: 'Today\'s Earnings',
      value: formatCurrency(driver?.totalEarnings || 0),
      icon: IndianRupee,
      color: 'bg-green-500',
    },
    {
      label: 'Total Trips',
      value: driver?.totalTrips || 0,
      icon: Car,
      color: 'bg-blue-500',
    },
    {
      label: 'Rating',
      value: driver?.rating ? driver.rating.toFixed(1) : 'N/A',
      icon: Star,
      color: 'bg-yellow-500',
    },
  ];

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-4">
          <Avatar className="h-14 w-14">
            <AvatarImage src={user?.profileImageUrl} alt={user?.firstName} />
            <AvatarFallback>
              {user?.firstName?.[0]}
              {user?.lastName?.[0]}
            </AvatarFallback>
          </Avatar>
          <div>
            <h1 className="text-xl font-bold">
              Hello, {user?.firstName || 'Driver'}!
            </h1>
            <p className="text-sm text-muted-foreground">
              {isOnline ? 'You are online and ready for trips' : 'You are currently offline'}
            </p>
          </div>
        </div>
        <Badge variant={isOnline ? 'default' : 'secondary'}>
          {isOnline ? 'Online' : 'Offline'}
        </Badge>
      </div>

      {/* Online Toggle */}
      <Card className={`${isOnline ? 'border-green-500' : ''}`}>
        <CardContent className="p-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-4">
              <div className={`w-12 h-12 rounded-full flex items-center justify-center ${isOnline ? 'bg-green-500' : 'bg-gray-400'}`}>
                <Power className="w-6 h-6 text-white" />
              </div>
              <div>
                <p className="font-medium">Go {isOnline ? 'Offline' : 'Online'}</p>
                <p className="text-sm text-muted-foreground">
                  {isOnline ? 'Stop receiving trip requests' : 'Start receiving trip requests'}
                </p>
              </div>
            </div>
            <Button
              variant={isOnline ? 'outline' : 'default'}
              size="lg"
              onClick={handleToggleOnline}
            >
              {isOnline ? 'Go Offline' : 'Go Online'}
            </Button>
          </div>
        </CardContent>
      </Card>

      {/* Stats */}
      <div className="grid grid-cols-3 gap-4">
        {stats.map((stat) => (
          <Card key={stat.label}>
            <CardContent className="p-4 text-center">
              <div className={`w-10 h-10 rounded-full ${stat.color} flex items-center justify-center mx-auto mb-2`}>
                <stat.icon className="w-5 h-5 text-white" />
              </div>
              <p className="text-lg font-bold">{stat.value}</p>
              <p className="text-xs text-muted-foreground">{stat.label}</p>
            </CardContent>
          </Card>
        ))}
      </div>

      {/* Map */}
      <Card>
        <CardHeader>
          <CardTitle className="text-lg">Your Location</CardTitle>
        </CardHeader>
        <CardContent>
          <MapContainer
            userLocation={location}
            height="250px"
          />
        </CardContent>
      </Card>

      {/* Quick Actions */}
      <div className="grid grid-cols-2 gap-4">
        <Button
          variant="outline"
          className="h-auto py-4 flex flex-col items-center gap-2"
          onClick={() => navigate('/driver/trips')}
        >
          <Car className="w-6 h-6" />
          <span>My Trips</span>
        </Button>
        <Button
          variant="outline"
          className="h-auto py-4 flex flex-col items-center gap-2"
          onClick={() => navigate('/driver/earnings')}
        >
          <TrendingUp className="w-6 h-6" />
          <span>Earnings</span>
        </Button>
      </div>
    </div>
  );
};

export default DriverDashboardPage;
