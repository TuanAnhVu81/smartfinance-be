package com.smartfinance.service.impl;

import com.smartfinance.dto.request.TransactionRequest;
import com.smartfinance.dto.response.TransactionResponse;
import com.smartfinance.entity.Budget;
import com.smartfinance.entity.Category;
import com.smartfinance.entity.Transaction;
import com.smartfinance.entity.User;
import com.smartfinance.enums.CategoryType;
import com.smartfinance.mapper.TransactionMapper;
import com.smartfinance.repository.BudgetRepository;
import com.smartfinance.repository.CategoryRepository;
import com.smartfinance.repository.TransactionRepository;
import com.smartfinance.repository.UserRepository;
import com.smartfinance.service.AiInsightService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TransactionMapper transactionMapper;

    @Mock
    private AiInsightService aiInsightService;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private User testUser;
    private Category testCategory;
    private Transaction testTransaction;
    private TransactionResponse testTransactionResponse;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);

        testCategory = new Category();
        testCategory.setId(1L);
        testCategory.setType(CategoryType.EXPENSE);
        testCategory.setUser(testUser);
        testCategory.setIsDeleted(false);

        testTransaction = new Transaction();
        testTransaction.setId(1L);
        testTransaction.setUser(testUser);
        testTransaction.setCategory(testCategory);
        testTransaction.setAmount(new BigDecimal("100.00"));
        testTransaction.setTransactionDate(LocalDate.of(2023, 10, 15));
        testTransaction.setIsDeleted(false);

        testTransactionResponse = new TransactionResponse(1L, null, new BigDecimal("100.00"), "Notes", LocalDate.of(2023, 10, 15), null);
    }

    @Test
    void getTransactions_Success() {
        // Arrange
        PageRequest pageable = PageRequest.of(0, 10);
        Page<Transaction> page = new PageImpl<>(List.of(testTransaction));
        when(transactionRepository.findAllFiltered(1L, null, 10, 2023, pageable)).thenReturn(page);
        when(transactionMapper.toResponse(testTransaction)).thenReturn(testTransactionResponse);

        // Act
        Page<TransactionResponse> result = transactionService.getTransactions(1L, 10, 2023, null, pageable);

        // Assert
        assertEquals(1, result.getTotalElements());
        verify(transactionRepository, times(1)).findAllFiltered(1L, null, 10, 2023, pageable);
    }

    @Test
    void createTransaction_Success() {
        // Arrange
        TransactionRequest request = new TransactionRequest(1L, new BigDecimal("100.00"), "Notes", LocalDate.of(2023, 10, 15));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        
        Transaction newTransaction = new Transaction();
        when(transactionMapper.toEntity(request)).thenReturn(newTransaction);
        when(transactionMapper.toResponse(any(Transaction.class))).thenReturn(testTransactionResponse);

        Budget budget = new Budget();
        budget.setSpentAmount(new BigDecimal("50.00"));
        when(budgetRepository.findByUserIdAndCategoryIdAndMonthAndYear(1L, 1L, 10, 2023)).thenReturn(Optional.of(budget));

        // Act
        TransactionResponse response = transactionService.createTransaction(1L, request);

        // Assert
        assertNotNull(response);
        verify(transactionRepository, times(1)).save(newTransaction);
        verify(budgetRepository, times(1)).save(budget);
        assertEquals(new BigDecimal("150.00"), budget.getSpentAmount());
        verify(aiInsightService, times(1)).markOutdated(1L, 10, 2023);
    }

    @Test
    void updateTransaction_Success() {
        // Arrange
        TransactionRequest request = new TransactionRequest(1L, new BigDecimal("200.00"), "Updated Notes", LocalDate.of(2023, 10, 15));
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(testTransaction));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(transactionMapper.toResponse(testTransaction)).thenReturn(testTransactionResponse);

        // Act
        TransactionResponse response = transactionService.updateTransaction(1L, 1L, request);

        // Assert
        assertNotNull(response);
        verify(transactionRepository, times(1)).save(testTransaction);
        verify(aiInsightService, times(1)).markOutdated(1L, 10, 2023);
    }

    @Test
    void deleteTransaction_Success() {
        // Arrange
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(testTransaction));

        Budget budget = new Budget();
        budget.setSpentAmount(new BigDecimal("150.00"));
        when(budgetRepository.findByUserIdAndCategoryIdAndMonthAndYear(1L, 1L, 10, 2023)).thenReturn(Optional.of(budget));

        // Act
        transactionService.deleteTransaction(1L, 1L);

        // Assert
        assertTrue(testTransaction.getIsDeleted());
        verify(transactionRepository, times(1)).save(testTransaction);
        verify(budgetRepository, times(1)).save(budget);
        assertEquals(new BigDecimal("50.00"), budget.getSpentAmount());
        verify(aiInsightService, times(1)).markOutdated(1L, 10, 2023);
    }
}
