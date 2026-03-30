package com.rydvrse.payment.controller;

import com.rydvrse.payment.entity.Payment;
import com.rydvrse.payment.entity.Wallet;
import com.rydvrse.payment.service.PaymentService;
import com.rydvrse.shared.dto.ApiResponse;
import com.rydvrse.shared.enums.PaymentMethod;
import com.rydvrse.shared.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@RestController @RequestMapping("/api/v1/payments") @RequiredArgsConstructor
@Tag(name = "Payment", description = "Payment processing, wallet, refunds")
public class PaymentController {
    private final PaymentService paymentService;

    @GetMapping("/{id}")
    @Operation(summary = "Get payment status")
    public ResponseEntity<ApiResponse<Payment>> getPayment(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(paymentService.getPayment(id)));
    }

    @PostMapping("/webhook")
    @Operation(summary = "Razorpay webhook")
    public ResponseEntity<ApiResponse<Void>> webhook(@RequestBody Map<String, Object> payload) {
        // Verify signature + process webhook in production
        return ResponseEntity.ok(ApiResponse.success("Webhook received", null));
    }

    @PostMapping("/wallet/topup")
    @Operation(summary = "Add money to wallet")
    public ResponseEntity<ApiResponse<Wallet>> getWallet(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(paymentService.getWallet(principal.getUserId())));
    }
}
