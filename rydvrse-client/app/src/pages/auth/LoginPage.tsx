/**
 * Login Page — Premium dark theme with Spotlight + FlipWords
 */

import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import { Phone, ArrowRight, Loader2 } from 'lucide-react';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { useAuth } from '@/hooks';
import { isValidPhoneNumber } from '@/utils/validators';
import { FlipWords } from '@/components/aceternity/flip-words';
import { MovingBorder } from '@/components/aceternity/moving-border';

export const LoginPage: React.FC = () => {
  const navigate = useNavigate();
  const { sendOtp, isLoading } = useAuth();
  const [phoneNumber, setPhoneNumber] = useState('');
  const [error, setError] = useState('');

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');

    if (!isValidPhoneNumber(phoneNumber)) {
      setError('Please enter a valid 10-digit mobile number');
      return;
    }

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
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.5 }}
    >
      <div className="glass-card p-8">
        {/* Tagline */}
        <div className="text-center mb-8">
          <h2 className="text-xl font-semibold text-foreground mb-2">
            Welcome to the future of
          </h2>
          <div className="h-8 flex items-center justify-center">
            <FlipWords
              words={["personal driving", "safe commuting", "driver hiring", "car travel"]}
              className="text-xl font-bold gradient-text"
              duration={2500}
            />
          </div>
        </div>

        {/* Phone Form */}
        <form onSubmit={handleSubmit} className="space-y-5">
          <div className="space-y-2">
            <Label htmlFor="phone" className="text-sm text-muted-foreground">
              Mobile Number
            </Label>
            <div className="relative group">
              <div className="absolute left-3 top-1/2 -translate-y-1/2 flex items-center gap-1.5">
                <span className="text-sm text-muted-foreground font-medium">+91</span>
                <div className="h-4 w-px bg-border" />
              </div>
              <Phone className="absolute right-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground group-focus-within:text-primary transition-colors" />
              <Input
                id="phone"
                type="tel"
                placeholder="Enter 10-digit number"
                value={phoneNumber}
                onChange={(e) => setPhoneNumber(e.target.value.replace(/\D/g, ''))}
                className="pl-14 pr-10 h-12 bg-white/5 border-white/10 focus:border-primary/50 focus:ring-primary/20 text-foreground placeholder:text-muted-foreground/50 transition-all"
                maxLength={10}
                disabled={isLoading}
              />
            </div>
            {error && (
              <motion.p
                initial={{ opacity: 0, y: -4 }}
                animate={{ opacity: 1, y: 0 }}
                className="text-sm text-destructive flex items-center gap-1"
              >
                {error}
              </motion.p>
            )}
          </div>

          {/* CTA Button */}
          <MovingBorder
            as="div"
            duration={3000}
            containerClassName="w-full h-12 rounded-xl"
            className="bg-background hover:bg-white/5 transition-colors cursor-pointer"
          >
            <button
              type="submit"
              disabled={isLoading || phoneNumber.length < 10}
              className="w-full h-full flex items-center justify-center gap-2 text-sm font-semibold text-foreground disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {isLoading ? (
                <>
                  <Loader2 className="h-4 w-4 animate-spin" />
                  Sending OTP...
                </>
              ) : (
                <>
                  Continue
                  <ArrowRight className="h-4 w-4" />
                </>
              )}
            </button>
          </MovingBorder>
        </form>

        {/* Footer */}
        <p className="mt-6 text-center text-xs text-muted-foreground/60">
          By continuing, you agree to receive an OTP on your mobile number
        </p>
      </div>
    </motion.div>
  );
};

export default LoginPage;
