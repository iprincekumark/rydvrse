/**
 * Trip Tracking Page
 * Real-time tracking of active trip with driver location
 */

import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  MapPin,
  Phone,
  MessageSquare,
  Shield,
  Star,
  X,
  Loader2,
} from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Card, CardContent } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Avatar, AvatarFallback } from '@/components/ui/avatar';
import { Separator } from '@/components/ui/separator';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription } from '@/components/ui/dialog';
import { useTripStore } from '@/store';
import { useTrip, useWebSocket } from '@/hooks';
import { MapContainer } from '@/components/maps';
import { formatCurrency, formatStatus } from '@/utils/formatters';
import type { DriverLocation } from '@/types';

const CANCELLABLE_STATUSES = ['REQUESTED', 'DRIVER_MATCHING', 'DRIVER_ASSIGNED', 'DRIVER_ARRIVING'];

const getStatusMessage = (status: string): string => {
  switch (status) {
    case 'REQUESTED':
      return 'Finding a driver for you...';
    case 'DRIVER_MATCHING':
      return 'Matching you with nearby drivers...';
    case 'DRIVER_ASSIGNED':
      return 'Driver assigned! Waiting for confirmation...';
    case 'DRIVER_ARRIVING':
      return 'Driver is on the way to pickup location';
    case 'DRIVER_ARRIVED':
      return 'Driver has arrived at pickup location';
    case 'TRIP_STARTED':
      return 'Trip in progress. Enjoy your ride!';
    case 'TRIP_COMPLETED':
      return 'Trip completed. Thank you for riding with us!';
    case 'CANCELLED':
      return 'Trip cancelled';
    default:
      return 'Processing...';
  }
};

