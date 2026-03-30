/**
 * RYDVRSE Type Definitions
 * Core types for the On-Demand Driver Marketplace
 */

// ============================================
// User & Authentication Types
// ============================================

export const UserRole = {
  CUSTOMER: 'CUSTOMER',
  DRIVER: 'DRIVER',
  ADMIN: 'ADMIN',
  OPERATIONS: 'OPERATIONS',
} as const;

export type UserRole = typeof UserRole[keyof typeof UserRole];

export const UserStatus = {
  ACTIVE: 'ACTIVE',
  INACTIVE: 'INACTIVE',
  SUSPENDED: 'SUSPENDED',
  PENDING_VERIFICATION: 'PENDING_VERIFICATION',
} as const;

export type UserStatus = typeof UserStatus[keyof typeof UserStatus];

export interface User {
  id: string;
  phoneNumber: string;
  email?: string;
  firstName: string;
  lastName: string;
  role: UserRole;
  status: UserStatus;
  profileImageUrl?: string;
  createdAt: string;
  updatedAt: string;
  lastLoginAt?: string;
}

export interface AuthTokens {
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
}

export interface AuthResponse {
  user: User;
  tokens: AuthTokens;
  isNewUser: boolean;
}

export interface OtpRequest {
  phone: string;       // format: +91XXXXXXXXXX
  userType: 'CUSTOMER' | 'DRIVER' | 'ADMIN';
}

export interface OtpResponse {
  otpSent: boolean;
  message: string;
  expiresIn: number;
}

export interface VerifyOtpRequest {
  phone: string;       // format: +91XXXXXXXXXX
  otp: string;
  userType: 'CUSTOMER' | 'DRIVER' | 'ADMIN';
}

export interface RefreshTokenRequest {
  refreshToken: string;
}

// ============================================
// Trip Types
// ============================================

export const TripStatus = {
  REQUESTED: 'REQUESTED',
  DRIVER_MATCHING: 'DRIVER_MATCHING',
  DRIVER_ASSIGNED: 'DRIVER_ASSIGNED',
  DRIVER_ARRIVING: 'DRIVER_ARRIVING',
  DRIVER_ARRIVED: 'DRIVER_ARRIVED',
  TRIP_STARTED: 'TRIP_STARTED',
  TRIP_COMPLETED: 'TRIP_COMPLETED',
  CANCELLED: 'CANCELLED',
} as const;

export type TripStatus = typeof TripStatus[keyof typeof TripStatus];

export const CancellationReason = {
  DRIVER_NOT_FOUND: 'DRIVER_NOT_FOUND',
  DRIVER_ARRIVED_LATE: 'DRIVER_ARRIVED_LATE',
  CUSTOMER_NOT_AVAILABLE: 'CUSTOMER_NOT_AVAILABLE',
  WRONG_PICKUP_LOCATION: 'WRONG_PICKUP_LOCATION',
  VEHICLE_ISSUE: 'VEHICLE_ISSUE',
  CHANGE_OF_PLANS: 'CHANGE_OF_PLANS',
  OTHER: 'OTHER',
} as const;

export type CancellationReason = typeof CancellationReason[keyof typeof CancellationReason];

export interface GeoLocation {
  latitude: number;
  longitude: number;
  address?: string;
  city?: string;
  pincode?: string;
}

export interface Trip {
  id: string;
  tripNumber: string;
  customerId: string;
  driverId?: string;
  vehicleId?: string;
  status: TripStatus;
  pickupLocation: GeoLocation;
  dropLocation: GeoLocation;
  estimatedFare?: number;
  finalFare?: number;
  surgeMultiplier?: number;
  estimatedDistanceKm?: number;
  estimatedDurationMin?: number;
  actualDistanceKm?: number;
  actualDurationMin?: number;
  startOtp?: string;
  rating?: number;
  feedback?: string;
  cancellationReason?: CancellationReason;
  cancelledBy?: string;
  createdAt: string;
  updatedAt: string;
  startedAt?: string;
  completedAt?: string;
  cancelledAt?: string;
}

export interface CreateTripRequest {
  customerId: string;
  vehicleId?: string;
  pickupLat: number;
  pickupLng: number;
  pickupAddress?: string;
  pickupCity?: string;
  dropLat: number;
  dropLng: number;
  dropAddress?: string;
  dropCity?: string;
  estimatedFare?: number;
  surgeMultiplier?: number;
}

export interface RateTripRequest {
  rating: number;
  feedback?: string;
}

export interface TripCancelRequest {
  reason: string;
  cancelledBy: string;
}

// ============================================
// Driver Types
// ============================================

export const DriverStatus = {
  PENDING_VERIFICATION: 'PENDING_VERIFICATION',
  UNDER_REVIEW: 'UNDER_REVIEW',
  APPROVED: 'APPROVED',
  REJECTED: 'REJECTED',
  SUSPENDED: 'SUSPENDED',
  ACTIVE: 'ACTIVE',
  INACTIVE: 'INACTIVE',
} as const;

export type DriverStatus = typeof DriverStatus[keyof typeof DriverStatus];

export const DriverAvailability = {
  ONLINE: 'ONLINE',
  OFFLINE: 'OFFLINE',
  ON_TRIP: 'ON_TRIP',
} as const;

export type DriverAvailability = typeof DriverAvailability[keyof typeof DriverAvailability];

export const DocumentType = {
  DRIVING_LICENSE: 'DRIVING_LICENSE',
  AADHAAR_CARD: 'AADHAAR_CARD',
  PAN_CARD: 'PAN_CARD',
  VEHICLE_REGISTRATION: 'VEHICLE_REGISTRATION',
  INSURANCE: 'INSURANCE',
  POLICE_VERIFICATION: 'POLICE_VERIFICATION',
  PROFILE_PHOTO: 'PROFILE_PHOTO',
} as const;

