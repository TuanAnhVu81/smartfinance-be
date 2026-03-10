package com.smartfinance.repository;

import com.smartfinance.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

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
}
