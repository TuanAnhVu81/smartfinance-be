package com.smartfinance.repository;

import com.smartfinance.entity.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {

    // Find a specific budget by composite key (used by TransactionService for delta update)
    @Query("SELECT b FROM Budget b WHERE b.user.id = :userId AND b.category.id = :categoryId AND b.month = :month AND b.year = :year")
    Optional<Budget> findByUserIdAndCategoryIdAndMonthAndYear(
            @Param("userId") Long userId,
            @Param("categoryId") Long categoryId,
            @Param("month") Integer month,
            @Param("year") Integer year
    );

    // Get all budgets for a user in a specific month/year (used for GET /api/budgets)
    @Query("SELECT b FROM Budget b JOIN FETCH b.category WHERE b.user.id = :userId AND b.month = :month AND b.year = :year")
    List<Budget> findAllByUserIdAndMonthAndYear(
            @Param("userId") Long userId,
            @Param("month") Integer month,
            @Param("year") Integer year
    );

    // Check duplicate before insert – enforce UNIQUE (user, category, month, year) at service layer
    boolean existsByUserIdAndCategoryIdAndMonthAndYear(Long userId, Long categoryId, Integer month, Integer year);
}

