/**
 * Book Ride Page — Premium multi-step booking with animations
 */

import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import {
  MapPin, Car, Navigation, Search, Loader2, ArrowRight,
  ArrowLeft, Home, Briefcase, Clock, IndianRupee,
} from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Badge } from '@/components/ui/badge';
import { useAuthStore, useCustomerStore } from '@/store';
import { useTrip, useLocation } from '@/hooks';
import { MapContainer } from '@/components/maps';
import { formatCurrency, formatDistance, formatDuration } from '@/utils/formatters';
import { MovingBorder } from '@/components/aceternity/moving-border';
import type { Vehicle, GeoLocation } from '@/types';

const estimateFare = (distanceKm: number) => {
  const baseFare = 50;
  const perKmRate = 15;
  const fare = baseFare + distanceKm * perKmRate;
  const duration = Math.ceil(distanceKm * 2.5);
  return { fare: Math.round(fare), duration, distance: distanceKm };
};

const steps = ['pickup', 'drop', 'vehicle', 'confirm'] as const;
type Step = typeof steps[number];

const slideVariants = {
  enter: (direction: number) => ({ x: direction > 0 ? 200 : -200, opacity: 0 }),
  center: { x: 0, opacity: 1 },
  exit: (direction: number) => ({ x: direction < 0 ? 200 : -200, opacity: 0 }),
};

