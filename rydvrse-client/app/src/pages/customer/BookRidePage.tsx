/**
 * Book Ride Page
 * Allows customers to book a driver for their vehicle
 */

import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  MapPin,
  Car,
  Navigation,
  Search,
  Loader2,
  ArrowRight,
  Home,
  Briefcase,
} from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Card, CardContent } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Separator } from '@/components/ui/separator';
import { useAuthStore, useCustomerStore } from '@/store';
import { useTrip, useLocation } from '@/hooks';
import { MapContainer } from '@/components/maps';
import { formatCurrency, formatDistance, formatDuration } from '@/utils/formatters';
import type { Vehicle, GeoLocation } from '@/types';

// Mock fare estimation
const estimateFare = (distanceKm: number): { fare: number; duration: number; distance: number } => {
  const baseFare = 50;
  const perKmRate = 15;
  const fare = baseFare + distanceKm * perKmRate;
  const duration = Math.ceil(distanceKm * 2.5); // Approx 2.5 min per km
  return { fare: Math.round(fare), duration, distance: distanceKm };
};

export const BookRidePage = () => {
  const navigate = useNavigate();
  const { user } = useAuthStore();
  const { vehicles, defaultVehicle, savedLocations } = useCustomerStore();
  const { createTrip, isLoading } = useTrip();
  const { location, getCurrentPosition } = useLocation();

  const [step, setStep] = useState<'pickup' | 'drop' | 'vehicle' | 'confirm'>('pickup');
  const [pickupLocation, setPickupLocation] = useState<GeoLocation | null>(null);
  const [dropLocation, setDropLocation] = useState<GeoLocation | null>(null);
  const [selectedVehicle, setSelectedVehicle] = useState<Vehicle | null>(defaultVehicle);
  const [fareEstimate, setFareEstimate] = useState<{ fare: number; duration: number; distance: number } | null>(null);
  const [searchQuery, setSearchQuery] = useState('');

  // Get current location on mount
  useEffect(() => {
    getCurrentPosition();
  }, [getCurrentPosition]);

  // Set pickup to current location
  useEffect(() => {
    if (location && !pickupLocation) {
      setPickupLocation({
        latitude: location.latitude,
        longitude: location.longitude,
        address: 'Current Location',
      });
    }
  }, [location, pickupLocation]);

  // Calculate fare when both locations are set
  useEffect(() => {
    if (pickupLocation && dropLocation) {
      // Calculate distance (mock calculation)
      const distance = Math.random() * 15 + 5; // Random distance between 5-20 km
      const estimate = estimateFare(distance);
      setFareEstimate(estimate);
    }
  }, [pickupLocation, dropLocation]);

  const handleLocationSelect = (type: 'pickup' | 'drop', location: GeoLocation) => {
    if (type === 'pickup') {
      setPickupLocation(location);
      setStep('drop');
    } else {
      setDropLocation(location);
      setStep('vehicle');
    }
  };

  const handleBookRide = async () => {
    if (!pickupLocation || !dropLocation || !selectedVehicle || !user) return;

    const trip = await createTrip({
      customerId: user.id,
      vehicleId: selectedVehicle.id,
      pickupLat: pickupLocation.latitude,
      pickupLng: pickupLocation.longitude,
      pickupAddress: pickupLocation.address,
      pickupCity: pickupLocation.city,
      dropLat: dropLocation.latitude,
      dropLng: dropLocation.longitude,
      dropAddress: dropLocation.address,
      dropCity: dropLocation.city,
      estimatedFare: fareEstimate?.fare,
    });

    if (trip) {
      navigate(`/customer/trips/${trip.id}`);
    }
  };

  const renderLocationSelector = (type: 'pickup' | 'drop') => (
    <div className="space-y-4">
      <div className="relative">
        <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground" />
        <Input
          placeholder={`Search ${type === 'pickup' ? 'pickup' : 'drop'} location`}
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
          className="pl-10"
        />
      </div>

      {/* Current Location */}
      {type === 'pickup' && location && (
        <Button
          variant="outline"
          className="w-full justify-start gap-3"
          onClick={() =>
            handleLocationSelect(type, {
              latitude: location.latitude,
              longitude: location.longitude,
              address: 'Current Location',
            })
          }
        >
          <Navigation className="w-5 h-5 text-primary" />
          <div className="text-left">
            <p className="font-medium">Current Location</p>
            <p className="text-sm text-muted-foreground">Use my current location</p>
          </div>
        </Button>
      )}

      {/* Saved Locations */}
      {savedLocations.length > 0 && (
        <>
          <Separator />
          <p className="text-sm font-medium text-muted-foreground">Saved Locations</p>
          <div className="space-y-2">
            {savedLocations.map((savedLoc) => (
              <Button
                key={savedLoc.id}
                variant="outline"
                className="w-full justify-start gap-3"
                onClick={() => handleLocationSelect(type, savedLoc.location)}
              >
                {savedLoc.type === 'HOME' ? (
                  <Home className="w-5 h-5 text-green-500" />
                ) : savedLoc.type === 'WORK' ? (
                  <Briefcase className="w-5 h-5 text-blue-500" />
                ) : (
                  <MapPin className="w-5 h-5 text-gray-500" />
                )}
                <div className="text-left">
                  <p className="font-medium">{savedLoc.name}</p>
                  <p className="text-sm text-muted-foreground truncate max-w-[200px]">
                    {savedLoc.location.address}
                  </p>
                </div>
              </Button>
            ))}
          </div>
        </>
      )}

      {/* Map Selection */}
      <Separator />
      <p className="text-sm font-medium text-muted-foreground">Select on Map</p>
      <MapContainer
        userLocation={location}
        height="200px"
        onMapClick={(lat, lng) =>
          handleLocationSelect(type, {
            latitude: lat,
            longitude: lng,
            address: `${lat.toFixed(4)}, ${lng.toFixed(4)}`,
          })
        }
      />
    </div>
  );

  const renderVehicleSelector = () => (
    <div className="space-y-4">
      <p className="text-sm font-medium text-muted-foreground">Select Vehicle</p>
      <div className="space-y-3">
        {vehicles.map((vehicle) => (
          <button
            key={vehicle.id}
            onClick={() => {
              setSelectedVehicle(vehicle);
              setStep('confirm');
            }}
            className={`w-full p-4 rounded-xl border-2 transition-all ${
              selectedVehicle?.id === vehicle.id
                ? 'border-primary bg-primary/5'
                : 'border-border hover:border-primary/50'
            }`}
          >
            <div className="flex items-center gap-4">
              <div className="w-12 h-12 rounded-full bg-muted flex items-center justify-center">
                <Car className="w-6 h-6" />
              </div>
              <div className="flex-1 text-left">
                <p className="font-medium">
                  {vehicle.make} {vehicle.model}
                </p>
                <p className="text-sm text-muted-foreground">
                  {vehicle.color} • {vehicle.registrationNumber}
                </p>
              </div>
              {vehicle.isDefault && (
                <Badge variant="secondary">Default</Badge>
              )}
            </div>
          </button>
        ))}
      </div>

      <Button
        variant="outline"
        className="w-full"
        onClick={() => navigate('/customer/vehicles/add')}
      >
        + Add New Vehicle
      </Button>
    </div>
  );

  const renderConfirmation = () => (
    <div className="space-y-6">
      {/* Trip Summary */}
      <Card>
        <CardContent className="p-4 space-y-4">
          <div className="flex items-start gap-3">
            <div className="w-3 h-3 rounded-full bg-green-500 mt-1.5" />
            <div>
              <p className="font-medium">Pickup</p>
              <p className="text-sm text-muted-foreground">
                {pickupLocation?.address}
              </p>
            </div>
          </div>
          <div className="flex items-start gap-3">
            <div className="w-3 h-3 rounded-full bg-red-500 mt-1.5" />
            <div>
              <p className="font-medium">Drop</p>
              <p className="text-sm text-muted-foreground">
                {dropLocation?.address}
              </p>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Vehicle Info */}
      <Card>
        <CardContent className="p-4">
          <div className="flex items-center gap-4">
            <div className="w-12 h-12 rounded-full bg-muted flex items-center justify-center">
              <Car className="w-6 h-6" />
            </div>
            <div>
              <p className="font-medium">
                {selectedVehicle?.make} {selectedVehicle?.model}
              </p>
              <p className="text-sm text-muted-foreground">
                {selectedVehicle?.color} • {selectedVehicle?.registrationNumber}
              </p>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Fare Estimate */}
      {fareEstimate && (
        <Card>
          <CardContent className="p-4 space-y-3">
            <div className="flex items-center justify-between">
              <span className="text-muted-foreground flex items-center gap-2">
                <Navigation className="w-4 h-4" />
                Distance
              </span>
              <span>{formatDistance(fareEstimate.distance)}</span>
            </div>
            <div className="flex items-center justify-between">
              <span className="text-muted-foreground flex items-center gap-2">
                Distance
              </span>
              <span>{formatDuration(fareEstimate.duration)}</span>
            </div>
            <Separator />
            <div className="flex items-center justify-between">
              <span className="font-medium">Estimated Fare</span>
              <span className="text-xl font-bold">
                {formatCurrency(fareEstimate.fare)}
              </span>
            </div>
          </CardContent>
        </Card>
      )}

      {/* Book Button */}
      <Button
        size="lg"
        className="w-full"
        onClick={handleBookRide}
        disabled={isLoading}
      >
        {isLoading ? (
          <>
            <Loader2 className="mr-2 h-5 w-5 animate-spin" />
            Booking...
          </>
        ) : (
          <>
            Book Now
            <ArrowRight className="ml-2 h-5 w-5" />
          </>
        )}
      </Button>
    </div>
  );

  return (
    <div className="max-w-lg mx-auto space-y-6">
      {/* Header */}
      <div className="flex items-center gap-4">
        <Button
          variant="ghost"
          size="icon"
          onClick={() => {
            if (step === 'pickup') navigate('/customer/dashboard');
            else if (step === 'drop') setStep('pickup');
            else if (step === 'vehicle') setStep('drop');
            else setStep('vehicle');
          }}
        >
          <ArrowRight className="w-5 h-5 rotate-180" />
        </Button>
        <h1 className="text-xl font-bold">
          {step === 'pickup' && 'Select Pickup'}
          {step === 'drop' && 'Select Drop'}
          {step === 'vehicle' && 'Select Vehicle'}
          {step === 'confirm' && 'Confirm Booking'}
        </h1>
      </div>

      {/* Progress Indicator */}
      <div className="flex gap-2">
        {['pickup', 'drop', 'vehicle', 'confirm'].map((s, i) => (
          <div
            key={s}
            className={`flex-1 h-1 rounded-full ${
              step === s
                ? 'bg-primary'
                : ['pickup', 'drop', 'vehicle', 'confirm'].indexOf(step) > i
                ? 'bg-primary/50'
                : 'bg-muted'
            }`}
          />
        ))}
      </div>

      {/* Content */}
      {step === 'pickup' && renderLocationSelector('pickup')}
      {step === 'drop' && renderLocationSelector('drop')}
      {step === 'vehicle' && renderVehicleSelector()}
      {step === 'confirm' && renderConfirmation()}
    </div>
  );
};

export default BookRidePage;
