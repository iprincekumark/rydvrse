/**
 * OTP Verification Page — Glowing OTP input with countdown timer
 */

import React, { useState, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { motion } from 'framer-motion';
import { ArrowLeft, Loader2, ShieldCheck } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { InputOTP, InputOTPGroup, InputOTPSlot, InputOTPSeparator } from '@/components/ui/input-otp';
import { useAuth } from '@/hooks';

export const OtpVerificationPage: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { verifyOtp, sendOtp, isLoading } = useAuth();
  const [otp, setOtp] = useState('');
  const [error, setError] = useState('');
  const [countdown, setCountdown] = useState(30);
  const [canResend, setCanResend] = useState(false);

  const phoneNumber = location.state?.phoneNumber;

  useEffect(() => {
    if (!phoneNumber) {
      navigate('/login');
    }
  }, [phoneNumber, navigate]);

  useEffect(() => {
    if (countdown > 0) {
      const timer = setTimeout(() => setCountdown(countdown - 1), 1000);
      return () => clearTimeout(timer);
    } else {
      setCanResend(true);
    }
  }, [countdown]);

  const handleVerify = async () => {
    setError('');
    if (otp.length !== 6) {
      setError('Please enter the 6-digit OTP');
      return;
    }
    const success = await verifyOtp(phoneNumber, otp);
    if (!success) {
      setError('Invalid OTP. Please try again.');
      setOtp('');
    }
  };

  const handleResend = async () => {
    if (!canResend) return;
    setCanResend(false);
    setCountdown(30);
    setOtp('');
    setError('');
    await sendOtp(phoneNumber);
  };

  useEffect(() => {
    if (otp.length === 6) {
      handleVerify();
    }
  }, [otp]);

  return (
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.5 }}
    >
      <div className="glass-card p-8">
        {/* Back button */}
        <button
          onClick={() => navigate('/login')}
          className="flex items-center gap-1 text-sm text-muted-foreground hover:text-foreground transition-colors mb-6"
        >
          <ArrowLeft className="h-4 w-4" />
          Back
        </button>

        {/* Icon */}
        <div className="flex justify-center mb-6">
          <div className="w-16 h-16 rounded-2xl gradient-primary flex items-center justify-center shadow-glow">
            <ShieldCheck className="h-8 w-8 text-white" />
          </div>
        </div>

        {/* Title */}
        <div className="text-center mb-8">
          <h2 className="text-xl font-semibold text-foreground mb-2">
            Verify your number
          </h2>
          <p className="text-sm text-muted-foreground">
            We sent a 6-digit code to{' '}
            <span className="text-foreground font-medium">+91 {phoneNumber}</span>
          </p>
        </div>

        {/* OTP Input */}
        <div className="flex justify-center mb-6">
          <InputOTP
            maxLength={6}
            value={otp}
            onChange={(value) => {
              setOtp(value);
              setError('');
            }}
            disabled={isLoading}
          >
            <InputOTPGroup>
              <InputOTPSlot index={0} className="w-12 h-14 text-lg bg-white/5 border-white/10 focus:border-primary" />
              <InputOTPSlot index={1} className="w-12 h-14 text-lg bg-white/5 border-white/10 focus:border-primary" />
              <InputOTPSlot index={2} className="w-12 h-14 text-lg bg-white/5 border-white/10 focus:border-primary" />
            </InputOTPGroup>
            <InputOTPSeparator />
            <InputOTPGroup>
              <InputOTPSlot index={3} className="w-12 h-14 text-lg bg-white/5 border-white/10 focus:border-primary" />
              <InputOTPSlot index={4} className="w-12 h-14 text-lg bg-white/5 border-white/10 focus:border-primary" />
              <InputOTPSlot index={5} className="w-12 h-14 text-lg bg-white/5 border-white/10 focus:border-primary" />
            </InputOTPGroup>
          </InputOTP>
        </div>

        {/* Error */}
        {error && (
          <motion.p
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            className="text-sm text-destructive text-center mb-4"
          >
            {error}
          </motion.p>
        )}

        {/* Loading indicator */}
        {isLoading && (
          <div className="flex items-center justify-center gap-2 mb-4 text-primary">
            <Loader2 className="h-4 w-4 animate-spin" />
            <span className="text-sm">Verifying...</span>
          </div>
        )}

        {/* Resend */}
        <div className="text-center">
          {canResend ? (
            <Button
              variant="ghost"
              onClick={handleResend}
              className="text-sm text-primary hover:text-primary/80"
            >
              Resend OTP
            </Button>
          ) : (
            <p className="text-sm text-muted-foreground">
              Resend code in{' '}
              <span className="text-foreground font-mono font-semibold">{countdown}s</span>
            </p>
          )}
        </div>
      </div>
    </motion.div>
  );
};

export default OtpVerificationPage;
