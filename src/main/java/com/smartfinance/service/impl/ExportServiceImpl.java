package com.smartfinance.service.impl;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.*;
import com.opencsv.CSVWriter;
import com.smartfinance.entity.Transaction;
import com.smartfinance.exception.AppException;
import com.smartfinance.exception.ErrorCode;
import com.smartfinance.repository.TransactionRepository;
import com.smartfinance.service.ExportService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExportServiceImpl implements ExportService {

    private final TransactionRepository transactionRepository;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    @Transactional(readOnly = true)
    public void exportTransactionsCsv(Long userId, Integer month, Integer year, HttpServletResponse response) throws IOException {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        // 1. Fail-fast Data Check
        long count = transactionRepository.countTransactionsForExport(userId, startDate, endDate);
        if (count == 0) {
            throw new AppException(ErrorCode.NO_DATA_TO_EXPORT);
        }

        // 2. Add BOM for Microsoft Excel compatibility (UTF-8)
        response.getOutputStream().write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});

        // 3. Setup CSV Streaming Writer
        try (OutputStreamWriter osw = new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8);
             CSVWriter writer = new CSVWriter(osw,
                     CSVWriter.DEFAULT_SEPARATOR,
                     CSVWriter.DEFAULT_QUOTE_CHARACTER,
                     CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                     CSVWriter.DEFAULT_LINE_END)) {

            // Write Headers
            String[] headers = {"Transaction Date", "Type", "Category", "Amount (VND)", "Note"};
            writer.writeNext(headers);

            // Fetch stream and process line by line to prevent OOM
            try (Stream<Transaction> txStream = transactionRepository.streamTransactionsForExport(userId, startDate, endDate)) {
                txStream.forEach(tx -> {
                    String[] line = {
                            tx.getTransactionDate().format(DATE_FORMATTER),
                            tx.getCategory().getType().name(),
                            tx.getCategory().getName(),
                            tx.getAmount().toPlainString(),
                            tx.getNote() != null ? tx.getNote() : ""
                    };
                    writer.writeNext(line);
                });
            }
            osw.flush();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public void exportTransactionsPdf(Long userId, Integer month, Integer year, HttpServletResponse response) throws IOException {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        // 1. Fail-fast Data Check
        long count = transactionRepository.countTransactionsForExport(userId, startDate, endDate);
        if (count == 0) {
            throw new AppException(ErrorCode.NO_DATA_TO_EXPORT);
        }

        // 2. Setup PDF Document
        Document document = new Document(PageSize.A4);
        try {
            PdfWriter.getInstance(document, response.getOutputStream());
            document.open();

            // Setup Custom Font safe approach
            BaseFont baseFont;
            try {
                // Read font from Spring Resources explicitly
                ClassPathResource resource = new ClassPathResource("fonts/Roboto-Regular.ttf");
                try (InputStream is = resource.getInputStream()) {
                    byte[] fontBytes = is.readAllBytes();
                    baseFont = BaseFont.createFont("Roboto-Regular.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, true, fontBytes, fontBytes);
                }
            } catch (Exception e) {
                log.warn("Custom font missing, falling back to default. UI may break on Vietnamese characters: {}", e.getMessage());
                baseFont = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
            }

            Font titleFont = new Font(baseFont, 18, Font.BOLD);
            Font headerFont = new Font(baseFont, 12, Font.BOLD);
            Font cellFont = new Font(baseFont, 12, Font.NORMAL);

            // Core Page Title
            Paragraph title = new Paragraph("Transaction Report - Month " + month + "/" + year, titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20f);
            document.add(title);

            // Table configuration (5 cols)
            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100f);
            table.setWidths(new float[]{3f, 2f, 3f, 3f, 4f});

            // Table Headers
            String[] headersArr = {"Transaction Date", "Type", "Category", "Amount (VND)", "Note"};
            for (String h : headersArr) {
                PdfPCell cell = new PdfPCell(new Phrase(h, headerFont));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(6f);
                cell.setBackgroundColor(java.awt.Color.LIGHT_GRAY);
                table.addCell(cell);
            }

            // Summary Variables
            final BigDecimal[] totals = {BigDecimal.ZERO, BigDecimal.ZERO}; // [0]=Income, [1]=Expense

            // Data rows via stream to prevent OOM
            try (Stream<Transaction> txStream = transactionRepository.streamTransactionsForExport(userId, startDate, endDate)) {
                txStream.forEach(tx -> {
                    if (tx.getCategory().getType() == com.smartfinance.enums.CategoryType.INCOME) {
                        totals[0] = totals[0].add(tx.getAmount());
                    } else {
                        totals[1] = totals[1].add(tx.getAmount());
                    }

                    table.addCell(createCell(tx.getTransactionDate().format(DATE_FORMATTER), cellFont));
                    table.addCell(createCell(tx.getCategory().getType().name(), cellFont));
                    table.addCell(createCell(tx.getCategory().getName(), cellFont));
                    table.addCell(createCell(tx.getAmount().toPlainString(), cellFont));
                    table.addCell(createCell(tx.getNote() != null ? tx.getNote() : "", cellFont));
                });
            }

            document.add(table);

            // Summary Block appended to the bottom
            document.add(new Paragraph("\n"));
            document.add(new Paragraph("FINANCIAL SUMMARY:", headerFont));
            document.add(new Paragraph("- Total Income: " + totals[0].toPlainString() + " VND", cellFont));
            document.add(new Paragraph("- Total Expense: " + totals[1].toPlainString() + " VND", cellFont));
            BigDecimal surplus = totals[0].subtract(totals[1]);
            document.add(new Paragraph("- Net Balance: " + surplus.toPlainString() + " VND", headerFont));

        } catch (DocumentException e) {
            log.error("PDF generation failed", e);
            throw new IOException("Error generating PDF", e);
        } finally {
            if (document.isOpen()) {
                document.close();
            }
        }
    }

    private PdfPCell createCell(String content, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(content, font));
        cell.setPadding(5f);
        return cell;
    }
}
