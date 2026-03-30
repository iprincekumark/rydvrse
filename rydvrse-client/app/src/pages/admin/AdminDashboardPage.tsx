/**
 * Admin Dashboard — Premium BentoGrid layout with glassmorphism
 */

import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import {
  Users, Car, TrendingUp, Clock, AlertCircle, CheckCircle,
  IndianRupee, Activity, ArrowUpRight, Shield, Loader2,
  ChevronRight,
} from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { adminService } from '@/services';
import { useUIStore } from '@/store';
import type { AdminDashboardStats } from '@/types';
import { formatCurrency } from '@/utils/formatters';
import { Meteors } from '@/components/aceternity/meteors';
import { TextGenerateEffect } from '@/components/aceternity/text-generate-effect';

const container = {
  hidden: { opacity: 0 },
  show: { opacity: 1, transition: { staggerChildren: 0.08 } },
};
const item = { hidden: { opacity: 0, y: 16 }, show: { opacity: 1, y: 0 } };

export const AdminDashboardPage = () => {
  const navigate = useNavigate();
  const { addToast } = useUIStore();
  const [stats, setStats] = useState<AdminDashboardStats | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const loadStats = async () => {
      setIsLoading(true);
      try {
        const data = await adminService.getDashboardStats();
        setStats(data);
      } catch {
        addToast({ type: 'error', message: 'Failed to load dashboard statistics' });
      } finally {
        setIsLoading(false);
      }
    };
    loadStats();
  }, [addToast]);

  if (isLoading) {
    return (
      <div className="min-h-[60vh] flex items-center justify-center">
        <Loader2 className="w-8 h-8 animate-spin text-primary" />
      </div>
    );
  }

  const statCards = [
    { title: 'Total Trips', value: String(stats?.totalTrips || 0), icon: <Car className="h-5 w-5" />, change: '+12%', gradient: 'from-blue-500 to-blue-600' },
    { title: 'Total Drivers', value: String(stats?.totalDrivers || 0), icon: <Users className="h-5 w-5" />, change: '+5%', gradient: 'from-emerald-500 to-emerald-600' },
    { title: 'Total Customers', value: String(stats?.totalCustomers || 0), icon: <Users className="h-5 w-5" />, change: '+8%', gradient: 'from-violet-500 to-violet-600' },
    { title: 'Total Revenue', value: formatCurrency(stats?.totalRevenue || 0), icon: <IndianRupee className="h-5 w-5" />, change: '+15%', gradient: 'from-amber-500 to-amber-600' },
  ];

  const quickStats = [
    { label: 'Active Trips', value: String(stats?.activeTrips || 0), icon: <Activity className="h-5 w-5" />, color: 'text-blue-400' },
    { label: 'Pending Approvals', value: String(stats?.pendingDriverApprovals || 0), icon: <AlertCircle className="h-5 w-5" />, color: 'text-orange-400', href: '/admin/drivers' },
    { label: "Today's Trips", value: String(stats?.todayTrips || 0), icon: <Clock className="h-5 w-5" />, color: 'text-emerald-400' },
    { label: "Today's Revenue", value: formatCurrency(stats?.todayRevenue || 0), icon: <TrendingUp className="h-5 w-5" />, color: 'text-violet-400' },
  ];

  const actionCards = [
    {
      title: 'Driver Approvals',
      description: `${stats?.pendingDriverApprovals || 0} pending`,
      icon: <AlertCircle className="h-6 w-6" />,
      gradient: 'from-orange-500 to-orange-600',
      action: 'Review',
      href: '/admin/drivers',
    },
    {
      title: 'Active Trips',
      description: `${stats?.activeTrips || 0} in progress`,
      icon: <Car className="h-6 w-6" />,
      gradient: 'from-blue-500 to-blue-600',
      action: 'Monitor',
      href: '/admin/trips',
    },
    {
      title: 'Safety Alerts',
      description: 'View incidents',
      icon: <Shield className="h-6 w-6" />,
      gradient: 'from-red-500 to-red-600',
      action: 'View',
      href: '/admin/safety',
    },
  ];

  return (
    <motion.div variants={container} initial="hidden" animate="show" className="space-y-6">
      {/* Header */}
      <motion.div variants={item}>
        <TextGenerateEffect
          words="Admin Dashboard"
          className="text-2xl md:text-3xl font-bold text-foreground"
          duration={0.2}
        />
        <p className="text-muted-foreground mt-1">Welcome to RYDVRSE Admin Panel</p>
      </motion.div>

      {/* Main Stats Grid */}
      <motion.div variants={item} className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        {statCards.map((card) => (
          <div key={card.title} className="relative overflow-hidden glass-card p-5 group">
            <Meteors number={6} />
            <div className="relative z-10">
              <div className="flex items-center justify-between mb-4">
                <div className={`w-10 h-10 rounded-xl bg-gradient-to-br ${card.gradient} flex items-center justify-center shadow-lg`}>
                  {card.icon}
                </div>
                <div className="flex items-center gap-1 text-green-400 text-xs font-medium">
                  <ArrowUpRight className="w-3 h-3" />
                  {card.change}
                </div>
              </div>
              <p className="text-2xl font-bold text-foreground">{card.value}</p>
              <p className="text-xs text-muted-foreground mt-1">{card.title}</p>
            </div>
          </div>
        ))}
      </motion.div>

      {/* Quick Stats */}
      <motion.div variants={item} className="glass-card p-5">
        <h3 className="text-sm font-semibold mb-4">Quick Overview</h3>
        <div className="grid grid-cols-2 md:grid-cols-4 gap-3">
          {quickStats.map((stat) => (
            <button
              key={stat.label}
              onClick={() => stat.href && navigate(stat.href)}
              className={`p-4 rounded-xl bg-white/[0.03] hover:bg-white/[0.06] transition-colors text-left ${stat.href ? 'cursor-pointer' : ''}`}
            >
              <div className={`${stat.color} mb-2`}>{stat.icon}</div>
              <p className="text-xl font-bold text-foreground">{stat.value}</p>
              <p className="text-xs text-muted-foreground mt-0.5">{stat.label}</p>
            </button>
          ))}
        </div>
      </motion.div>

      {/* Action Cards */}
      <motion.div variants={item} className="grid grid-cols-1 md:grid-cols-3 gap-4">
        {actionCards.map((card) => (
          <div key={card.title} className="glass-card p-5">
            <div className="flex items-center gap-4">
              <div className={`w-12 h-12 rounded-xl bg-gradient-to-br ${card.gradient} flex items-center justify-center shadow-lg flex-shrink-0`}>
                {card.icon}
              </div>
              <div className="flex-1 min-w-0">
                <p className="font-semibold text-sm">{card.title}</p>
                <p className="text-xs text-muted-foreground">{card.description}</p>
              </div>
              <Button
                size="sm"
                onClick={() => navigate(card.href)}
                className="bg-white/10 text-foreground hover:bg-white/20 border-0"
              >
                {card.action}
                <ChevronRight className="w-3 h-3 ml-1" />
              </Button>
            </div>
          </div>
        ))}
      </motion.div>

      {/* Platform Health */}
      <motion.div variants={item} className="glass-card p-5">
        <div className="flex items-center gap-4">
          <div className="w-12 h-12 rounded-xl bg-gradient-to-br from-emerald-500 to-emerald-600 flex items-center justify-center shadow-lg">
            <CheckCircle className="w-6 h-6 text-white" />
          </div>
          <div className="flex-1">
            <p className="font-semibold text-sm">Platform Health</p>
            <p className="text-xs text-muted-foreground">All systems operational</p>
          </div>
          <Badge className="bg-emerald-500/20 text-emerald-400 border-emerald-500/30">
            <div className="w-1.5 h-1.5 rounded-full bg-emerald-400 animate-pulse mr-1.5" />
            Healthy
          </Badge>
        </div>
      </motion.div>
    </motion.div>
  );
};

export default AdminDashboardPage;
