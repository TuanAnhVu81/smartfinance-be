package com.smartfinance.repository;

import com.smartfinance.entity.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {

    @Query("SELECT b FROM Budget b WHERE b.user.id = :userId AND b.category.id = :categoryId AND b.month = :month AND b.year = :year")
    Optional<Budget> findByUserIdAndCategoryIdAndMonthAndYear(
            @Param("userId") Long userId,
            @Param("categoryId") Long categoryId,
            @Param("month") Integer month,
            @Param("year") Integer year
    );
}
