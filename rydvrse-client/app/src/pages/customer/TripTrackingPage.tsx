/**
 * Trip Tracking Page — Real-time tracking with premium glassmorphism UI
 */

import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import {
  MapPin, Phone, MessageSquare, Shield, Star, ArrowLeft,
  Loader2,
} from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Avatar, AvatarFallback } from '@/components/ui/avatar';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription } from '@/components/ui/dialog';
import { useTripStore } from '@/store';
import { useTrip, useWebSocket } from '@/hooks';
import { MapContainer } from '@/components/maps';
import { formatCurrency, formatStatus } from '@/utils/formatters';
import { GlowingBorder } from '@/components/aceternity/glowing-effect';
import type { DriverLocation } from '@/types';

const CANCELLABLE_STATUSES = ['REQUESTED', 'DRIVER_MATCHING', 'DRIVER_ASSIGNED', 'DRIVER_ARRIVING'];

const getStatusMessage = (status: string): string => {
  switch (status) {
    case 'REQUESTED': return 'Finding a driver for you...';
    case 'DRIVER_MATCHING': return 'Matching you with nearby drivers...';
    case 'DRIVER_ASSIGNED': return 'Driver assigned! Waiting for confirmation...';
    case 'DRIVER_ARRIVING': return 'Driver is on the way to pickup';
    case 'DRIVER_ARRIVED': return 'Driver has arrived at pickup';
    case 'TRIP_STARTED': return 'Trip in progress — enjoy your ride!';
    case 'TRIP_COMPLETED': return 'Trip completed!';
    case 'CANCELLED': return 'Trip cancelled';
    default: return 'Processing...';
  }
};

