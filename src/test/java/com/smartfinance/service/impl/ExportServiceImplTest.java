package com.smartfinance.service.impl;

import com.smartfinance.entity.Category;
import com.smartfinance.entity.Transaction;
import com.smartfinance.enums.CategoryType;
import com.smartfinance.exception.AppException;
import com.smartfinance.exception.ErrorCode;
import com.smartfinance.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class ExportServiceImplTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private ExportServiceImpl exportService;

    private MockHttpServletResponse response;
    private Transaction testTransaction;

    @BeforeEach
    void setUp() {
        response = new MockHttpServletResponse();

        Category category = new Category();
        category.setId(1L);
        category.setName("Food");
        category.setType(CategoryType.EXPENSE);

        testTransaction = new Transaction();
        testTransaction.setId(1L);
        testTransaction.setAmount(new BigDecimal("100.00"));
        testTransaction.setTransactionDate(LocalDate.of(2023, 10, 15));
        testTransaction.setCategory(category);
        testTransaction.setNote("Test Note");
    }

    @Test
    void exportTransactionsCsv_Success() throws IOException {
        // Arrange
        Long userId = 1L;
        Integer month = 10;
        Integer year = 2023;
        LocalDate startDate = LocalDate.of(2023, 10, 1);
        LocalDate endDate = LocalDate.of(2023, 10, 31);

        when(transactionRepository.countTransactionsForExport(userId, startDate, endDate)).thenReturn(1L);
        when(transactionRepository.streamTransactionsForExport(userId, startDate, endDate))
                .thenReturn(Stream.of(testTransaction));

        // Act
        exportService.exportTransactionsCsv(userId, month, year, response);

        // Assert
        verify(transactionRepository, times(1)).countTransactionsForExport(userId, startDate, endDate);
        verify(transactionRepository, times(1)).streamTransactionsForExport(userId, startDate, endDate);
        
        String content = response.getContentAsString();
        assertTrue(content.contains("Transaction Date"));
        assertTrue(content.contains("Food"));
        assertTrue(content.contains("100.00"));
    }

    @Test
    void exportTransactionsCsv_NoData_ThrowsException() {
        // Arrange
        Long userId = 1L;
        LocalDate startDate = LocalDate.of(2023, 10, 1);
        LocalDate endDate = LocalDate.of(2023, 10, 31);

        when(transactionRepository.countTransactionsForExport(userId, startDate, endDate)).thenReturn(0L);

        // Act & Assert
        AppException exception = assertThrows(AppException.class, 
                () -> exportService.exportTransactionsCsv(userId, 10, 2023, response));
        assertEquals(ErrorCode.NO_DATA_TO_EXPORT, exception.getErrorCode());
    }

    @Test
    void exportTransactionsPdf_Success() throws IOException {
        // Arrange
        Long userId = 1L;
        Integer month = 10;
        Integer year = 2023;
        LocalDate startDate = LocalDate.of(2023, 10, 1);
        LocalDate endDate = LocalDate.of(2023, 10, 31);

        when(transactionRepository.countTransactionsForExport(userId, startDate, endDate)).thenReturn(1L);
        when(transactionRepository.streamTransactionsForExport(userId, startDate, endDate))
                .thenReturn(Stream.of(testTransaction));

        // Act
        exportService.exportTransactionsPdf(userId, month, year, response);

        // Assert
        verify(transactionRepository, times(1)).countTransactionsForExport(userId, startDate, endDate);
        verify(transactionRepository, times(1)).streamTransactionsForExport(userId, startDate, endDate);
        
        assertTrue(response.getContentAsByteArray().length > 0);
    }
}
