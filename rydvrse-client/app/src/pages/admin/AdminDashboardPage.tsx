/**
 * Admin Dashboard Page
 * Main dashboard for administrators to monitor platform
 */

import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Users,
  Car,
  TrendingUp,
  Clock,
  AlertCircle,
  CheckCircle,
  DollarSign,
  Activity,
  ArrowUpRight,
} from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { adminService } from '@/services';
import { useUIStore } from '@/store';
import type { AdminDashboardStats } from '@/types';
import { formatCurrency } from '@/utils/formatters';

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
      } catch (error) {
        addToast({
          type: 'error',
          message: 'Failed to load dashboard statistics',
        });
      } finally {
        setIsLoading(false);
      }
    };

    loadStats();
  }, [addToast]);

  const statCards = [
    {
      title: 'Total Trips',
      value: stats?.totalTrips || 0,
      icon: Car,
      change: '+12%',
      trend: 'up',
      color: 'bg-blue-500',
    },
    {
      title: 'Total Drivers',
      value: stats?.totalDrivers || 0,
      icon: Users,
      change: '+5%',
      trend: 'up',
      color: 'bg-green-500',
    },
    {
      title: 'Total Customers',
      value: stats?.totalCustomers || 0,
      icon: Users,
      change: '+8%',
      trend: 'up',
      color: 'bg-purple-500',
    },
    {
      title: 'Total Revenue',
      value: formatCurrency(stats?.totalRevenue || 0),
      icon: DollarSign,
      change: '+15%',
      trend: 'up',
      color: 'bg-yellow-500',
    },
  ];

  const quickStats = [
    {
      label: 'Active Trips',
      value: stats?.activeTrips || 0,
      icon: Activity,
    },
    {
      label: 'Pending Approvals',
      value: stats?.pendingDriverApprovals || 0,
      icon: AlertCircle,
      action: () => navigate('/admin/drivers'),
    },
    {
      label: "Today's Trips",
      value: stats?.todayTrips || 0,
      icon: Clock,
    },
    {
      label: "Today's Revenue",
      value: formatCurrency(stats?.todayRevenue || 0),
      icon: TrendingUp,
    },
  ];

  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary" />
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div>
        <h1 className="text-2xl font-bold">Dashboard</h1>
        <p className="text-muted-foreground">
          Welcome to RYDVRSE Admin Panel
        </p>
      </div>

      {/* Main Stats */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        {statCards.map((card) => (
          <Card key={card.title}>
            <CardContent className="p-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-muted-foreground">{card.title}</p>
                  <p className="text-2xl font-bold mt-1">{card.value}</p>
                  <div className="flex items-center gap-1 mt-2">
                    <ArrowUpRight className="w-4 h-4 text-green-500" />
                    <span className="text-sm text-green-500">
                      {card.change}
                    </span>
                  </div>
                </div>
                <div className={`w-12 h-12 rounded-full ${card.color} flex items-center justify-center`}>
                  <card.icon className="w-6 h-6 text-white" />
                </div>
              </div>
            </CardContent>
          </Card>
        ))}
      </div>

      {/* Quick Stats */}
      <Card>
        <CardHeader>
          <CardTitle>Quick Overview</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
            {quickStats.map((stat) => (
              <button
                key={stat.label}
                onClick={stat.action}
                className={`p-4 rounded-lg bg-muted/50 ${stat.action ? 'hover:bg-muted cursor-pointer' : ''}`}
              >
                <stat.icon className="w-5 h-5 text-muted-foreground mb-2" />
                <p className="text-lg font-semibold">{stat.value}</p>
                <p className="text-sm text-muted-foreground">{stat.label}</p>
              </button>
            ))}
          </div>
        </CardContent>
      </Card>

      {/* Actions */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <Card>
          <CardContent className="p-6">
            <div className="flex items-center gap-4">
              <div className="w-12 h-12 rounded-full bg-orange-500 flex items-center justify-center">
                <AlertCircle className="w-6 h-6 text-white" />
              </div>
              <div className="flex-1">
                <p className="font-medium">Driver Approvals</p>
                <p className="text-sm text-muted-foreground">
                  {stats?.pendingDriverApprovals || 0} pending approvals
                </p>
              </div>
              <Button onClick={() => navigate('/admin/drivers')}>
                Review
              </Button>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="p-6">
            <div className="flex items-center gap-4">
              <div className="w-12 h-12 rounded-full bg-blue-500 flex items-center justify-center">
                <Car className="w-6 h-6 text-white" />
              </div>
              <div className="flex-1">
                <p className="font-medium">Active Trips</p>
                <p className="text-sm text-muted-foreground">
                  {stats?.activeTrips || 0} trips in progress
                </p>
              </div>
              <Button variant="outline" onClick={() => navigate('/admin/trips')}>
                Monitor
              </Button>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="p-6">
            <div className="flex items-center gap-4">
              <div className="w-12 h-12 rounded-full bg-green-500 flex items-center justify-center">
                <CheckCircle className="w-6 h-6 text-white" />
              </div>
              <div className="flex-1">
                <p className="font-medium">Platform Health</p>
                <p className="text-sm text-muted-foreground">
                  All systems operational
                </p>
              </div>
              <Badge variant="default">Healthy</Badge>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
};

export default AdminDashboardPage;
