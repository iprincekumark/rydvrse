/**
 * Formatters Utility
 * Helper functions for formatting data
 */

import { format, formatDistanceToNow, parseISO } from 'date-fns';

/**
 * Format currency (INR)
 */
export const formatCurrency = (amount: number): string => {
  return new Intl.NumberFormat('en-IN', {
    style: 'currency',
    currency: 'INR',
    minimumFractionDigits: 0,
    maximumFractionDigits: 2,
  }).format(amount);
};

/**
 * Format distance (km)
 */
export const formatDistance = (km: number): string => {
  if (km < 1) {
    return `${Math.round(km * 1000)} m`;
  }
  return `${km.toFixed(1)} km`;
};

/**
 * Format duration (minutes)
 */
export const formatDuration = (minutes: number): string => {
  if (minutes < 60) {
    return `${Math.round(minutes)} min`;
  }
  const hours = Math.floor(minutes / 60);
  const mins = Math.round(minutes % 60);
  return `${hours}h ${mins}m`;
};

/**
 * Format phone number (Indian format)
 */
export const formatPhoneNumber = (phone: string): string => {
  if (phone.length === 10) {
    return `+91 ${phone.slice(0, 5)} ${phone.slice(5)}`;
  }
  if (phone.startsWith('91') && phone.length === 12) {
    return `+${phone.slice(0, 2)} ${phone.slice(2, 7)} ${phone.slice(7)}`;
  }
  if (phone.startsWith('+91')) {
    return phone;
  }
  return `+91 ${phone}`;
};

/**
 * Format date
 */
export const formatDate = (date: string | Date, pattern: string = 'dd MMM yyyy'): string => {
  const d = typeof date === 'string' ? parseISO(date) : date;
  return format(d, pattern);
};

/**
 * Format time
 */
export const formatTime = (date: string | Date, pattern: string = 'hh:mm a'): string => {
  const d = typeof date === 'string' ? parseISO(date) : date;
  return format(d, pattern);
};

/**
 * Format datetime
 */
export const formatDateTime = (date: string | Date): string => {
  const d = typeof date === 'string' ? parseISO(date) : date;
  return format(d, 'dd MMM yyyy, hh:mm a');
};

/**
 * Format relative time (e.g., "2 hours ago")
 */
export const formatRelativeTime = (date: string | Date): string => {
  const d = typeof date === 'string' ? parseISO(date) : date;
  return formatDistanceToNow(d, { addSuffix: true });
};

/**
 * Format rating
 */
export const formatRating = (rating: number): string => {
  return rating.toFixed(1);
};

/**
 * Format trip number
 */
export const formatTripNumber = (tripNumber: string): string => {
  return tripNumber.toUpperCase();
};

/**
 * Format vehicle info
 */
export const formatVehicleInfo = (
  make: string,
  model: string,
  color: string,
  registrationNumber: string
): string => {
  return `${color} ${make} ${model} (${registrationNumber})`;
};

/**
 * Truncate text
 */
export const truncateText = (text: string, maxLength: number): string => {
  if (text.length <= maxLength) return text;
  return `${text.slice(0, maxLength)}...`;
};

/**
 * Capitalize first letter
 */
export const capitalize = (text: string): string => {
  return text.charAt(0).toUpperCase() + text.slice(1).toLowerCase();
};

/**
 * Format status for display
 */
export const formatStatus = (status: string): string => {
  return status
    .split('_')
    .map((word) => capitalize(word))
    .join(' ');
};
