package com.rydvrse.auth.application;

import org.springframework.stereotype.Component;

@Component
public class MobileNumberNormalizer {

    public String normalize(String mobileNumber) {
        String digits = mobileNumber == null ? "" : mobileNumber.replaceAll("[^0-9+]", "");
        if (digits.isBlank()) {
            return digits;
        }
        if (digits.startsWith("+")) {
            return digits;
        }
        if (digits.length() == 10) {
            return "+91" + digits;
        }
        if (digits.startsWith("91") && digits.length() == 12) {
            return "+" + digits;
        }
        return digits.startsWith("+") ? digits : "+" + digits;
    }

    public String mask(String mobileNumber) {
        if (mobileNumber == null || mobileNumber.length() < 4) {
            return mobileNumber;
        }
        return mobileNumber.substring(0, Math.min(3, mobileNumber.length())) + "******" + mobileNumber.substring(mobileNumber.length() - 2);
    }
}
