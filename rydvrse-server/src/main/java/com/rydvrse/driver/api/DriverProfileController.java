package com.rydvrse.driver.api;

import com.rydvrse.common.api.ApiResponse;
import com.rydvrse.driver.application.DriverAvailabilityService;
import com.rydvrse.driver.application.DriverDashboardQueryService;
import com.rydvrse.driver.application.DriverDocumentService;
import com.rydvrse.driver.application.DriverOnboardingService;
import com.rydvrse.driver.application.DriverProfileService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/drivers")
public class DriverProfileController {

    private final DriverProfileService driverProfileService;
    private final DriverOnboardingService driverOnboardingService;
    private final DriverDocumentService driverDocumentService;
    private final DriverAvailabilityService driverAvailabilityService;
    private final DriverDashboardQueryService driverDashboardQueryService;

    public DriverProfileController(
            DriverProfileService driverProfileService,
            DriverOnboardingService driverOnboardingService,
            DriverDocumentService driverDocumentService,
            DriverAvailabilityService driverAvailabilityService,
            DriverDashboardQueryService driverDashboardQueryService
    ) {
        this.driverProfileService = driverProfileService;
        this.driverOnboardingService = driverOnboardingService;
        this.driverDocumentService = driverDocumentService;
        this.driverAvailabilityService = driverAvailabilityService;
        this.driverDashboardQueryService = driverDashboardQueryService;
    }

    @GetMapping("/me")
    public ApiResponse<Map<String, Object>> me() {
        return ApiResponse.of(driverProfileService.toResponse(driverProfileService.requireCurrentProfile()), null);
    }

    @PatchMapping("/me")
    public ApiResponse<Map<String, Object>> update(@Valid @RequestBody DriverProfilePatchRequest request) {
        return ApiResponse.of(driverProfileService.updateProfile(new DriverProfileService.DriverProfilePatch(
                request.fullName(),
                request.email(),
                request.languages(),
                request.emergencyContact() == null ? null : new DriverProfileService.EmergencyContact(
                        request.emergencyContact().name(),
                        request.emergencyContact().mobileNumber()
                )
        )), null);
    }

    @GetMapping("/me/dashboard")
    public ApiResponse<Map<String, Object>> dashboard() {
        return ApiResponse.of(driverDashboardQueryService.getDashboard(), null);
    }

    @PutMapping("/onboarding")
    public ApiResponse<Map<String, Object>> onboarding(@Valid @RequestBody DriverOnboardingRequest request) {
        return ApiResponse.of(driverOnboardingService.submit(new DriverOnboardingService.OnboardingCommand(
                request.fullName(),
                request.dateOfBirth(),
                request.cityId(),
                request.languages(),
                request.licenseNumber(),
                request.aadhaarNumberMaskedOrTokenized(),
                request.panNumberMaskedOrTokenized(),
                request.bankAccount().accountHolderName(),
                request.bankAccount().bankName(),
                request.bankAccount().accountNumberMasked(),
                request.bankAccount().ifscCode(),
                request.emergencyContact().name(),
                request.emergencyContact().mobileNumber()
        )), null);
    }

    @GetMapping("/onboarding/status")
    public ApiResponse<Map<String, Object>> onboardingStatus() {
        return ApiResponse.of(driverOnboardingService.getStatus(), null);
    }

    @PostMapping("/documents")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Map<String, Object>> createDocument(@Valid @RequestBody DriverDocumentRequest request) {
        return ApiResponse.of(driverDocumentService.create(new DriverDocumentService.DocumentCommand(
                request.documentType(),
                request.mediaAssetId(),
                request.documentNumber(),
                request.expiresOn()
        )), null);
    }

    @GetMapping("/documents")
    public ApiResponse<List<Map<String, Object>>> listDocuments() {
        return ApiResponse.of(driverDocumentService.list(), null);
    }

    @PatchMapping("/me/availability")
    public ApiResponse<Map<String, Object>> updateAvailability(@Valid @RequestBody AvailabilityRequest request) {
        return ApiResponse.of(driverAvailabilityService.update(request.status(), request.reasonCode()), null);
    }

    @GetMapping("/jobs/upcoming")
    public ApiResponse<List<Map<String, Object>>> upcomingJobs() {
        return ApiResponse.of(driverDashboardQueryService.upcomingJobs(), null);
    }

    @GetMapping("/earnings/summary")
    public ApiResponse<Map<String, Object>> earningsSummary(
            @RequestParam(required = false) String period,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to
    ) {
        return ApiResponse.of(driverDashboardQueryService.earningsSummary(period, from, to), null);
    }

    @GetMapping("/earnings/ledger")
    public ApiResponse<List<Map<String, Object>>> earningsLedger() {
        return ApiResponse.of(driverDashboardQueryService.earningsLedger(), null);
    }

    public record DriverProfilePatchRequest(String fullName, @Email String email, List<String> languages, EmergencyContactRequest emergencyContact) {
    }

    public record DriverOnboardingRequest(
            @NotBlank String fullName,
            @NotNull LocalDate dateOfBirth,
            @NotNull UUID cityId,
            List<String> languages,
            @NotBlank String licenseNumber,
            @NotBlank String aadhaarNumberMaskedOrTokenized,
            @NotBlank String panNumberMaskedOrTokenized,
            @NotNull BankAccountRequest bankAccount,
            @NotNull EmergencyContactRequest emergencyContact
    ) {
    }

    public record DriverDocumentRequest(@NotBlank String documentType, @NotBlank String mediaAssetId, String documentNumber, LocalDate expiresOn) {
    }

    public record AvailabilityRequest(@NotBlank String status, String reasonCode) {
    }

    public record BankAccountRequest(String accountHolderName, String bankName, String accountNumberMasked, String ifscCode) {
    }

    public record EmergencyContactRequest(String name, String mobileNumber) {
    }
}