export const TripTrackingPage = () => {
  const { tripId } = useParams<{ tripId: string }>();
  const navigate = useNavigate();
  const { activeTrip, setActiveTrip, setDriverLocation } = useTripStore();
  const { getTrip, cancelTrip } = useTrip();
  const { subscribeToTripLocation, isConnected } = useWebSocket();

  const [showCancelDialog, setShowCancelDialog] = useState(false);
  const [showSosDialog, setShowSosDialog] = useState(false);
  const [rating, setRating] = useState(0);
  const [feedback, setFeedback] = useState('');
  const [showRatingDialog, setShowRatingDialog] = useState(false);
  const [isLoading, setIsLoading] = useState(true);

  // Load trip data
  useEffect(() => {
    const loadTrip = async () => {
      if (!tripId) return;
      setIsLoading(true);
      const trip = await getTrip(tripId);
      if (trip) {
        setActiveTrip(trip);
      }
      setIsLoading(false);
    };

    loadTrip();
  }, [tripId, getTrip, setActiveTrip]);

  // Subscribe to driver location updates
  useEffect(() => {
    if (tripId && isConnected) {
      subscribeToTripLocation(tripId, (location: DriverLocation) => {
        setDriverLocation(location);
      });
    }
  }, [tripId, isConnected, subscribeToTripLocation, setDriverLocation]);

  const handleCancelTrip = async () => {
    if (!tripId) return;
    const success = await cancelTrip('Cancelled by customer');
    if (success) {
      setShowCancelDialog(false);
      navigate('/customer/trips');
    }
  };

  const handleRateTrip = async () => {
    if (!tripId || rating === 0) return;
    // Rate trip logic here
    setShowRatingDialog(false);
    navigate('/customer/trips');
  };

  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <Loader2 className="w-8 h-8 animate-spin text-primary" />
      </div>
    );
  }

  if (!activeTrip) {
    return (
      <div className="min-h-screen flex flex-col items-center justify-center p-4">
        <MapPin className="w-16 h-16 text-muted-foreground mb-4" />
        <h2 className="text-xl font-semibold mb-2">Trip not found</h2>
        <p className="text-muted-foreground mb-4">The trip you are looking for does not exist.</p>
        <Button onClick={() => navigate('/customer/dashboard')}>
          Go to Dashboard
        </Button>
      </div>
    );
  }

  const showCancelButton = CANCELLABLE_STATUSES.includes(activeTrip.status);
  const showRating = activeTrip.status === 'TRIP_COMPLETED' && !activeTrip.rating;

  return (
    <div className="min-h-screen bg-background">
      {/* Map Section */}
      <div className="relative h-[50vh]">
        <MapContainer
          pickup={activeTrip.pickupLocation}
          drop={activeTrip.dropLocation}
          height="100%"
        />

        {/* Back Button */}
        <Button
          variant="secondary"
          size="icon"
          className="absolute top-4 left-4 z-10"
          onClick={() => navigate('/customer/dashboard')}
        >
          <X className="w-5 h-5" />
        </Button>

        {/* SOS Button */}
        <Button
          variant="destructive"
          size="sm"
          className="absolute top-4 right-4 z-10"
          onClick={() => setShowSosDialog(true)}
        >
          <Shield className="w-4 h-4 mr-1" />
          SOS
        </Button>
      </div>

      {/* Trip Details */}
      <div className="px-4 -mt-6 relative z-10">
        <Card className="shadow-lg">
          <CardContent className="p-4 space-y-4">
            {/* Status */}
            <div className="flex items-center justify-between">
              <Badge variant="default" className="text-sm">
                {formatStatus(activeTrip.status)}
              </Badge>
              <span className="text-sm text-muted-foreground">
                {activeTrip.tripNumber}
              </span>
            </div>

            <p className="text-sm text-center text-muted-foreground">
              {getStatusMessage(activeTrip.status)}
            </p>

            {/* Driver Info (if assigned) */}
            {activeTrip.driverId && (
              <>
                <Separator />
                <div className="flex items-center gap-4">
                  <Avatar className="h-12 w-12">
                    <AvatarFallback>D</AvatarFallback>
                  </Avatar>
                  <div className="flex-1">
                    <p className="font-medium">Your Driver</p>
                    <div className="flex items-center gap-1 text-sm text-muted-foreground">
                      <Star className="w-4 h-4 fill-yellow-400 text-yellow-400" />
                      <span>4.8</span>
                    </div>
                  </div>
                  <div className="flex gap-2">
                    <Button variant="outline" size="icon">
                      <Phone className="w-4 h-4" />
                    </Button>
                    <Button variant="outline" size="icon">
                      <MessageSquare className="w-4 h-4" />
                    </Button>
                  </div>
                </div>
              </>
            )}

            {/* Locations */}
            <Separator />
            <div className="space-y-3">
              <div className="flex items-start gap-3">
                <MapPin className="w-5 h-5 text-green-500 mt-0.5" />
                <div>
                  <p className="font-medium text-sm">Pickup</p>
                  <p className="text-sm text-muted-foreground">
                    {activeTrip.pickupLocation.address}
                  </p>
                </div>
              </div>
              <div className="flex items-start gap-3">
                <MapPin className="w-5 h-5 text-red-500 mt-0.5" />
                <div>
                  <p className="font-medium text-sm">Drop</p>
                  <p className="text-sm text-muted-foreground">
                    {activeTrip.dropLocation.address}
                  </p>
                </div>
              </div>
            </div>

            {/* Fare */}
            <Separator />
            <div className="flex items-center justify-between">
              <span className="text-muted-foreground">Estimated Fare</span>
              <span className="text-lg font-bold">
                {formatCurrency(activeTrip.estimatedFare || 0)}
              </span>
            </div>

            {/* Actions */}
            {showCancelButton && (
              <Button
                variant="outline"
                className="w-full text-destructive"
                onClick={() => setShowCancelDialog(true)}
              >
                Cancel Trip
              </Button>
            )}

            {showRating && (
              <Button
                className="w-full"
                onClick={() => setShowRatingDialog(true)}
              >
                Rate Trip
              </Button>
            )}
          </CardContent>
        </Card>
      </div>

      {/* Cancel Dialog */}
      <Dialog open={showCancelDialog} onOpenChange={setShowCancelDialog}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Cancel Trip?</DialogTitle>
            <DialogDescription>
              Are you sure you want to cancel this trip? Cancellation fees may apply.
            </DialogDescription>
          </DialogHeader>
          <div className="flex gap-3 mt-4">
            <Button
              variant="outline"
              className="flex-1"
              onClick={() => setShowCancelDialog(false)}
            >
              Keep Trip
            </Button>
            <Button
              variant="destructive"
              className="flex-1"
              onClick={handleCancelTrip}
            >
              Cancel Trip
            </Button>
          </div>
        </DialogContent>
      </Dialog>

      {/* SOS Dialog */}
      <Dialog open={showSosDialog} onOpenChange={setShowSosDialog}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle className="text-destructive flex items-center gap-2">
              <Shield className="w-5 h-5" />
              Emergency Assistance
            </DialogTitle>
            <DialogDescription>
              Are you in an emergency situation? This will alert our safety team and emergency contacts.
            </DialogDescription>
          </DialogHeader>
          <div className="space-y-3 mt-4">
            <Button
              variant="destructive"
              className="w-full"
              onClick={() => {
                // Trigger emergency protocol
                setShowSosDialog(false);
              }}
            >
              <Phone className="w-4 h-4 mr-2" />
              Call Emergency Services
            </Button>
            <Button
              variant="outline"
              className="w-full"
              onClick={() => setShowSosDialog(false)}
            >
              Cancel
            </Button>
          </div>
        </DialogContent>
      </Dialog>

      {/* Rating Dialog */}
      <Dialog open={showRatingDialog} onOpenChange={setShowRatingDialog}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Rate Your Trip</DialogTitle>
            <DialogDescription>
              How was your experience with the driver?
            </DialogDescription>
          </DialogHeader>
          <div className="space-y-4 mt-4">
            <div className="flex justify-center gap-2">
              {[1, 2, 3, 4, 5].map((star) => (
                <button
                  key={star}
                  onClick={() => setRating(star)}
                  className="p-1"
                >
                  <Star
                    className={`w-8 h-8 ${
                      star <= rating
                        ? 'fill-yellow-400 text-yellow-400'
                        : 'text-gray-300'
                    }`}
                  />
                </button>
              ))}
            </div>
            <textarea
              placeholder="Share your feedback (optional)"
              value={feedback}
              onChange={(e) => setFeedback(e.target.value)}
              className="w-full p-3 border rounded-lg resize-none"
              rows={3}
            />
            <Button
              className="w-full"
              onClick={handleRateTrip}
              disabled={rating === 0}
            >
              Submit Rating
            </Button>
          </div>
        </DialogContent>
      </Dialog>
    </div>
  );
};

export default TripTrackingPage;
