package com.rydvrse.customer.api;

import com.rydvrse.common.api.ApiResponse;
import com.rydvrse.customer.application.CustomerHomeQueryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/customers/me/home")
public class CustomerHomeController {

    private final CustomerHomeQueryService customerHomeQueryService;

    public CustomerHomeController(CustomerHomeQueryService customerHomeQueryService) {
        this.customerHomeQueryService = customerHomeQueryService;
    }

    @GetMapping
    public ApiResponse<Map<String, Object>> home() {
        return ApiResponse.of(customerHomeQueryService.getHome(), null);
    }
}
