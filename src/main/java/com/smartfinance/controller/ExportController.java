package com.smartfinance.controller;

import com.smartfinance.security.UserPrincipal;
import com.smartfinance.service.ExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/exports")
@RequiredArgsConstructor
@Tag(name = "Export Module", description = "Report Generation and Export (CSV, PDF)")
public class ExportController {

    private final ExportService exportService;

    @Operation(summary = "Export Transactions to CSV")
    @GetMapping("/csv")
    public void exportTransactionsCsv(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam Integer month,
            @RequestParam Integer year,
            HttpServletResponse response
    ) throws IOException {
        
        // Define standard Content-Disposition filename
        String filename = String.format("smartfinance_%02d_%d.csv", month, year);
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        
        exportService.exportTransactionsCsv(principal.getId(), month, year, response);
    }

    @Operation(summary = "Export Transactions to PDF")
    @GetMapping("/pdf")
    public void exportTransactionsPdf(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam Integer month,
            @RequestParam Integer year,
            HttpServletResponse response
    ) throws IOException {

        // Define standard Content-Disposition filename
        String filename = String.format("smartfinance_%02d_%d.pdf", month, year);
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

        exportService.exportTransactionsPdf(principal.getId(), month, year, response);
    }
}
