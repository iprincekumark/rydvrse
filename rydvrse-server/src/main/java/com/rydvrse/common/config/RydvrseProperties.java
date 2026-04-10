package com.rydvrse.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "rydvrse")
public class RydvrseProperties {

    private final Security security = new Security();
    private final Auth auth = new Auth();
    private final Pricing pricing = new Pricing();
    private final Payments payments = new Payments();
    private final Jobs jobs = new Jobs();
    private final Tracking tracking = new Tracking();

    public Security getSecurity() {
        return security;
    }

    public Auth getAuth() {
        return auth;
    }

    public Pricing getPricing() {
        return pricing;
    }

    public Payments getPayments() {
        return payments;
    }

    public Jobs getJobs() {
        return jobs;
    }

    public Tracking getTracking() {
        return tracking;
    }

    public static class Security {
        private String jwtSecret;
        private long accessTokenTtlMinutes;
        private long refreshTokenTtlDays;
        private String bootstrapAdminEmail;
        private String bootstrapAdminPassword;

        public String getJwtSecret() {
            return jwtSecret;
        }

        public void setJwtSecret(String jwtSecret) {
            this.jwtSecret = jwtSecret;
        }

        public long getAccessTokenTtlMinutes() {
            return accessTokenTtlMinutes;
        }

        public void setAccessTokenTtlMinutes(long accessTokenTtlMinutes) {
            this.accessTokenTtlMinutes = accessTokenTtlMinutes;
        }

        public long getRefreshTokenTtlDays() {
            return refreshTokenTtlDays;
        }

        public void setRefreshTokenTtlDays(long refreshTokenTtlDays) {
            this.refreshTokenTtlDays = refreshTokenTtlDays;
        }

        public String getBootstrapAdminEmail() {
            return bootstrapAdminEmail;
        }

        public void setBootstrapAdminEmail(String bootstrapAdminEmail) {
            this.bootstrapAdminEmail = bootstrapAdminEmail;
        }

        public String getBootstrapAdminPassword() {
            return bootstrapAdminPassword;
        }

        public void setBootstrapAdminPassword(String bootstrapAdminPassword) {
            this.bootstrapAdminPassword = bootstrapAdminPassword;
        }
    }

    public static class Auth {
        private String defaultOtpCode;
        private long otpExpiryMinutes;

        public String getDefaultOtpCode() {
            return defaultOtpCode;
        }

        public void setDefaultOtpCode(String defaultOtpCode) {
            this.defaultOtpCode = defaultOtpCode;
        }

        public long getOtpExpiryMinutes() {
            return otpExpiryMinutes;
        }

        public void setOtpExpiryMinutes(long otpExpiryMinutes) {
            this.otpExpiryMinutes = otpExpiryMinutes;
        }
    }

    public static class Pricing {
        private long quoteExpiryMinutes;

        public long getQuoteExpiryMinutes() {
            return quoteExpiryMinutes;
        }

        public void setQuoteExpiryMinutes(long quoteExpiryMinutes) {
            this.quoteExpiryMinutes = quoteExpiryMinutes;
        }
    }

    public static class Payments {
        private boolean sandboxAutoCapture;
        private String webhookSecret;

        public boolean isSandboxAutoCapture() {
            return sandboxAutoCapture;
        }

        public void setSandboxAutoCapture(boolean sandboxAutoCapture) {
            this.sandboxAutoCapture = sandboxAutoCapture;
        }

        public String getWebhookSecret() {
            return webhookSecret;
        }

        public void setWebhookSecret(String webhookSecret) {
            this.webhookSecret = webhookSecret;
        }
    }

    public static class Jobs {
        private boolean enabled;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    public static class Tracking {
        private long staleThresholdSeconds;

        public long getStaleThresholdSeconds() {
            return staleThresholdSeconds;
        }

        public void setStaleThresholdSeconds(long staleThresholdSeconds) {
            this.staleThresholdSeconds = staleThresholdSeconds;
        }
    }
}
