/**
 * Login Page
 * OTP-based authentication for all user types
 */

import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Phone, ArrowRight, Loader2 } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { useAuth } from '@/hooks';
import { isValidPhoneNumber } from '@/utils/validators';

export const LoginPage: React.FC = () => {
  const navigate = useNavigate();
  const { sendOtp, isLoading } = useAuth();
  const [phoneNumber, setPhoneNumber] = useState('');
  const [error, setError] = useState('');

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');

    // Validate phone number
    if (!isValidPhoneNumber(phoneNumber)) {
      setError('Please enter a valid 10-digit mobile number');
      return;
    }

    // Format phone number (remove non-numeric characters)
    const formattedPhone = phoneNumber.replace(/\D/g, '');
    const cleanPhone = formattedPhone.length === 12 && formattedPhone.startsWith('91')
      ? formattedPhone.slice(2)
      : formattedPhone;

    const success = await sendOtp(cleanPhone);
    if (success) {
      navigate('/verify-otp', { state: { phoneNumber: cleanPhone } });
    }
  };

  return (
    <Card>
      <CardHeader className="space-y-1">
        <CardTitle className="text-2xl text-center">Welcome to RYDVRSE</CardTitle>
        <CardDescription className="text-center">
          Enter your mobile number to continue
        </CardDescription>
      </CardHeader>
      <CardContent>
        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="phone">Mobile Number</Label>
            <div className="relative">
              <Phone className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground" />
              <Input
                id="phone"
                type="tel"
                placeholder="Enter 10-digit mobile number"
                value={phoneNumber}
                onChange={(e) => setPhoneNumber(e.target.value)}
                className="pl-10"
                maxLength={10}
                disabled={isLoading}
              />
            </div>
            {error && (
              <p className="text-sm text-destructive">{error}</p>
            )}
          </div>

          <Button
            type="submit"
            className="w-full"
            disabled={isLoading || phoneNumber.length < 10}
          >
            {isLoading ? (
              <>
                <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                Sending OTP...
              </>
            ) : (
              <>
                Continue
                <ArrowRight className="ml-2 h-4 w-4" />
              </>
            )}
          </Button>
        </form>

        <div className="mt-6 text-center">
          <p className="text-sm text-muted-foreground">
            By continuing, you agree to receive an OTP on your mobile number
          </p>
        </div>
      </CardContent>
    </Card>
  );
};

export default LoginPage;
