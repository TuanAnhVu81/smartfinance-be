package com.smartfinance.service.impl;

import com.smartfinance.dto.request.CategoryRequest;
import com.smartfinance.dto.response.CategoryResponse;
import com.smartfinance.entity.Category;
import com.smartfinance.entity.User;
import com.smartfinance.enums.CategoryType;
import com.smartfinance.exception.AppException;
import com.smartfinance.exception.ErrorCode;
import com.smartfinance.mapper.CategoryMapper;
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
    private final CategoryMapper categoryMapper;

    @Override
    public List<CategoryResponse> getAll(Long userId) {
        // Query: user's own categories + system default categories, all non-deleted
        return categoryRepository.findAllVisibleByUserId(userId)
                .stream()
                .map(categoryMapper::toResponse)
                .toList();
    }

    @Override
    public List<CategoryResponse> getAllByType(Long userId, CategoryType type) {
        return categoryRepository.findAllVisibleByUserIdAndType(userId, type)
                .stream()
                .map(categoryMapper::toResponse)
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

        // Use CategoryMapper to build new entity from request (system fields ignored)
        Category category = categoryMapper.toEntity(request);
        category.setUser(user);
        category.setIsDefault(false);

        Category saved = categoryRepository.save(category);
        log.info("Category created: id={}, name={}, userId={}", saved.getId(), saved.getName(), userId);
        return categoryMapper.toResponse(saved);
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

        // Use CategoryMapper to update mutable fields (id, user, system flags are ignored)
        categoryMapper.updateCategoryFromRequest(request, category);

        Category saved = categoryRepository.save(category);
        log.info("Category updated: id={}, name={}, userId={}", saved.getId(), saved.getName(), userId);
        return categoryMapper.toResponse(saved);
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
