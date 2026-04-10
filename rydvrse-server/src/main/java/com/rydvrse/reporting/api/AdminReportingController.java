package com.rydvrse.reporting.api;

import com.rydvrse.common.api.ApiResponse;
import com.rydvrse.reporting.application.OperationsReportingService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/reports")
public class AdminReportingController {

    private final OperationsReportingService operationsReportingService;

    public AdminReportingController(OperationsReportingService operationsReportingService) {
        this.operationsReportingService = operationsReportingService;
    }

    @GetMapping("/operations")
    public ApiResponse<Map<String, Object>> operations(
            @RequestParam(name = "from_date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(name = "to_date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(name = "city_id", required = false) UUID cityId
    ) {
        return ApiResponse.of(operationsReportingService.summary(fromDate, toDate, cityId), null);
    }
}
