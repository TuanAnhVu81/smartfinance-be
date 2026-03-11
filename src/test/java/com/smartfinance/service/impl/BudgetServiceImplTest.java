package com.smartfinance.service.impl;

import com.smartfinance.dto.request.BudgetRequest;
import com.smartfinance.dto.response.BudgetResponse;
import com.smartfinance.entity.Budget;
import com.smartfinance.entity.Category;
import com.smartfinance.entity.User;
import com.smartfinance.enums.CategoryType;
import com.smartfinance.exception.AppException;
import com.smartfinance.exception.ErrorCode;
import com.smartfinance.mapper.BudgetMapper;
import com.smartfinance.repository.BudgetRepository;
import com.smartfinance.repository.CategoryRepository;
import com.smartfinance.repository.TransactionRepository;
import com.smartfinance.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class BudgetServiceImplTest {

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BudgetMapper budgetMapper;

    @InjectMocks
    private BudgetServiceImpl budgetService;

    private User testUser;
    private Category testCategory;
    private Budget testBudget;
    private BudgetResponse testBudgetResponse;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);

        testCategory = new Category();
        testCategory.setId(1L);
        testCategory.setType(CategoryType.EXPENSE);
        testCategory.setIsDeleted(false);
        testCategory.setUser(testUser);

        testBudget = new Budget();
        testBudget.setId(1L);
        testBudget.setUser(testUser);
        testBudget.setCategory(testCategory);
        testBudget.setAmountLimit(new BigDecimal("1000.00"));
        testBudget.setSpentAmount(new BigDecimal("500.00"));
        testBudget.setMonth(10);
        testBudget.setYear(2023);

        testBudgetResponse = new BudgetResponse(1L, null, 10, 2023, new BigDecimal("1000.00"), new BigDecimal("500.00"), null);
    }

    @Test
    void getBudgets_Success() {
        // Arrange
        when(budgetRepository.findAllByUserIdAndMonthAndYear(1L, 10, 2023)).thenReturn(List.of(testBudget));
        when(budgetMapper.toResponse(testBudget)).thenReturn(testBudgetResponse);

        // Act
        List<BudgetResponse> responses = budgetService.getBudgets(1L, 10, 2023);

        // Assert
        assertEquals(1, responses.size());
        assertEquals(new BigDecimal("50.00"), responses.get(0).getPercentage());
        verify(budgetRepository, times(1)).findAllByUserIdAndMonthAndYear(1L, 10, 2023);
    }

    @Test
    void createBudget_Success() {
        // Arrange
        BudgetRequest request = new BudgetRequest(1L, new BigDecimal("1000.00"), 10, 2023);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(budgetRepository.existsByUserIdAndCategoryIdAndMonthAndYear(1L, 1L, 10, 2023)).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(transactionRepository.sumExpenseAmountByCategoryAndPeriod(1L, 1L, 10, 2023)).thenReturn(new BigDecimal("100.00"));
        when(budgetRepository.save(any(Budget.class))).thenReturn(testBudget);
        when(budgetMapper.toResponse(testBudget)).thenReturn(testBudgetResponse);

        // Act
        BudgetResponse response = budgetService.createBudget(1L, request);

        // Assert
        assertNotNull(response);
        verify(budgetRepository, times(1)).save(any(Budget.class));
    }

    @Test
    void createBudget_IncomeCategory_ThrowsException() {
        // Arrange
        testCategory.setType(CategoryType.INCOME);
        BudgetRequest request = new BudgetRequest(1L, new BigDecimal("1000.00"), 10, 2023);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> budgetService.createBudget(1L, request));
        assertEquals(ErrorCode.BUDGET_INCOME_NOT_ALLOWED, exception.getErrorCode());
    }

    @Test
    void updateBudget_Success() {
        // Arrange
        BudgetRequest request = new BudgetRequest(1L, new BigDecimal("2000.00"), 10, 2023);
        when(budgetRepository.findById(1L)).thenReturn(Optional.of(testBudget));
        when(budgetRepository.save(any(Budget.class))).thenReturn(testBudget);
        when(budgetMapper.toResponse(testBudget)).thenReturn(testBudgetResponse);

        // Act
        BudgetResponse response = budgetService.updateBudget(1L, 1L, request);

        // Assert
        assertNotNull(response);
        verify(budgetRepository, times(1)).save(testBudget);
        assertEquals(new BigDecimal("2000.00"), testBudget.getAmountLimit());
    }

    @Test
    void deleteBudget_Success() {
        // Arrange
        when(budgetRepository.findById(1L)).thenReturn(Optional.of(testBudget));

        // Act
        budgetService.deleteBudget(1L, 1L);

        // Assert
        verify(budgetRepository, times(1)).delete(testBudget);
    }
}
