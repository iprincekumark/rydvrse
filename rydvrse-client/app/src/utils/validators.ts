/**
 * Validators Utility
 * Helper functions for validation
 */

/**
 * Validate Indian phone number
 */
export const isValidPhoneNumber = (phone: string): boolean => {
  // Remove all non-numeric characters
  const cleanPhone = phone.replace(/\D/g, '');
  
  // Check if it's a valid 10-digit Indian number
  // Can start with 6, 7, 8, or 9
  const indianMobileRegex = /^[6-9]\d{9}$/;
  
  // Check if it includes country code
  const withCountryCodeRegex = /^91[6-9]\d{9}$/;
  
  return indianMobileRegex.test(cleanPhone) || withCountryCodeRegex.test(cleanPhone);
};

/**
 * Validate OTP (6 digits)
 */
export const isValidOtp = (otp: string): boolean => {
  return /^\d{6}$/.test(otp);
};

/**
 * Validate email
 */
export const isValidEmail = (email: string): boolean => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return emailRegex.test(email);
};

/**
 * Validate name (at least 2 characters)
 */
export const isValidName = (name: string): boolean => {
  return name.trim().length >= 2;
};

/**
 * Validate vehicle registration number (Indian format)
 */
export const isValidRegistrationNumber = (regNumber: string): boolean => {
  // Basic Indian registration number format: XX00XX0000 or XX00X0000
  const regRegex = /^[A-Z]{2}\d{2}[A-Z]{1,2}\d{4}$/;
  return regRegex.test(regNumber.toUpperCase());
};

/**
 * Validate coordinates
 */
export const isValidCoordinates = (lat: number, lng: number): boolean => {
  return lat >= -90 && lat <= 90 && lng >= -180 && lng <= 180;
};

/**
 * Validate PIN code (Indian format)
 */
export const isValidPincode = (pincode: string): boolean => {
  return /^\d{6}$/.test(pincode);
};

/**
 * Validate rating (1-5)
 */
export const isValidRating = (rating: number): boolean => {
  return rating >= 1 && rating <= 5;
};

/**
 * Validate required field
 */
export const isRequired = (value: string | number | null | undefined): boolean => {
  if (value === null || value === undefined) return false;
  if (typeof value === 'string') return value.trim().length > 0;
  return true;
};

/**
 * Validate minimum length
 */
export const minLength = (value: string, min: number): boolean => {
  return value.length >= min;
};

/**
 * Validate maximum length
 */
export const maxLength = (value: string, max: number): boolean => {
  return value.length <= max;
};

/**
 * Validate Aadhaar number (12 digits)
 */
export const isValidAadhaar = (aadhaar: string): boolean => {
  return /^\d{12}$/.test(aadhaar);
};

/**
 * Validate PAN card number
 */
export const isValidPanCard = (pan: string): boolean => {
  const panRegex = /^[A-Z]{5}\d{4}[A-Z]$/;
  return panRegex.test(pan.toUpperCase());
};

/**
 * Validate driving license number (basic format)
 */
export const isValidDrivingLicense = (license: string): boolean => {
  // Basic format: XX00 00000000000 (state code + number)
  const dlRegex = /^[A-Z]{2}\d{2}\s?\d{11,13}$/;
  return dlRegex.test(license.toUpperCase());
};

/**
 * Get validation error message
 */
export const getValidationError = (
  field: string,
  value: string,
  rules: { required?: boolean; minLength?: number; maxLength?: number }
): string | null => {
  if (rules.required && !isRequired(value)) {
    return `${field} is required`;
  }
  if (rules.minLength && !minLength(value, rules.minLength)) {
    return `${field} must be at least ${rules.minLength} characters`;
  }
  if (rules.maxLength && !maxLength(value, rules.maxLength)) {
    return `${field} must be at most ${rules.maxLength} characters`;
  }
  return null;
};
