package com.rydvrse.auth.application;

public record SessionTokens(String accessToken, String refreshToken, long expiresInSeconds) {
}