export const BookRidePage = () => {
  const navigate = useNavigate();
  const { user } = useAuthStore();
  const { vehicles, defaultVehicle, savedLocations } = useCustomerStore();
  const { createTrip, isLoading } = useTrip();
  const { location, getCurrentPosition } = useLocation();

  const [step, setStep] = useState<Step>('pickup');
  const [direction, setDirection] = useState(1);
  const [pickupLocation, setPickupLocation] = useState<GeoLocation | null>(null);
  const [dropLocation, setDropLocation] = useState<GeoLocation | null>(null);
  const [selectedVehicle, setSelectedVehicle] = useState<Vehicle | null>(defaultVehicle);
  const [fareEstimate, setFareEstimate] = useState<{ fare: number; duration: number; distance: number } | null>(null);
  const [searchQuery, setSearchQuery] = useState('');

  useEffect(() => { getCurrentPosition(); }, [getCurrentPosition]);

  useEffect(() => {
    if (location && !pickupLocation) {
      setPickupLocation({ latitude: location.latitude, longitude: location.longitude, address: 'Current Location' });
    }
  }, [location, pickupLocation]);

  useEffect(() => {
    if (pickupLocation && dropLocation) {
      const distance = Math.random() * 15 + 5;
      setFareEstimate(estimateFare(distance));
    }
  }, [pickupLocation, dropLocation]);

  const goTo = (newStep: Step) => {
    const dir = steps.indexOf(newStep) > steps.indexOf(step) ? 1 : -1;
    setDirection(dir);
    setStep(newStep);
  };

  const handleLocationSelect = (type: 'pickup' | 'drop', loc: GeoLocation) => {
    if (type === 'pickup') { setPickupLocation(loc); goTo('drop'); }
    else { setDropLocation(loc); goTo('vehicle'); }
  };

  const handleBookRide = async () => {
    if (!pickupLocation || !dropLocation || !selectedVehicle || !user) return;
    const trip = await createTrip({
      customerId: user.id, vehicleId: selectedVehicle.id,
      pickupLat: pickupLocation.latitude, pickupLng: pickupLocation.longitude,
      pickupAddress: pickupLocation.address, pickupCity: pickupLocation.city,
      dropLat: dropLocation.latitude, dropLng: dropLocation.longitude,
      dropAddress: dropLocation.address, dropCity: dropLocation.city,
      estimatedFare: fareEstimate?.fare,
    });
    if (trip) navigate(`/customer/trips/${trip.id}`);
  };

  const stepIndex = steps.indexOf(step);

  return (
    <div className="max-w-lg mx-auto space-y-6">
      {/* Header */}
      <div className="flex items-center gap-4">
        <Button
          variant="ghost"
          size="icon"
          className="rounded-full glass"
          onClick={() => {
            if (step === 'pickup') navigate('/customer/dashboard');
            else goTo(steps[stepIndex - 1]);
          }}
        >
          <ArrowLeft className="w-4 h-4" />
        </Button>
        <div>
          <h1 className="text-xl font-bold text-foreground">
            {step === 'pickup' && 'Select Pickup'}
            {step === 'drop' && 'Select Drop-off'}
            {step === 'vehicle' && 'Select Vehicle'}
            {step === 'confirm' && 'Confirm Booking'}
          </h1>
          <p className="text-xs text-muted-foreground">Step {stepIndex + 1} of 4</p>
        </div>
      </div>

      {/* Progress Bar */}
      <div className="flex gap-1.5">
        {steps.map((s, i) => (
          <motion.div
            key={s}
            className="flex-1 h-1.5 rounded-full overflow-hidden bg-white/5"
          >
            <motion.div
              className="h-full rounded-full gradient-primary"
              initial={false}
              animate={{ width: stepIndex >= i ? '100%' : '0%' }}
              transition={{ duration: 0.3 }}
            />
          </motion.div>
        ))}
      </div>

      {/* Step Content */}
      <AnimatePresence mode="wait" custom={direction}>
        <motion.div
          key={step}
          custom={direction}
          variants={slideVariants}
          initial="enter"
          animate="center"
          exit="exit"
          transition={{ type: "tween", duration: 0.25 }}
        >
          {/* Pickup / Drop Step */}
          {(step === 'pickup' || step === 'drop') && (
            <div className="space-y-4">
              <div className="relative group">
                <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground group-focus-within:text-primary transition-colors" />
                <Input
                  placeholder={`Search ${step === 'pickup' ? 'pickup' : 'drop-off'} location`}
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                  className="pl-10 h-12 bg-white/5 border-white/10 focus:border-primary/50"
                />
              </div>

              {step === 'pickup' && location && (
                <button
                  onClick={() => handleLocationSelect('pickup', {
                    latitude: location.latitude, longitude: location.longitude, address: 'Current Location'
                  })}
                  className="w-full glass-card p-4 flex items-center gap-3 hover:border-primary/30 transition-colors"
                >
                  <div className="w-10 h-10 rounded-xl bg-primary/20 flex items-center justify-center">
                    <Navigation className="w-5 h-5 text-primary" />
                  </div>
                  <div className="text-left">
                    <p className="font-medium text-sm">Current Location</p>
                    <p className="text-xs text-muted-foreground">Use GPS location</p>
                  </div>
                </button>
              )}

              {savedLocations.length > 0 && (
                <div className="space-y-2">
                  <p className="text-xs text-muted-foreground font-medium uppercase tracking-wider">Saved Places</p>
                  {savedLocations.map((savedLoc) => (
                    <button
                      key={savedLoc.id}
                      onClick={() => handleLocationSelect(step, savedLoc.location)}
                      className="w-full glass-card p-3 flex items-center gap-3 hover:border-white/20 transition-colors"
                    >
                      <div className="w-9 h-9 rounded-lg bg-white/5 flex items-center justify-center">
                        {savedLoc.type === 'HOME' ? <Home className="w-4 h-4 text-green-400" />
                          : savedLoc.type === 'WORK' ? <Briefcase className="w-4 h-4 text-blue-400" />
                          : <MapPin className="w-4 h-4 text-muted-foreground" />}
                      </div>
                      <div className="text-left flex-1 min-w-0">
                        <p className="font-medium text-sm">{savedLoc.name}</p>
                        <p className="text-xs text-muted-foreground truncate">{savedLoc.location.address}</p>
                      </div>
                    </button>
                  ))}
                </div>
              )}

              <div className="space-y-2">
                <p className="text-xs text-muted-foreground font-medium uppercase tracking-wider">Select on map</p>
                <div className="rounded-xl overflow-hidden border border-white/10">
                  <MapContainer
                    userLocation={location}
                    height="200px"
                    onMapClick={(lat, lng) => handleLocationSelect(step, {
                      latitude: lat, longitude: lng, address: `${lat.toFixed(4)}, ${lng.toFixed(4)}`
                    })}
                  />
                </div>
              </div>
            </div>
          )}

          {/* Vehicle Step */}
          {step === 'vehicle' && (
            <div className="space-y-4">
              <p className="text-xs text-muted-foreground font-medium uppercase tracking-wider">Your Vehicles</p>
              {vehicles.map((vehicle) => (
                <button
                  key={vehicle.id}
                  onClick={() => { setSelectedVehicle(vehicle); goTo('confirm'); }}
                  className={`w-full glass-card p-4 flex items-center gap-4 transition-all ${
                    selectedVehicle?.id === vehicle.id
                      ? 'border-primary/50 shadow-glow/20'
                      : 'hover:border-white/20'
                  }`}
                >
                  <div className="w-12 h-12 rounded-xl bg-white/5 flex items-center justify-center">
                    <Car className="w-6 h-6 text-primary" />
                  </div>
                  <div className="flex-1 text-left">
                    <p className="font-medium">{vehicle.make} {vehicle.model}</p>
                    <p className="text-xs text-muted-foreground">{vehicle.color} • {vehicle.registrationNumber}</p>
                  </div>
                  {vehicle.isDefault && (
                    <Badge className="bg-primary/20 text-primary border-primary/30 text-[10px]">Default</Badge>
                  )}
                </button>
              ))}
              <button
                onClick={() => navigate('/customer/vehicles/add')}
                className="w-full glass-card p-4 flex items-center gap-3 text-muted-foreground hover:text-foreground hover:border-white/20 transition-colors"
              >
                <div className="w-10 h-10 rounded-xl bg-white/5 flex items-center justify-center border border-dashed border-white/20">
                  <span className="text-lg">+</span>
                </div>
                <span className="text-sm font-medium">Add New Vehicle</span>
              </button>
            </div>
          )}

          {/* Confirm Step */}
          {step === 'confirm' && (
            <div className="space-y-4">
              {/* Route Card */}
              <div className="glass-card p-5">
                <div className="space-y-3">
                  <div className="flex items-start gap-3">
                    <div className="w-3 h-3 rounded-full bg-green-400 mt-1.5 ring-4 ring-green-400/20" />
                    <div>
                      <p className="text-[10px] text-muted-foreground uppercase tracking-wider">Pickup</p>
                      <p className="font-medium text-sm">{pickupLocation?.address}</p>
                    </div>
                  </div>
                  <div className="ml-1.5 border-l border-dashed border-white/20 h-4" />
                  <div className="flex items-start gap-3">
                    <div className="w-3 h-3 rounded-full bg-primary mt-1.5 ring-4 ring-primary/20" />
                    <div>
                      <p className="text-[10px] text-muted-foreground uppercase tracking-wider">Drop-off</p>
                      <p className="font-medium text-sm">{dropLocation?.address}</p>
                    </div>
                  </div>
                </div>
              </div>

              {/* Vehicle Card */}
              <div className="glass-card p-4 flex items-center gap-3">
                <div className="w-10 h-10 rounded-xl bg-white/5 flex items-center justify-center">
                  <Car className="w-5 h-5 text-primary" />
                </div>
                <div>
                  <p className="font-medium text-sm">{selectedVehicle?.make} {selectedVehicle?.model}</p>
                  <p className="text-xs text-muted-foreground">{selectedVehicle?.color} • {selectedVehicle?.registrationNumber}</p>
                </div>
              </div>

              {/* Fare Estimate */}
              {fareEstimate && (
                <div className="glass-card p-5 space-y-3">
                  <div className="flex items-center justify-between text-sm">
                    <span className="text-muted-foreground flex items-center gap-2">
                      <Navigation className="w-3.5 h-3.5" /> Distance
                    </span>
                    <span>{formatDistance(fareEstimate.distance)}</span>
                  </div>
                  <div className="flex items-center justify-between text-sm">
                    <span className="text-muted-foreground flex items-center gap-2">
                      <Clock className="w-3.5 h-3.5" /> Duration
                    </span>
                    <span>{formatDuration(fareEstimate.duration)}</span>
                  </div>
                  <div className="border-t border-white/10 pt-3 flex items-center justify-between">
                    <span className="font-medium flex items-center gap-2">
                      <IndianRupee className="w-4 h-4" /> Estimated Fare
                    </span>
                    <span className="text-2xl font-bold gradient-text">
                      {formatCurrency(fareEstimate.fare)}
                    </span>
                  </div>
                </div>
              )}

              {/* Book Button */}
              <MovingBorder
                as="div"
                duration={3000}
                containerClassName="w-full h-14 rounded-xl"
                className="bg-background hover:bg-white/5 transition-colors cursor-pointer"
              >
                <button
                  onClick={handleBookRide}
                  disabled={isLoading}
                  className="w-full h-full flex items-center justify-center gap-2 font-semibold text-foreground disabled:opacity-50"
                >
                  {isLoading ? (
                    <>
                      <Loader2 className="h-5 w-5 animate-spin" />
                      Finding a Driver...
                    </>
                  ) : (
                    <>
                      Book Now
                      <ArrowRight className="h-5 w-5" />
                    </>
                  )}
                </button>
              </MovingBorder>
            </div>
          )}
        </motion.div>
      </AnimatePresence>
    </div>
  );
};

export default BookRidePage;
