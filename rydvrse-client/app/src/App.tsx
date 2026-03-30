/**
 * RYDVRSE Application
 * Main application component with routing configuration
 */

import { Suspense, lazy } from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';

// Error Boundary
import { ErrorBoundary } from '@/components/common/ErrorBoundary';

// Layouts
import { MainLayout, AuthLayout } from '@/layouts';

// Route Guards
import {
  CustomerRoute,
  DriverRoute,
  AdminRoute,
  PublicRoute,
} from '@/components/common/ProtectedRoute';

// Loading Fallback
const LoadingFallback = () => (
  <div style={{
    minHeight: '100vh',
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'center',
    justifyContent: 'center',
    gap: '1rem',
    background: 'hsl(240 6% 4%)',
  }}>
    <div style={{
      width: '40px',
      height: '40px',
      border: '3px solid rgba(255,255,255,0.1)',
      borderTop: '3px solid hsl(217 91% 60%)',
      borderRadius: '50%',
      animation: 'spin 0.8s linear infinite',
    }} />
    <p style={{ color: 'rgba(255,255,255,0.4)', fontSize: '0.875rem', letterSpacing: '0.05em' }}>Loading RYDVRSE...</p>
    <style>{`@keyframes spin { to { transform: rotate(360deg); } }`}</style>
  </div>
);

// Lazy-loaded Pages (prevents import chain crashes from blocking the entire app)
const LoginPage = lazy(() => import('@/pages/auth/LoginPage'));
const OtpVerificationPage = lazy(() => import('@/pages/auth/OtpVerificationPage'));
const CustomerDashboardPage = lazy(() => import('@/pages/customer/CustomerDashboardPage'));
const BookRidePage = lazy(() => import('@/pages/customer/BookRidePage'));
const TripTrackingPage = lazy(() => import('@/pages/customer/TripTrackingPage'));
const DriverDashboardPage = lazy(() => import('@/pages/driver/DriverDashboardPage'));
const AdminDashboardPage = lazy(() => import('@/pages/admin/AdminDashboardPage'));

// Create Query Client
const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: 1,
      refetchOnWindowFocus: false,
    },
  },
});

function App() {
  return (
    <ErrorBoundary>
      <QueryClientProvider client={queryClient}>
        <BrowserRouter>
          <Suspense fallback={<LoadingFallback />}>
            <Routes>
              {/* Public Routes */}
              <Route element={<AuthLayout />}>
                <Route
                  path="/login"
                  element={
                    <PublicRoute>
                      <LoginPage />
                    </PublicRoute>
                  }
                />
                <Route
                  path="/verify-otp"
                  element={
                    <PublicRoute>
                      <OtpVerificationPage />
                    </PublicRoute>
                  }
                />
              </Route>

              {/* Protected Routes */}
              <Route element={<MainLayout />}>
                {/* Customer Routes */}
                <Route
                  path="/customer/dashboard"
                  element={
                    <CustomerRoute>
                      <CustomerDashboardPage />
                    </CustomerRoute>
                  }
                />
                <Route
                  path="/customer/book"
                  element={
                    <CustomerRoute>
                      <BookRidePage />
                    </CustomerRoute>
                  }
                />
                <Route
                  path="/customer/trips"
                  element={
                    <CustomerRoute>
                      <div className="p-6 text-center text-muted-foreground">My Trips Page (Coming Soon)</div>
                    </CustomerRoute>
                  }
                />
                <Route
                  path="/customer/trips/:tripId"
                  element={
                    <CustomerRoute>
                      <TripTrackingPage />
                    </CustomerRoute>
                  }
                />
                <Route
                  path="/customer/vehicles"
                  element={
                    <CustomerRoute>
                      <div className="p-6 text-center text-muted-foreground">Vehicles Page (Coming Soon)</div>
                    </CustomerRoute>
                  }
                />
                <Route
                  path="/customer/wallet"
                  element={
                    <CustomerRoute>
                      <div className="p-6 text-center text-muted-foreground">Wallet Page (Coming Soon)</div>
                    </CustomerRoute>
                  }
                />
                <Route
                  path="/customer/profile"
                  element={
                    <CustomerRoute>
                      <div className="p-6 text-center text-muted-foreground">Profile Page (Coming Soon)</div>
                    </CustomerRoute>
                  }
                />

                {/* Driver Routes */}
                <Route
                  path="/driver/dashboard"
                  element={
                    <DriverRoute>
                      <DriverDashboardPage />
                    </DriverRoute>
                  }
                />
                <Route
                  path="/driver/trips"
                  element={
                    <DriverRoute>
                      <div className="p-6 text-center text-muted-foreground">Driver Trips Page (Coming Soon)</div>
                    </DriverRoute>
                  }
                />
                <Route
                  path="/driver/earnings"
                  element={
                    <DriverRoute>
                      <div className="p-6 text-center text-muted-foreground">Earnings Page (Coming Soon)</div>
                    </DriverRoute>
                  }
                />
                <Route
                  path="/driver/documents"
                  element={
                    <DriverRoute>
                      <div className="p-6 text-center text-muted-foreground">Documents Page (Coming Soon)</div>
                    </DriverRoute>
                  }
                />
                <Route
                  path="/driver/profile"
                  element={
                    <DriverRoute>
                      <div className="p-6 text-center text-muted-foreground">Driver Profile Page (Coming Soon)</div>
                    </DriverRoute>
                  }
                />

                {/* Admin Routes */}
                <Route
                  path="/admin/dashboard"
                  element={
                    <AdminRoute>
                      <AdminDashboardPage />
                    </AdminRoute>
                  }
                />
                <Route
                  path="/admin/drivers"
                  element={
                    <AdminRoute>
                      <div className="p-6 text-center text-muted-foreground">Driver Management Page (Coming Soon)</div>
                    </AdminRoute>
                  }
                />
                <Route
                  path="/admin/trips"
                  element={
                    <AdminRoute>
                      <div className="p-6 text-center text-muted-foreground">Trip Management Page (Coming Soon)</div>
                    </AdminRoute>
                  }
                />
                <Route
                  path="/admin/safety"
                  element={
                    <AdminRoute>
                      <div className="p-6 text-center text-muted-foreground">Safety Monitoring Page (Coming Soon)</div>
                    </AdminRoute>
                  }
                />
                <Route
                  path="/admin/settings"
                  element={
                    <AdminRoute>
                      <div className="p-6 text-center text-muted-foreground">Admin Settings Page (Coming Soon)</div>
                    </AdminRoute>
                  }
                />
              </Route>

              {/* Default Redirect */}
              <Route path="/" element={<Navigate to="/login" replace />} />
              <Route path="*" element={<Navigate to="/login" replace />} />
            </Routes>
          </Suspense>
        </BrowserRouter>
      </QueryClientProvider>
    </ErrorBoundary>
  );
}

export default App;
