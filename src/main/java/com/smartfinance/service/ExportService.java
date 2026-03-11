package com.smartfinance.service;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface ExportService {
    void exportTransactionsCsv(Long userId, Integer month, Integer year, HttpServletResponse response) throws IOException;
    void exportTransactionsPdf(Long userId, Integer month, Integer year, HttpServletResponse response) throws IOException;
}
