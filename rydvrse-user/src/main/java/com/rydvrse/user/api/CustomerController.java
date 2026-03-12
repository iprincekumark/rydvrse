package com.rydvrse.user.api;

import com.rydvrse.shared.dto.ApiResponse;
import com.rydvrse.user.dto.CustomerProfileRequest;
import com.rydvrse.user.dto.CustomerResponse;
import com.rydvrse.user.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<CustomerResponse>> getMyProfile(@AuthenticationPrincipal UUID authUserId) {
        return ResponseEntity.ok(ApiResponse.success(customerService.getProfileByAuthId(authUserId)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerResponse>> getProfile(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(customerService.getProfile(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerResponse>> updateProfile(
            @PathVariable UUID id, @RequestBody CustomerProfileRequest request) {
        return ResponseEntity.ok(ApiResponse.success(customerService.updateProfile(id, request)));
    }
}