export type DocumentType = typeof DocumentType[keyof typeof DocumentType];

export const DocumentStatus = {
  PENDING: 'PENDING',
  VERIFIED: 'VERIFIED',
  REJECTED: 'REJECTED',
} as const;

export type DocumentStatus = typeof DocumentStatus[keyof typeof DocumentStatus];

export interface DriverDocument {
  id: string;
  driverId: string;
  documentType: DocumentType;
  documentUrl: string;
  documentNumber?: string;
  status: DocumentStatus;
  verifiedAt?: string;
  verifiedBy?: string;
  rejectionReason?: string;
  uploadedAt: string;
}

export interface Driver {
  id: string;
  userId: string;
  status: DriverStatus;
  availability: DriverAvailability;
  currentLocation?: GeoLocation;
  rating?: number;
  totalTrips: number;
  totalEarnings: number;
  drivingExperience?: number;
  languages?: string[];
  documents?: DriverDocument[];
  createdAt: string;
  updatedAt: string;
}

export interface DriverProfile extends Driver {
  user: User;
}

export interface DriverAvailabilityRequest {
  available: boolean;
}

export interface DocumentUploadRequest {
  documentType: DocumentType;
  documentUrl: string;
  documentNumber?: string;
}

// ============================================
// Vehicle Types
// ============================================

export const VehicleType = {
  HATCHBACK: 'HATCHBACK',
  SEDAN: 'SEDAN',
  SUV: 'SUV',
  LUXURY: 'LUXURY',
} as const;

export type VehicleType = typeof VehicleType[keyof typeof VehicleType];

export interface Vehicle {
  id: string;
  customerId: string;
  type: VehicleType;
  make: string;
  model: string;
  year: number;
  color: string;
  registrationNumber: string;
  isDefault: boolean;
  createdAt: string;
  updatedAt: string;
}

// ============================================
// Customer Types
// ============================================

export interface Customer {
  id: string;
  userId: string;
  rating?: number;
  totalTrips: number;
  preferredLanguage?: string;
  emergencyContact?: string;
  createdAt: string;
  updatedAt: string;
}

export interface CustomerProfile extends Customer {
  user: User;
  vehicles: Vehicle[];
  savedLocations: SavedLocation[];
}

export interface SavedLocation {
  id: string;
  customerId: string;
  name: string;
  location: GeoLocation;
  type: 'HOME' | 'WORK' | 'OTHER';
  createdAt: string;
}

// ============================================
// Location & Tracking Types
// ============================================

export interface DriverLocation {
  driverId: string;
  tripId?: string;
  latitude: number;
  longitude: number;
  heading?: number;
  speed?: number;
  accuracy?: number;
  timestamp: string;
}

export interface LocationUpdate {
  latitude: number;
  longitude: number;
  heading?: number;
  speed?: number;
}

// ============================================
// Payment & Wallet Types
// ============================================

export const PaymentStatus = {
  PENDING: 'PENDING',
  COMPLETED: 'COMPLETED',
  FAILED: 'FAILED',
  REFUNDED: 'REFUNDED',
} as const;

export type PaymentStatus = typeof PaymentStatus[keyof typeof PaymentStatus];

export const PaymentMethod = {
  CASH: 'CASH',
  WALLET: 'WALLET',
  UPI: 'UPI',
  CARD: 'CARD',
} as const;

export type PaymentMethod = typeof PaymentMethod[keyof typeof PaymentMethod];

export interface Payment {
  id: string;
  tripId: string;
  amount: number;
  method: PaymentMethod;
  status: PaymentStatus;
  transactionId?: string;
  createdAt: string;
  completedAt?: string;
}

export interface Wallet {
  id: string;
  userId: string;
  balance: number;
  currency: string;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface WalletTransaction {
  id: string;
  walletId: string;
  amount: number;
  type: 'CREDIT' | 'DEBIT';
  description: string;
  referenceId?: string;
  createdAt: string;
}

// ============================================
// Earnings Types
// ============================================

export interface DriverEarnings {
  driverId: string;
  totalEarnings: number;
  totalTrips: number;
  dailyEarnings: DailyEarning[];
  weeklyEarnings: WeeklyEarning[];
}

export interface DailyEarning {
  date: string;
  earnings: number;
  trips: number;
  incentives: number;
}

export interface WeeklyEarning {
  weekStart: string;
  weekEnd: string;
  earnings: number;
  trips: number;
  incentives: number;
}

// ============================================
// Admin Types
// ============================================

export interface AdminDashboardStats {
  totalTrips: number;
  totalDrivers: number;
  totalCustomers: number;
  activeTrips: number;
  pendingDriverApprovals: number;
  totalRevenue: number;
  todayTrips: number;
  todayRevenue: number;
}

export interface DriverApprovalRequest {
  driverId: string;
  status: DriverStatus;
  rejectionReason?: string;
}

// ============================================
// API Response Types
// ============================================

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp: string;
}

export interface PagedResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}

export interface ApiError {
  code: string;
  message: string;
  details?: Record<string, string[]>;
  timestamp: string;
}

// ============================================
// WebSocket Types
// ============================================

export interface WebSocketMessage<T> {
  type: string;
  payload: T;
  timestamp: string;
}

export interface TripLocationUpdate {
  tripId: string;
  driverId: string;
  location: DriverLocation;
}

// ============================================
// UI Types
// ============================================

export interface NavItem {
  label: string;
  href: string;
  icon?: string;
  roles?: UserRole[];
}

export interface Toast {
  id: string;
  type: 'success' | 'error' | 'warning' | 'info';
  message: string;
  duration?: number;
}

export interface LoadingState {
  isLoading: boolean;
  error: string | null;
}
