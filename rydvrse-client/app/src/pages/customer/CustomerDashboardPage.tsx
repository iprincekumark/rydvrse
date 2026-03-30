/**
 * Customer Dashboard — Premium design with TextGenerate, SpotlightCards, GlowingBorder
 */

import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import {
  MapPin, Clock, Car, ChevronRight, Plus, Zap, Star,
} from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar';
import { useAuthStore, useTripStore, useCustomerStore } from '@/store';
import { useTrip, useLocation } from '@/hooks';
import { MapContainer } from '@/components/maps';
import { formatCurrency, formatDateTime, formatStatus } from '@/utils/formatters';
import { TextGenerateEffect } from '@/components/aceternity/text-generate-effect';
import { Meteors } from '@/components/aceternity/meteors';
import { GlowingBorder } from '@/components/aceternity/glowing-effect';

const COMPLETED_STATUSES = ['TRIP_COMPLETED', 'CANCELLED'];

const container = {
  hidden: { opacity: 0 },
  show: {
    opacity: 1,
    transition: { staggerChildren: 0.1 },
  },
};

const item = {
  hidden: { opacity: 0, y: 20 },
  show: { opacity: 1, y: 0 },
};

export const CustomerDashboardPage = () => {
  const navigate = useNavigate();
  const { user } = useAuthStore();
  const { activeTrip, tripHistory } = useTripStore();
  const { defaultVehicle } = useCustomerStore();
  const { getCurrentPosition, location } = useLocation();
  const { getCustomerTrips } = useTrip();

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
      description: 'Get a driver now',
      icon: Car,
      onClick: () => navigate('/customer/book'),
      gradient: 'from-blue-500 to-blue-600',
    },
    {
      label: 'My Trips',
      description: 'View history',
      icon: Clock,
      onClick: () => navigate('/customer/trips'),
      gradient: 'from-violet-500 to-violet-600',
    },
    {
      label: 'Add Vehicle',
      description: 'Register your car',
      icon: Plus,
      onClick: () => navigate('/customer/vehicles/add'),
      gradient: 'from-emerald-500 to-emerald-600',
    },
  ];

  return (
    <motion.div
      variants={container}
      initial="hidden"
      animate="show"
      className="space-y-6"
    >
      {/* Welcome Section */}
      <motion.div variants={item} className="flex items-center justify-between">
        <div>
          <TextGenerateEffect
            words={`Welcome back, ${user?.firstName || 'Guest'}!`}
            className="text-2xl md:text-3xl font-bold text-foreground"
            duration={0.3}
          />
          <p className="text-muted-foreground mt-1">
            Ready to book your next ride?
          </p>
        </div>
        <Avatar className="h-12 w-12 border-2 border-white/10">
          <AvatarImage src={user?.profileImageUrl} alt={user?.firstName} />
          <AvatarFallback className="bg-primary/20 text-primary font-semibold">
            {user?.firstName?.[0]}{user?.lastName?.[0]}
          </AvatarFallback>
        </Avatar>
      </motion.div>

      {/* Active Trip Card */}
      {hasActiveTrip && (
        <motion.div variants={item}>
          <GlowingBorder active className="rounded-xl">
            <div className="glass-card p-5">
              <div className="flex items-center justify-between mb-4">
                <div className="flex items-center gap-2">
                  <div className="w-2 h-2 rounded-full bg-green-400 animate-pulse" />
                  <span className="text-sm font-semibold text-foreground">Active Trip</span>
                </div>
                <Badge className="bg-primary/20 text-primary border-primary/30">
                  {formatStatus(activeTrip.status)}
                </Badge>
              </div>
              <div className="space-y-3">
                <div className="flex items-start gap-3">
                  <div className="w-3 h-3 rounded-full bg-green-400 mt-1 ring-4 ring-green-400/20" />
                  <div>
                    <p className="font-medium text-sm">{activeTrip.pickupLocation.address || 'Pickup'}</p>
                  </div>
                </div>
                <div className="ml-1.5 border-l border-dashed border-white/20 h-4" />
                <div className="flex items-start gap-3">
                  <div className="w-3 h-3 rounded-full bg-primary mt-1 ring-4 ring-primary/20" />
                  <div>
                    <p className="font-medium text-sm">{activeTrip.dropLocation.address || 'Destination'}</p>
                  </div>
                </div>
              </div>
              {activeTrip.estimatedFare && (
                <div className="flex items-center justify-between pt-4 mt-4 border-t border-white/10">
                  <span className="text-sm text-muted-foreground">Estimated Fare</span>
                  <span className="font-bold text-lg gradient-text">
                    {formatCurrency(activeTrip.estimatedFare)}
                  </span>
                </div>
              )}
              <Button
                className="w-full mt-4 gradient-primary text-white border-0 shadow-glow hover:shadow-glow-lg transition-shadow"
                onClick={() => navigate(`/customer/trips/${activeTrip.id}`)}
              >
                Track Ride
              </Button>
            </div>
          </GlowingBorder>
        </motion.div>
      )}

      {/* Quick Actions */}
      <motion.div variants={item} className="grid grid-cols-3 gap-3">
        {quickActions.map((action) => (
          <button
            key={action.label}
            onClick={action.onClick}
            className="glass-card p-4 flex flex-col items-center gap-3 group hover:border-white/20 transition-all hover:shadow-glow/20"
          >
            <div className={`w-12 h-12 rounded-xl bg-gradient-to-br ${action.gradient} flex items-center justify-center shadow-lg group-hover:scale-110 transition-transform`}>
              <action.icon className="w-5 h-5 text-white" />
            </div>
            <div className="text-center">
              <p className="text-sm font-medium text-foreground">{action.label}</p>
              <p className="text-[10px] text-muted-foreground mt-0.5">{action.description}</p>
            </div>
          </button>
        ))}
      </motion.div>

      {/* Stats Row */}
      <motion.div variants={item} className="grid grid-cols-3 gap-3">
        {[
          { label: 'Total Trips', value: tripHistory.length.toString(), icon: <Car className="h-4 w-4" /> },
          { label: 'Saved', value: '₹0', icon: <Zap className="h-4 w-4" /> },
          { label: 'Rating', value: '5.0', icon: <Star className="h-4 w-4" /> },
        ].map((stat) => (
          <div
            key={stat.label}
            className="relative overflow-hidden glass-card p-4"
          >
            <Meteors number={5} />
            <div className="relative z-10">
              <div className="flex items-center gap-1.5 text-muted-foreground mb-2">
                {stat.icon}
                <span className="text-xs">{stat.label}</span>
              </div>
              <p className="text-2xl font-bold text-foreground">{stat.value}</p>
            </div>
          </div>
        ))}
      </motion.div>

      {/* Map Section */}
      <motion.div variants={item} className="glass-card overflow-hidden">
        <div className="p-4 pb-2">
          <h3 className="text-sm font-semibold flex items-center gap-2">
            <MapPin className="h-4 w-4 text-primary" />
            Your Location
          </h3>
        </div>
        <MapContainer userLocation={location} height="200px" />
      </motion.div>

      {/* Default Vehicle */}
      {defaultVehicle && (
        <motion.div variants={item} className="glass-card p-4">
          <div className="flex items-center justify-between mb-3">
            <h3 className="text-sm font-semibold flex items-center gap-2">
              <Car className="h-4 w-4 text-primary" />
              Default Vehicle
            </h3>
            <Button
              variant="ghost"
              size="sm"
              onClick={() => navigate('/customer/vehicles')}
              className="text-xs text-muted-foreground hover:text-foreground"
            >
              Manage <ChevronRight className="w-3 h-3 ml-0.5" />
            </Button>
          </div>
          <div className="flex items-center gap-3">
            <div className="w-12 h-12 rounded-xl bg-white/5 flex items-center justify-center">
              <Car className="w-6 h-6 text-primary" />
            </div>
            <div>
              <p className="font-medium text-sm">
                {defaultVehicle.make} {defaultVehicle.model}
              </p>
              <p className="text-xs text-muted-foreground">
                {defaultVehicle.color} • {defaultVehicle.registrationNumber}
              </p>
            </div>
          </div>
        </motion.div>
      )}

      {/* Recent Trips */}
      <motion.div variants={item} className="glass-card p-4">
        <div className="flex items-center justify-between mb-4">
          <h3 className="text-sm font-semibold flex items-center gap-2">
            <Clock className="h-4 w-4 text-primary" />
            Recent Trips
          </h3>
          <Button
            variant="ghost"
            size="sm"
            onClick={() => navigate('/customer/trips')}
            className="text-xs text-muted-foreground hover:text-foreground"
          >
            View All <ChevronRight className="w-3 h-3 ml-0.5" />
          </Button>
        </div>

        {tripHistory.length === 0 ? (
          <div className="text-center py-10">
            <div className="w-16 h-16 rounded-2xl bg-white/5 flex items-center justify-center mx-auto mb-4">
              <Car className="w-8 h-8 text-muted-foreground" />
            </div>
            <p className="text-muted-foreground text-sm mb-4">No trips yet</p>
            <Button
              onClick={() => navigate('/customer/book')}
              className="gradient-primary text-white border-0"
            >
              Book Your First Ride
            </Button>
          </div>
        ) : (
          <div className="space-y-2">
            {tripHistory.slice(0, 3).map((trip) => (
              <motion.div
                key={trip.id}
                whileHover={{ x: 4 }}
                className="flex items-center justify-between p-3 rounded-xl bg-white/[0.03] hover:bg-white/[0.06] cursor-pointer transition-colors"
                onClick={() => navigate(`/customer/trips/${trip.id}`)}
              >
                <div className="flex items-center gap-3">
                  <div className="w-10 h-10 rounded-xl bg-primary/10 flex items-center justify-center">
                    <Car className="w-5 h-5 text-primary" />
                  </div>
                  <div>
                    <p className="font-medium text-sm">
                      {trip.pickupLocation.city || 'Pickup'} → {trip.dropLocation.city || 'Drop'}
                    </p>
                    <p className="text-xs text-muted-foreground">
                      {formatDateTime(trip.createdAt)}
                    </p>
                  </div>
                </div>
                <div className="text-right">
                  <p className="font-semibold text-sm">
                    {trip.finalFare
                      ? formatCurrency(trip.finalFare)
                      : formatCurrency(trip.estimatedFare || 0)}
                  </p>
                  <Badge
                    variant={trip.status === 'TRIP_COMPLETED' ? 'default' : 'secondary'}
                    className="text-[10px] mt-1"
                  >
                    {formatStatus(trip.status)}
                  </Badge>
                </div>
              </motion.div>
            ))}
          </div>
        )}
      </motion.div>
    </motion.div>
  );
};

export default CustomerDashboardPage;
