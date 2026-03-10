package com.smartfinance.repository;

import com.smartfinance.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // Dynamic filter query for transaction list (GET /api/transactions)
    @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId " +
           "AND (:categoryId IS NULL OR t.category.id = :categoryId) " +
           "AND (:month IS NULL OR MONTH(t.transactionDate) = :month) " +
           "AND (:year IS NULL OR YEAR(t.transactionDate) = :year) " +
           "AND t.isDeleted = false")
    Page<Transaction> findAllFiltered(
            @Param("userId") Long userId,
            @Param("categoryId") Long categoryId,
            @Param("month") Integer month,
            @Param("year") Integer year,
            Pageable pageable
    );

    // SUM all non-deleted EXPENSE transactions for a given category/month/year
    // Returns null if no rows match – caller must handle null via BigDecimal.ZERO fallback
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "WHERE t.user.id = :userId " +
           "AND t.category.id = :categoryId " +
           "AND MONTH(t.transactionDate) = :month " +
           "AND YEAR(t.transactionDate) = :year " +
           "AND t.isDeleted = false")
    BigDecimal sumExpenseAmountByCategoryAndPeriod(
            @Param("userId") Long userId,
            @Param("categoryId") Long categoryId,
            @Param("month") Integer month,
            @Param("year") Integer year
    );

    // -----------------------------------------------------
    // DASHBOARD METRICS (PHASE 4)
    // -----------------------------------------------------

    // 1. Calculate Summary (Total Income / Total Expense) for a specific period
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "WHERE t.user.id = :userId " +
           "AND t.category.type = :type " +
           "AND t.transactionDate >= :startDate " +
           "AND t.transactionDate <= :endDate " +
           "AND t.isDeleted = false")
    BigDecimal calculateTotalAmountByTypeAndPeriod(
            @Param("userId") Long userId,
            @Param("type") com.smartfinance.enums.CategoryType type,
            @Param("startDate") java.time.LocalDate startDate,
            @Param("endDate") java.time.LocalDate endDate
    );

    // 2. Category Breakdown (Pie Chart) for a specific month/period
    @Query("SELECT c.id AS categoryId, c.name AS categoryName, c.icon AS icon, c.color AS color, SUM(t.amount) AS totalAmount " +
           "FROM Transaction t JOIN t.category c " +
           "WHERE t.user.id = :userId " +
           "AND c.type = :type " +
           "AND t.transactionDate >= :startDate " +
           "AND t.transactionDate <= :endDate " +
           "AND t.isDeleted = false " +
           "GROUP BY c.id, c.name, c.icon, c.color " +
           "ORDER BY totalAmount DESC")
    java.util.List<com.smartfinance.dto.response.CategoryChartProjection> getCategoryBreakdownByPeriod(
            @Param("userId") Long userId,
            @Param("type") com.smartfinance.enums.CategoryType type,
            @Param("startDate") java.time.LocalDate startDate,
            @Param("endDate") java.time.LocalDate endDate
    );

    // 3. Monthly Trend (Bar Chart) for a specific year
    // Extracts the month from transactionDate and groups by month + category type
    @Query("SELECT MONTH(t.transactionDate) AS month, c.type AS type, SUM(t.amount) AS totalAmount " +
           "FROM Transaction t JOIN t.category c " +
           "WHERE t.user.id = :userId " +
           "AND t.transactionDate >= :startDate " +
           "AND t.transactionDate <= :endDate " +
           "AND t.isDeleted = false " +
           "GROUP BY MONTH(t.transactionDate), c.type")
    java.util.List<com.smartfinance.dto.response.MonthlyTrendProjection> getMonthlyTrendByPeriod(
            @Param("userId") Long userId,
            @Param("startDate") java.time.LocalDate startDate,
            @Param("endDate") java.time.LocalDate endDate
    );
}


