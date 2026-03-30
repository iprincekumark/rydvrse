package com.rydvrse.customer.controller;

import com.rydvrse.customer.dto.*;
import com.rydvrse.customer.entity.CustomerAddress;
import com.rydvrse.customer.service.CustomerService;
import com.rydvrse.shared.dto.ApiResponse;
import com.rydvrse.shared.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
@Tag(name = "Customer", description = "Customer profile and address management")
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping("/me")
    @Operation(summary = "Get own profile")
    public ResponseEntity<ApiResponse<CustomerResponse>> getProfile(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(customerService.getProfile(principal.getUserId())));
    }

    @PutMapping("/me")
    @Operation(summary = "Update profile")
    public ResponseEntity<ApiResponse<CustomerResponse>> updateProfile(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody UpdateCustomerRequest request) {
        return ResponseEntity.ok(ApiResponse.success(customerService.updateProfile(principal.getUserId(), request)));
    }

    @PostMapping("/me/addresses")
    @Operation(summary = "Add saved address")
    public ResponseEntity<ApiResponse<CustomerAddress>> addAddress(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody AddAddressRequest request) {
        return ResponseEntity.ok(ApiResponse.success(customerService.addAddress(principal.getUserId(), request)));
    }

    @GetMapping("/me/addresses")
    @Operation(summary = "List saved addresses")
    public ResponseEntity<ApiResponse<List<CustomerAddress>>> getAddresses(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(customerService.getAddresses(principal.getUserId())));
    }

    @DeleteMapping("/me/addresses/{id}")
    @Operation(summary = "Delete saved address")
    public ResponseEntity<ApiResponse<Void>> deleteAddress(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id) {
        customerService.deleteAddress(principal.getUserId(), id);
        return ResponseEntity.ok(ApiResponse.success("Address deleted", null));
    }
}
