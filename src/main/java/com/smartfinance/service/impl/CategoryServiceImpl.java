package com.smartfinance.service.impl;

import com.smartfinance.dto.request.CategoryRequest;
import com.smartfinance.dto.response.CategoryResponse;
import com.smartfinance.entity.Category;
import com.smartfinance.entity.User;
import com.smartfinance.enums.CategoryType;
import com.smartfinance.exception.AppException;
import com.smartfinance.exception.ErrorCode;
import com.smartfinance.repository.CategoryRepository;
import com.smartfinance.repository.UserRepository;
import com.smartfinance.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    @Override
    public List<CategoryResponse> getAll(Long userId) {
        // Query: user's own categories + system default categories, all non-deleted
        return categoryRepository.findAllVisibleByUserId(userId)
                .stream()
                .map(CategoryResponse::from)
                .toList();
    }

    @Override
    public List<CategoryResponse> getAllByType(Long userId, CategoryType type) {
        return categoryRepository.findAllVisibleByUserIdAndType(userId, type)
                .stream()
                .map(CategoryResponse::from)
                .toList();
    }

    @Override
    @Transactional
    public CategoryResponse create(Long userId, CategoryRequest request) {
        // Validate that user does not already have a category with the same name
        if (categoryRepository.existsByUserIdAndName(userId, request.name())) {
            throw new AppException(ErrorCode.CATEGORY_NAME_DUPLICATED);
        }

        User user = findUser(userId);

        Category category = Category.builder()
                .user(user)
                .name(request.name())
                .type(request.type())
                .icon(request.icon())
                .color(request.color())
                .isDefault(false)   // user-created categories are never system defaults
                .build();

        Category saved = categoryRepository.save(category);
        log.info("Category created: id={}, name={}, userId={}", saved.getId(), saved.getName(), userId);
        return CategoryResponse.from(saved);
    }

    @Override
    @Transactional
    public CategoryResponse update(Long userId, Long categoryId, CategoryRequest request) {
        Category category = findCategoryById(categoryId);

        // System default categories (user IS NULL) must never be mutated
        if (category.getIsDefault() || category.getUser() == null) {
            throw new AppException(ErrorCode.SYSTEM_CATEGORY_IMMUTABLE);
        }

        // Users may only edit their own categories, not another user's
        if (!category.getUser().getId().equals(userId)) {
            throw new AppException(ErrorCode.CATEGORY_NOT_OWNED);
        }

        // Validate name uniqueness, excluding the current category being updated
        if (categoryRepository.existsByUserIdAndNameExcluding(userId, request.name(), categoryId)) {
            throw new AppException(ErrorCode.CATEGORY_NAME_DUPLICATED);
        }

        category.setName(request.name());
        category.setType(request.type());
        category.setIcon(request.icon());
        category.setColor(request.color());

        Category saved = categoryRepository.save(category);
        log.info("Category updated: id={}, name={}, userId={}", saved.getId(), saved.getName(), userId);
        return CategoryResponse.from(saved);
    }

    @Override
    @Transactional
    public void delete(Long userId, Long categoryId) {
        Category category = findCategoryById(categoryId);

        // Block deletion of system default categories
        if (category.getIsDefault() || category.getUser() == null) {
            throw new AppException(ErrorCode.SYSTEM_CATEGORY_IMMUTABLE);
        }

        // Block deletion of another user's category
        if (!category.getUser().getId().equals(userId)) {
            throw new AppException(ErrorCode.CATEGORY_NOT_OWNED);
        }

        // Block deletion if the category still has active (non-deleted) transactions
        if (categoryRepository.hasActiveTransactions(categoryId)) {
            throw new AppException(ErrorCode.CATEGORY_HAS_TRANSACTIONS);
        }

        // Soft delete – do not physically remove from DB
        category.setIsDeleted(true);
        categoryRepository.save(category);
        log.info("Category soft-deleted: id={}, userId={}", categoryId, userId);
    }

    private Category findCategoryById(Long id) {
        return categoryRepository.findById(id)
                .filter(c -> !c.getIsDeleted())
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }
}
