package com.smartfinance.repository;

import com.smartfinance.entity.Category;
import com.smartfinance.enums.CategoryType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // Fetch all visible categories for a user:
    // includes default system categories (user IS NULL) + user's own categories
    // excludes soft-deleted entries
    @Query("SELECT c FROM Category c WHERE (c.user.id = :userId OR c.user IS NULL) AND c.isDeleted = false ORDER BY c.isDefault DESC, c.name ASC")
    List<Category> findAllVisibleByUserId(@Param("userId") Long userId);

    // Fetch visible categories for a user filtered by type (INCOME or EXPENSE)
    @Query("SELECT c FROM Category c WHERE (c.user.id = :userId OR c.user IS NULL) AND c.type = :type AND c.isDeleted = false ORDER BY c.isDefault DESC, c.name ASC")
    List<Category> findAllVisibleByUserIdAndType(@Param("userId") Long userId, @Param("type") CategoryType type);

    // Check duplicate name within the same user's categories (excluding soft deleted)
    @Query("SELECT COUNT(c) > 0 FROM Category c WHERE c.user.id = :userId AND c.name = :name AND c.isDeleted = false")
    boolean existsByUserIdAndName(@Param("userId") Long userId, @Param("name") String name);

    // Same duplicate check but excluding a specific category (used during UPDATE)
    @Query("SELECT COUNT(c) > 0 FROM Category c WHERE c.user.id = :userId AND c.name = :name AND c.id != :excludeId AND c.isDeleted = false")
    boolean existsByUserIdAndNameExcluding(@Param("userId") Long userId, @Param("name") String name, @Param("excludeId") Long excludeId);

    // Check whether any non-deleted transactions reference this category (before soft delete)
    @Query("SELECT COUNT(t) > 0 FROM Transaction t WHERE t.category.id = :categoryId AND t.isDeleted = false")
    boolean hasActiveTransactions(@Param("categoryId") Long categoryId);
}
