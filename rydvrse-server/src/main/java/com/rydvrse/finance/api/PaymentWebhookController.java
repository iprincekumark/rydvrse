package com.rydvrse.finance.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.rydvrse.common.api.ApiResponse;
import com.rydvrse.finance.application.FinanceOperationsService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class PaymentWebhookController {

    private final FinanceOperationsService financeOperationsService;

    public PaymentWebhookController(FinanceOperationsService financeOperationsService) {
        this.financeOperationsService = financeOperationsService;
    }

    @PostMapping("/api/v1/webhooks/payments/{provider}")
    public ApiResponse<Map<String, Object>> paymentWebhook(
            @PathVariable String provider,
            @RequestHeader(name = "X-Provider-Signature", required = false) String signature,
            @RequestBody JsonNode payload
    ) {
        return ApiResponse.of(financeOperationsService.processWebhook(provider, signature, payload), null);
    }
}