const isActiveStatus = (status: string) =>
  ['REQUESTED', 'DRIVER_MATCHING', 'DRIVER_ASSIGNED', 'DRIVER_ARRIVING', 'DRIVER_ARRIVED', 'TRIP_STARTED'].includes(status);

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

  useEffect(() => {
    const loadTrip = async () => {
      if (!tripId) return;
      setIsLoading(true);
      const trip = await getTrip(tripId);
      if (trip) setActiveTrip(trip);
      setIsLoading(false);
    };
    loadTrip();
  }, [tripId, getTrip, setActiveTrip]);

  useEffect(() => {
    if (tripId && isConnected) {
      subscribeToTripLocation(tripId, (location: DriverLocation) => setDriverLocation(location));
    }
  }, [tripId, isConnected, subscribeToTripLocation, setDriverLocation]);

  const handleCancelTrip = async () => {
    if (!tripId) return;
    const success = await cancelTrip('Cancelled by customer');
    if (success) { setShowCancelDialog(false); navigate('/customer/trips'); }
  };

  const handleRateTrip = async () => {
    if (!tripId || rating === 0) return;
    setShowRatingDialog(false);
    navigate('/customer/trips');
  };

  if (isLoading) {
    return (
      <div className="min-h-[60vh] flex items-center justify-center">
        <Loader2 className="w-8 h-8 animate-spin text-primary" />
      </div>
    );
  }

  if (!activeTrip) {
    return (
      <div className="min-h-[60vh] flex flex-col items-center justify-center p-4">
        <div className="w-16 h-16 rounded-2xl bg-white/5 flex items-center justify-center mb-4">
          <MapPin className="w-8 h-8 text-muted-foreground" />
        </div>
        <h2 className="text-xl font-semibold mb-2">Trip not found</h2>
        <p className="text-muted-foreground text-sm mb-6">The trip you're looking for doesn't exist.</p>
        <Button onClick={() => navigate('/customer/dashboard')} className="gradient-primary text-white border-0">
          Go to Dashboard
        </Button>
      </div>
    );
  }

  const showCancelButton = CANCELLABLE_STATUSES.includes(activeTrip.status);
  const showRating = activeTrip.status === 'TRIP_COMPLETED' && !activeTrip.rating;

  return (
    <div className="min-h-screen bg-background -mx-4 -mt-6">
      {/* Map Section */}
      <div className="relative h-[45vh]">
        <MapContainer
          pickup={activeTrip.pickupLocation}
          drop={activeTrip.dropLocation}
          height="100%"
        />

        {/* Overlay Controls */}
        <div className="absolute top-4 left-4 z-10">
          <Button
            size="icon"
            className="glass rounded-full h-10 w-10"
            onClick={() => navigate('/customer/dashboard')}
          >
            <ArrowLeft className="w-4 h-4" />
          </Button>
        </div>
        <div className="absolute top-4 right-4 z-10">
          <Button
            size="sm"
            className="bg-red-500/90 hover:bg-red-600 text-white border-0 rounded-full"
            onClick={() => setShowSosDialog(true)}
          >
            <Shield className="w-3.5 h-3.5 mr-1" />
            SOS
          </Button>
        </div>
      </div>

      {/* Trip Details Panel */}
      <div className="px-4 -mt-8 relative z-10 pb-8">
        <motion.div
          initial={{ y: 30, opacity: 0 }}
          animate={{ y: 0, opacity: 1 }}
          transition={{ delay: 0.1 }}
        >
          <GlowingBorder active={isActiveStatus(activeTrip.status)} className="rounded-2xl">
            <div className="glass-card p-5 space-y-5">
              {/* Status Banner */}
              <div className="flex items-center justify-between">
                <Badge className={
                  isActiveStatus(activeTrip.status)
                    ? 'bg-primary/20 text-primary border-primary/30'
                    : activeTrip.status === 'TRIP_COMPLETED'
                    ? 'bg-emerald-500/20 text-emerald-400 border-emerald-500/30'
                    : 'bg-white/5 text-muted-foreground border-white/10'
                }>
                  {isActiveStatus(activeTrip.status) && (
                    <div className="w-1.5 h-1.5 rounded-full bg-primary animate-pulse mr-1.5" />
                  )}
                  {formatStatus(activeTrip.status)}
                </Badge>
                <span className="text-xs text-muted-foreground font-mono">{activeTrip.tripNumber}</span>
              </div>

              <p className="text-sm text-center text-muted-foreground">
                {getStatusMessage(activeTrip.status)}
              </p>

              {/* Driver Info */}
              {activeTrip.driverId && (
                <div className="flex items-center gap-4 p-3 rounded-xl bg-white/[0.03]">
                  <Avatar className="h-12 w-12 border border-white/10">
                    <AvatarFallback className="bg-primary/20 text-primary font-semibold">D</AvatarFallback>
                  </Avatar>
                  <div className="flex-1">
                    <p className="font-medium text-sm">Your Driver</p>
                    <div className="flex items-center gap-1 text-muted-foreground text-xs">
                      <Star className="w-3 h-3 fill-amber-400 text-amber-400" />
                      <span>4.8</span>
                    </div>
                  </div>
                  <div className="flex gap-2">
                    <Button variant="ghost" size="icon" className="rounded-full glass h-9 w-9">
                      <Phone className="w-3.5 h-3.5" />
                    </Button>
                    <Button variant="ghost" size="icon" className="rounded-full glass h-9 w-9">
                      <MessageSquare className="w-3.5 h-3.5" />
                    </Button>
                  </div>
                </div>
              )}

              {/* Route */}
              <div className="space-y-3">
                <div className="flex items-start gap-3">
                  <div className="w-3 h-3 rounded-full bg-green-400 mt-1.5 ring-4 ring-green-400/20" />
                  <div>
                    <p className="text-[10px] text-muted-foreground uppercase tracking-wider">Pickup</p>
                    <p className="font-medium text-sm">{activeTrip.pickupLocation.address}</p>
                  </div>
                </div>
                <div className="ml-1.5 border-l border-dashed border-white/20 h-4" />
                <div className="flex items-start gap-3">
                  <div className="w-3 h-3 rounded-full bg-primary mt-1.5 ring-4 ring-primary/20" />
                  <div>
                    <p className="text-[10px] text-muted-foreground uppercase tracking-wider">Drop-off</p>
                    <p className="font-medium text-sm">{activeTrip.dropLocation.address}</p>
                  </div>
                </div>
              </div>

              {/* Fare */}
              <div className="flex items-center justify-between pt-3 border-t border-white/10">
                <span className="text-sm text-muted-foreground">Estimated Fare</span>
                <span className="text-xl font-bold gradient-text">
                  {formatCurrency(activeTrip.estimatedFare || 0)}
                </span>
              </div>

              {/* Actions */}
              {showCancelButton && (
                <Button
                  variant="ghost"
                  className="w-full text-destructive hover:text-destructive hover:bg-destructive/10"
                  onClick={() => setShowCancelDialog(true)}
                >
                  Cancel Trip
                </Button>
              )}

              {showRating && (
                <Button
                  className="w-full gradient-primary text-white border-0 shadow-glow"
                  onClick={() => setShowRatingDialog(true)}
                >
                  <Star className="w-4 h-4 mr-2" />
                  Rate Trip
                </Button>
              )}
            </div>
          </GlowingBorder>
        </motion.div>
      </div>

      {/* Cancel Dialog */}
      <Dialog open={showCancelDialog} onOpenChange={setShowCancelDialog}>
        <DialogContent className="glass-card border-white/10">
          <DialogHeader>
            <DialogTitle>Cancel Trip?</DialogTitle>
            <DialogDescription>
              Are you sure? Cancellation fees may apply.
            </DialogDescription>
          </DialogHeader>
          <div className="flex gap-3 mt-4">
            <Button variant="ghost" className="flex-1" onClick={() => setShowCancelDialog(false)}>
              Keep Trip
            </Button>
            <Button variant="destructive" className="flex-1" onClick={handleCancelTrip}>
              Cancel Trip
            </Button>
          </div>
        </DialogContent>
      </Dialog>

      {/* SOS Dialog */}
      <Dialog open={showSosDialog} onOpenChange={setShowSosDialog}>
        <DialogContent className="glass-card border-white/10">
          <DialogHeader>
            <DialogTitle className="text-destructive flex items-center gap-2">
              <Shield className="w-5 h-5" />
              Emergency
            </DialogTitle>
            <DialogDescription>
              This will alert our safety team and your emergency contacts.
            </DialogDescription>
          </DialogHeader>
          <div className="space-y-3 mt-4">
            <Button variant="destructive" className="w-full" onClick={() => setShowSosDialog(false)}>
              <Phone className="w-4 h-4 mr-2" />
              Call Emergency
            </Button>
            <Button variant="ghost" className="w-full" onClick={() => setShowSosDialog(false)}>
              Cancel
            </Button>
          </div>
        </DialogContent>
      </Dialog>

      {/* Rating Dialog */}
      <Dialog open={showRatingDialog} onOpenChange={setShowRatingDialog}>
        <DialogContent className="glass-card border-white/10">
          <DialogHeader>
            <DialogTitle>Rate Your Trip</DialogTitle>
            <DialogDescription>How was your experience?</DialogDescription>
          </DialogHeader>
          <div className="space-y-4 mt-4">
            <div className="flex justify-center gap-2">
              {[1, 2, 3, 4, 5].map((star) => (
                <motion.button
                  key={star}
                  onClick={() => setRating(star)}
                  whileHover={{ scale: 1.2 }}
                  whileTap={{ scale: 0.9 }}
                  className="p-1"
                >
                  <Star
                    className={`w-8 h-8 transition-colors ${
                      star <= rating ? 'fill-amber-400 text-amber-400' : 'text-white/20'
                    }`}
                  />
                </motion.button>
              ))}
            </div>
            <textarea
              placeholder="Share your feedback (optional)"
              value={feedback}
              onChange={(e) => setFeedback(e.target.value)}
              className="w-full p-3 rounded-xl bg-white/5 border border-white/10 text-foreground placeholder:text-muted-foreground/50 resize-none focus:border-primary/50 focus:outline-none focus:ring-1 focus:ring-primary/20"
              rows={3}
            />
            <Button
              className="w-full gradient-primary text-white border-0"
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
