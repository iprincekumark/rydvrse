/**
 * Driver Dashboard — Premium dark theme with availability toggle and earnings
 */

import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import {
  Power, TrendingUp, Star, Car, AlertCircle, Loader2,
  IndianRupee, MapPin, FileText, ChevronRight,
} from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar';
import { useAuthStore, useDriverStore } from '@/store';
import { useDriver, useLocation } from '@/hooks';
import { MapContainer } from '@/components/maps';
import { formatCurrency } from '@/utils/formatters';
import { DriverStatus } from '@/types';
import { Meteors } from '@/components/aceternity/meteors';
import { TextGenerateEffect } from '@/components/aceternity/text-generate-effect';

const container = {
  hidden: { opacity: 0 },
  show: { opacity: 1, transition: { staggerChildren: 0.08 } },
};
const item = { hidden: { opacity: 0, y: 16 }, show: { opacity: 1, y: 0 } };

export const DriverDashboardPage = () => {
  const navigate = useNavigate();
  const { user } = useAuthStore();
  const { driver, isOnline, setIsOnline } = useDriverStore();
  const { getProfile, toggleAvailability, updateLocation } = useDriver();
  const { location, getCurrentPosition, startWatching, stopWatching } = useLocation({ watch: true });
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const load = async () => { setIsLoading(true); await getProfile(); setIsLoading(false); };
    load();
  }, [getProfile]);

  useEffect(() => {
    getCurrentPosition();
    if (isOnline) startWatching();
    return () => stopWatching();
  }, [getCurrentPosition, startWatching, stopWatching, isOnline]);

  useEffect(() => {
    if (isOnline && location && driver) updateLocation(location.latitude, location.longitude);
  }, [location, isOnline, driver, updateLocation]);

  const handleToggleOnline = async () => {
    const newStatus = !isOnline;
    const success = await toggleAvailability(newStatus);
    if (success) setIsOnline(newStatus);
  };

  if (isLoading) {
    return (
      <div className="min-h-[60vh] flex items-center justify-center">
        <Loader2 className="w-8 h-8 animate-spin text-primary" />
      </div>
    );
  }

  const isApproved = driver?.status === DriverStatus.APPROVED || driver?.status === DriverStatus.ACTIVE;

  if (!isApproved) {
    return (
      <div className="max-w-md mx-auto mt-12">
        <div className="glass-card p-8 text-center">
          <div className="w-16 h-16 rounded-2xl bg-yellow-500/20 flex items-center justify-center mx-auto mb-4">
            <AlertCircle className="w-8 h-8 text-yellow-500" />
          </div>
          <h2 className="text-xl font-semibold mb-2">Account Under Review</h2>
          <p className="text-muted-foreground text-sm mb-6">
            Your driver account is being reviewed. We'll notify you once approved.
          </p>
          <Button onClick={() => navigate('/driver/documents')} className="gradient-primary text-white border-0">
            View Documents
          </Button>
        </div>
      </div>
    );
  }

  const stats = [
    { label: "Today's Earnings", value: formatCurrency(driver?.totalEarnings || 0), icon: <IndianRupee className="h-4 w-4" />, gradient: 'from-emerald-500 to-emerald-600' },
    { label: 'Total Trips', value: String(driver?.totalTrips || 0), icon: <Car className="h-4 w-4" />, gradient: 'from-blue-500 to-blue-600' },
    { label: 'Rating', value: driver?.rating ? driver.rating.toFixed(1) : 'N/A', icon: <Star className="h-4 w-4" />, gradient: 'from-amber-500 to-amber-600' },
  ];

  return (
    <motion.div variants={container} initial="hidden" animate="show" className="space-y-6">
      {/* Header */}
      <motion.div variants={item} className="flex items-center justify-between">
        <div className="flex items-center gap-4">
          <Avatar className="h-14 w-14 border-2 border-white/10">
            <AvatarImage src={user?.profileImageUrl} alt={user?.firstName} />
            <AvatarFallback className="bg-primary/20 text-primary font-semibold">
              {user?.firstName?.[0]}{user?.lastName?.[0]}
            </AvatarFallback>
          </Avatar>
          <div>
            <TextGenerateEffect
              words={`Hello, ${user?.firstName || 'Driver'}!`}
              className="text-xl font-bold text-foreground"
              duration={0.2}
            />
            <p className="text-sm text-muted-foreground mt-0.5">
              {isOnline ? 'Ready for trips' : 'Currently offline'}
            </p>
          </div>
        </div>
        <Badge className={isOnline
          ? 'bg-green-500/20 text-green-400 border-green-500/30'
          : 'bg-white/5 text-muted-foreground border-white/10'
        }>
          <div className={`w-1.5 h-1.5 rounded-full mr-1.5 ${isOnline ? 'bg-green-400 animate-pulse' : 'bg-muted-foreground'}`} />
          {isOnline ? 'Online' : 'Offline'}
        </Badge>
      </motion.div>

      {/* Online Toggle */}
      <motion.div variants={item}>
        <div className={`glass-card p-5 transition-all ${isOnline ? 'border-green-500/30 shadow-[0_0_20px_rgba(34,197,94,0.1)]' : ''}`}>
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-4">
              <motion.div
                className={`w-14 h-14 rounded-2xl flex items-center justify-center transition-colors ${
                  isOnline ? 'bg-green-500 shadow-[0_0_20px_rgba(34,197,94,0.4)]' : 'bg-white/10'
                }`}
                whileTap={{ scale: 0.95 }}
              >
                <Power className="w-6 h-6 text-white" />
              </motion.div>
              <div>
                <p className="font-semibold">Go {isOnline ? 'Offline' : 'Online'}</p>
                <p className="text-xs text-muted-foreground">
                  {isOnline ? 'Stop receiving requests' : 'Start receiving requests'}
                </p>
              </div>
            </div>
            <Button
              size="lg"
              onClick={handleToggleOnline}
              className={isOnline
                ? 'bg-white/10 text-foreground hover:bg-white/20 border-0'
                : 'gradient-primary text-white border-0 shadow-glow'
              }
            >
              {isOnline ? 'Go Offline' : 'Go Online'}
            </Button>
          </div>
        </div>
      </motion.div>

      {/* Stats */}
      <motion.div variants={item} className="grid grid-cols-3 gap-3">
        {stats.map((stat) => (
          <div key={stat.label} className="relative overflow-hidden glass-card p-4">
            <Meteors number={4} />
            <div className="relative z-10">
              <div className={`w-9 h-9 rounded-lg bg-gradient-to-br ${stat.gradient} flex items-center justify-center mb-3`}>
                {stat.icon}
              </div>
              <p className="text-xl font-bold text-foreground">{stat.value}</p>
              <p className="text-[10px] text-muted-foreground mt-1">{stat.label}</p>
            </div>
          </div>
        ))}
      </motion.div>

      {/* Map */}
      <motion.div variants={item} className="glass-card overflow-hidden">
        <div className="p-4 pb-2">
          <h3 className="text-sm font-semibold flex items-center gap-2">
            <MapPin className="h-4 w-4 text-primary" />
            Your Location
          </h3>
        </div>
        <MapContainer userLocation={location} height="200px" />
      </motion.div>

      {/* Quick Actions */}
      <motion.div variants={item} className="grid grid-cols-2 gap-3">
        {[
          { label: 'My Trips', icon: Car, href: '/driver/trips' },
          { label: 'Earnings', icon: TrendingUp, href: '/driver/earnings' },
          { label: 'Documents', icon: FileText, href: '/driver/documents' },
          { label: 'Profile', icon: Star, href: '/driver/profile' },
        ].map((action) => (
          <button
            key={action.label}
            onClick={() => navigate(action.href)}
            className="glass-card p-4 flex items-center gap-3 hover:border-white/20 transition-colors group"
          >
            <div className="w-10 h-10 rounded-xl bg-white/5 flex items-center justify-center group-hover:bg-primary/10 transition-colors">
              <action.icon className="w-5 h-5 text-muted-foreground group-hover:text-primary transition-colors" />
            </div>
            <span className="text-sm font-medium flex-1 text-left">{action.label}</span>
            <ChevronRight className="w-4 h-4 text-muted-foreground" />
          </button>
        ))}
      </motion.div>
    </motion.div>
  );
};

export default DriverDashboardPage;
