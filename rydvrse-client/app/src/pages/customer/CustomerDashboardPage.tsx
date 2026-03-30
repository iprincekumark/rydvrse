/**
 * Customer Dashboard Page
 * Main dashboard for customers to book rides and view activity
 */

import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  MapPin,
  Clock,
  Car,
  ChevronRight,
  Plus,
} from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar';
import { Badge } from '@/components/ui/badge';
import { useAuthStore, useTripStore, useCustomerStore } from '@/store';
import { useTrip, useLocation } from '@/hooks';
import { MapContainer } from '@/components/maps';
import { formatCurrency, formatDateTime, formatStatus } from '@/utils/formatters';

const COMPLETED_STATUSES = ['TRIP_COMPLETED', 'CANCELLED'];

export const CustomerDashboardPage = () => {
  const navigate = useNavigate();
  const { user } = useAuthStore();
  const { activeTrip, tripHistory } = useTripStore();
  const { defaultVehicle } = useCustomerStore();
  const { getCurrentPosition, location } = useLocation();
  const { getCustomerTrips } = useTrip();

  // Load data on mount
  useEffect(() => {
    getCurrentPosition();
    if (user?.id) {
      getCustomerTrips(user.id, 0, 5);
    }
  }, [user, getCurrentPosition, getCustomerTrips]);

  const hasActiveTrip = activeTrip && !COMPLETED_STATUSES.includes(activeTrip.status);

  const quickActions = [
    {
      label: 'Book a Ride',
      icon: Car,
      onClick: () => navigate('/customer/book'),
      color: 'bg-primary',
    },
    {
      label: 'My Trips',
      icon: Clock,
      onClick: () => navigate('/customer/trips'),
      color: 'bg-blue-500',
    },
    {
      label: 'Add Vehicle',
      icon: Plus,
      onClick: () => navigate('/customer/vehicles/add'),
      color: 'bg-green-500',
    },
  ];

  return (
    <div className="space-y-6">
      {/* Welcome Section */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold">
            Welcome back, {user?.firstName || 'Guest'}!
          </h1>
          <p className="text-muted-foreground">
            Ready to book your next ride?
          </p>
        </div>
        <Avatar className="h-12 w-12">
          <AvatarImage src={user?.profileImageUrl} alt={user?.firstName} />
          <AvatarFallback>
            {user?.firstName?.[0]}
            {user?.lastName?.[0]}
          </AvatarFallback>
        </Avatar>
      </div>

      {/* Active Trip Card */}
      {hasActiveTrip && (
        <Card className="border-primary">
          <CardHeader className="pb-3">
            <div className="flex items-center justify-between">
              <CardTitle className="text-lg flex items-center gap-2">
                <Car className="w-5 h-5 text-primary" />
                Active Trip
              </CardTitle>
              <Badge variant="default">
                {formatStatus(activeTrip.status)}
              </Badge>
            </div>
          </CardHeader>
          <CardContent>
            <div className="space-y-3">
              <div className="flex items-start gap-3">
                <MapPin className="w-5 h-5 text-green-500 mt-0.5" />
                <div>
                  <p className="font-medium">Pickup</p>
                  <p className="text-sm text-muted-foreground">
                    {activeTrip.pickupLocation.address || 'Current Location'}
                  </p>
                </div>
              </div>
              <div className="flex items-start gap-3">
                <MapPin className="w-5 h-5 text-red-500 mt-0.5" />
                <div>
                  <p className="font-medium">Drop</p>
                  <p className="text-sm text-muted-foreground">
                    {activeTrip.dropLocation.address || 'Destination'}
                  </p>
                </div>
              </div>
              {activeTrip.estimatedFare && (
                <div className="flex items-center justify-between pt-2 border-t">
                  <span className="text-muted-foreground">Estimated Fare</span>
                  <span className="font-semibold">
                    {formatCurrency(activeTrip.estimatedFare)}
                  </span>
                </div>
              )}
              <Button
                className="w-full"
                onClick={() => navigate(`/customer/trips/${activeTrip.id}`)}
              >
                Track Ride
              </Button>
            </div>
          </CardContent>
        </Card>
      )}

      {/* Quick Actions */}
      <div className="grid grid-cols-3 gap-4">
        {quickActions.map((action) => (
          <button
            key={action.label}
            onClick={action.onClick}
            className="flex flex-col items-center gap-2 p-4 rounded-xl bg-card border hover:border-primary transition-colors"
          >
            <div className={`w-12 h-12 rounded-full ${action.color} flex items-center justify-center`}>
              <action.icon className="w-6 h-6 text-white" />
            </div>
            <span className="text-sm font-medium">{action.label}</span>
          </button>
        ))}
      </div>

      {/* Map Section */}
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

      {/* Default Vehicle */}
      {defaultVehicle && (
        <Card>
          <CardHeader className="pb-3">
            <div className="flex items-center justify-between">
              <CardTitle className="text-lg flex items-center gap-2">
                <Car className="w-5 h-5" />
                Default Vehicle
              </CardTitle>
              <Button
                variant="ghost"
                size="sm"
                onClick={() => navigate('/customer/vehicles')}
              >
                Manage
                <ChevronRight className="w-4 h-4 ml-1" />
              </Button>
            </div>
          </CardHeader>
          <CardContent>
            <div className="flex items-center gap-4">
              <div className="w-12 h-12 rounded-full bg-muted flex items-center justify-center">
                <Car className="w-6 h-6" />
              </div>
              <div>
                <p className="font-medium">
                  {defaultVehicle.make} {defaultVehicle.model}
                </p>
                <p className="text-sm text-muted-foreground">
                  {defaultVehicle.color} • {defaultVehicle.registrationNumber}
                </p>
              </div>
            </div>
          </CardContent>
        </Card>
      )}

      {/* Recent Trips */}
      <Card>
        <CardHeader className="pb-3">
          <div className="flex items-center justify-between">
            <CardTitle className="text-lg flex items-center gap-2">
              <Clock className="w-5 h-5" />
              Recent Trips
            </CardTitle>
            <Button
              variant="ghost"
              size="sm"
              onClick={() => navigate('/customer/trips')}
            >
              View All
              <ChevronRight className="w-4 h-4 ml-1" />
            </Button>
          </div>
        </CardHeader>
        <CardContent>
          {tripHistory.length === 0 ? (
            <div className="text-center py-8">
              <Car className="w-12 h-12 mx-auto text-muted-foreground mb-3" />
              <p className="text-muted-foreground">No trips yet</p>
              <Button
                variant="outline"
                className="mt-4"
                onClick={() => navigate('/customer/book')}
              >
                Book Your First Ride
              </Button>
            </div>
          ) : (
            <div className="space-y-4">
              {tripHistory.slice(0, 3).map((trip) => (
                <div
                  key={trip.id}
                  className="flex items-center justify-between p-3 rounded-lg bg-muted/50 cursor-pointer hover:bg-muted transition-colors"
                  onClick={() => navigate(`/customer/trips/${trip.id}`)}
                >
                  <div className="flex items-center gap-3">
                    <div className="w-10 h-10 rounded-full bg-primary/10 flex items-center justify-center">
                      <Car className="w-5 h-5 text-primary" />
                    </div>
                    <div>
                      <p className="font-medium text-sm">
                        {trip.pickupLocation.city || 'Pickup'} →{' '}
                        {trip.dropLocation.city || 'Drop'}
                      </p>
                      <p className="text-xs text-muted-foreground">
                        {formatDateTime(trip.createdAt)}
                      </p>
                    </div>
                  </div>
                  <div className="text-right">
                    <p className="font-medium">
                      {trip.finalFare
                        ? formatCurrency(trip.finalFare)
                        : formatCurrency(trip.estimatedFare || 0)}
                    </p>
                    <Badge variant={trip.status === 'TRIP_COMPLETED' ? 'default' : 'secondary'} className="text-xs">
                      {formatStatus(trip.status)}
                    </Badge>
                  </div>
                </div>
              ))}
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
};

export default CustomerDashboardPage;
