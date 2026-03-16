package com.smartfinance.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/health")
@Tag(name = "Health Check", description = "Endpoint for monitoring server status and anti-sleep mechanism")
public class HealthController {

    @Operation(summary = "Check server health status")
    @GetMapping
    public String check() {
        return "SmartFinance Backend is UP and running!";
    }
}
