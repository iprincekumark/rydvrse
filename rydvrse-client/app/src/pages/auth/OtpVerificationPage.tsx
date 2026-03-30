/**
 * OTP Verification Page
 * Verifies the OTP sent to user's mobile number
 */

import React, { useState, useRef, useEffect } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { ArrowLeft, Loader2, RefreshCw } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { useAuth } from '@/hooks';
import { isValidOtp } from '@/utils/validators';
import { formatPhoneNumber } from '@/utils/formatters';

export const OtpVerificationPage: React.FC = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const { verifyOtp, sendOtp, isLoading } = useAuth();
  const [otp, setOtp] = useState(['', '', '', '', '', '']);
  const [error, setError] = useState('');
  const [resendTimer, setResendTimer] = useState(30);
  const inputRefs = useRef<(HTMLInputElement | null)[]>([]);

  const phoneNumber = location.state?.phoneNumber as string;

  // Redirect if no phone number
  useEffect(() => {
    if (!phoneNumber) {
      navigate('/login');
    }
  }, [phoneNumber, navigate]);

  // Resend timer countdown
  useEffect(() => {
    if (resendTimer > 0) {
      const timer = setTimeout(() => setResendTimer(resendTimer - 1), 1000);
      return () => clearTimeout(timer);
    }
  }, [resendTimer]);

  const handleChange = (index: number, value: string) => {
    // Only allow numbers
    if (!/^\d*$/.test(value)) return;

    const newOtp = [...otp];
    newOtp[index] = value.slice(-1); // Take only the last digit
    setOtp(newOtp);
    setError('');

    // Auto-focus next input
    if (value && index < 5) {
      inputRefs.current[index + 1]?.focus();
    }

    // Auto-submit when all digits are entered
    if (index === 5 && value) {
      const fullOtp = [...newOtp.slice(0, 5), value].join('');
      if (isValidOtp(fullOtp)) {
        handleSubmit(fullOtp);
      }
    }
  };

  const handleKeyDown = (index: number, e: React.KeyboardEvent) => {
    // Move to previous input on backspace
    if (e.key === 'Backspace' && !otp[index] && index > 0) {
      inputRefs.current[index - 1]?.focus();
    }
  };

  const handleSubmit = async (fullOtp: string) => {
    setError('');

    if (!isValidOtp(fullOtp)) {
      setError('Please enter a valid 6-digit OTP');
      return;
    }

    const success = await verifyOtp(phoneNumber, fullOtp);
    if (!success) {
      setError('Invalid OTP. Please try again.');
      setOtp(['', '', '', '', '', '']);
      inputRefs.current[0]?.focus();
    }
  };

  const handleResend = async () => {
    const success = await sendOtp(phoneNumber);
    if (success) {
      setResendTimer(30);
      setOtp(['', '', '', '', '', '']);
      inputRefs.current[0]?.focus();
    }
  };

  const handleBack = () => {
    navigate('/login');
  };

  return (
    <Card>
      <CardHeader className="space-y-1">
        <Button
          variant="ghost"
          size="sm"
          className="absolute left-4 top-4"
          onClick={handleBack}
        >
          <ArrowLeft className="h-4 w-4" />
        </Button>
        <CardTitle className="text-2xl text-center">Verify OTP</CardTitle>
        <CardDescription className="text-center">
          Enter the 6-digit code sent to{' '}
          <span className="font-medium">{formatPhoneNumber(phoneNumber)}</span>
        </CardDescription>
      </CardHeader>
      <CardContent>
        <div className="space-y-6">
          {/* OTP Input */}
          <div className="flex justify-center gap-2">
            {otp.map((digit, index) => (
              <input
                key={index}
                ref={(el) => { inputRefs.current[index] = el; }}
                type="text"
                inputMode="numeric"
                maxLength={1}
                value={digit}
                onChange={(e) => handleChange(index, e.target.value)}
                onKeyDown={(e) => handleKeyDown(index, e)}
                className="w-12 h-14 text-center text-2xl font-semibold border rounded-lg focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent"
                disabled={isLoading}
              />
            ))}
          </div>

          {error && (
            <p className="text-sm text-destructive text-center">{error}</p>
          )}

          {/* Verify Button */}
          <Button
            onClick={() => handleSubmit(otp.join(''))}
            className="w-full"
            disabled={isLoading || otp.some((d) => !d)}
          >
            {isLoading ? (
              <>
                <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                Verifying...
              </>
            ) : (
              'Verify & Continue'
            )}
          </Button>

          {/* Resend OTP */}
          <div className="text-center">
            {resendTimer > 0 ? (
              <p className="text-sm text-muted-foreground">
                Resend OTP in {resendTimer}s
              </p>
            ) : (
              <Button
                variant="ghost"
                size="sm"
                onClick={handleResend}
                disabled={isLoading}
              >
                <RefreshCw className="mr-2 h-4 w-4" />
                Resend OTP
              </Button>
            )}
          </div>
        </div>
      </CardContent>
    </Card>
  );
};

export default OtpVerificationPage;
