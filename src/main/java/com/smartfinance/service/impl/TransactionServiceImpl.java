package com.smartfinance.service.impl;

import com.smartfinance.dto.request.TransactionRequest;
import com.smartfinance.dto.response.TransactionResponse;
import com.smartfinance.entity.Budget;
import com.smartfinance.entity.Category;
import com.smartfinance.entity.Transaction;
import com.smartfinance.entity.User;
import com.smartfinance.enums.CategoryType;
import com.smartfinance.exception.AppException;
import com.smartfinance.exception.ErrorCode;
import com.smartfinance.mapper.TransactionMapper;
import com.smartfinance.repository.BudgetRepository;
import com.smartfinance.repository.CategoryRepository;
import com.smartfinance.repository.TransactionRepository;
import com.smartfinance.repository.UserRepository;
import com.smartfinance.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final BudgetRepository budgetRepository;
    private final UserRepository userRepository;
    private final TransactionMapper transactionMapper;

    @Override
    public Page<TransactionResponse> getTransactions(Long userId, Integer month, Integer year, Long categoryId, Pageable pageable) {
        return transactionRepository.findAllFiltered(userId, categoryId, month, year, pageable)
                .map(transactionMapper::toResponse);
    }

    @Override
    @Transactional
    public TransactionResponse createTransaction(Long userId, TransactionRequest request) {
        User user = findUser(userId);
        Category category = validateCategory(userId, request.categoryId());

        // Use TransactionMapper to create entity (category and user are set manually)
        Transaction tx = transactionMapper.toEntity(request);
        tx.setUser(user);
        tx.setCategory(category);
        tx.setIsDeleted(false);

        transactionRepository.save(tx);
        
        // Cập nhật Budget (cộng vào)
        applyBudgetDelta(userId, category, null, request.amount(), request.transactionDate().getMonthValue(), request.transactionDate().getYear());

        log.info("Created transaction id={} for user={}", tx.getId(), userId);
        return transactionMapper.toResponse(tx);
    }

    @Override
    @Transactional
    public TransactionResponse updateTransaction(Long userId, Long transactionId, TransactionRequest request) {
        Transaction tx = findTransactionByIdAndUser(transactionId, userId);
        
        Category oldCategory = tx.getCategory();
        Integer oldMonth = tx.getTransactionDate().getMonthValue();
        Integer oldYear = tx.getTransactionDate().getYear();
        BigDecimal oldAmount = tx.getAmount();

        Category newCategory = validateCategory(userId, request.categoryId());
        Integer newMonth = request.transactionDate().getMonthValue();
        Integer newYear = request.transactionDate().getYear();
        BigDecimal newAmount = request.amount();

        // 1. Rollback old budget (Trừ đi ở Budget cũ)
        applyBudgetDelta(userId, oldCategory, oldAmount, null, oldMonth, oldYear);

        // Use TransactionMapper to update mutable fields (category and user set manually)
        transactionMapper.updateTransactionFromRequest(request, tx);
        tx.setCategory(newCategory);
        transactionRepository.save(tx);

        // 3. Apply new budget (Cộng vào Budget mới)
        applyBudgetDelta(userId, newCategory, null, newAmount, newMonth, newYear);

        log.info("Updated transaction id={} for user={}", tx.getId(), userId);
        return transactionMapper.toResponse(tx);
    }

    @Override
    @Transactional
    public void deleteTransaction(Long userId, Long transactionId) {
        Transaction tx = findTransactionByIdAndUser(transactionId, userId);
        
        tx.setIsDeleted(true);
        transactionRepository.save(tx);

        // Rollback budget (Trừ đi số tiền)
        applyBudgetDelta(userId, tx.getCategory(), tx.getAmount(), null, 
                tx.getTransactionDate().getMonthValue(), tx.getTransactionDate().getYear());

        log.info("Soft-deleted transaction id={} for user={}", tx.getId(), userId);
    }

    // --- Helper Methods ---

    /**
     * Logic thông minh để điều chỉnh dòng tiền trong ngân sách:
     * - oldAmount != null: Nghĩa là CẦN TRỪ số tiền này khỏi Budget (rollback).
     * - newAmount != null: Nghĩa là CẦN CỘNG số tiền này vào Budget (apply).
     */
    private void applyBudgetDelta(Long userId, Category category, BigDecimal oldAmount, BigDecimal newAmount, Integer month, Integer year) {
        // Chỉ cập nhật Budget đối với loại chi tiêu (EXPENSE). Không làm Budget cho INCOME.
        if (category.getType() != CategoryType.EXPENSE) {
            return;
        }

        // Nếu budget không có, "skip silently" theo đúng yêu cầu dự án.
        budgetRepository.findByUserIdAndCategoryIdAndMonthAndYear(userId, category.getId(), month, year)
                .ifPresent(budget -> {
                    BigDecimal spent = budget.getSpentAmount();
                    if (oldAmount != null) {
                        spent = spent.subtract(oldAmount);
                    }
                    if (newAmount != null) {
                        spent = spent.add(newAmount);
                    }
                    
                    // Để an toàn, nếu số bị trừ lố về âm thì ép lên 0 (phòng trường hợp data lệch)
                    if (spent.compareTo(BigDecimal.ZERO) < 0) {
                        spent = BigDecimal.ZERO;
                    }
                    
                    budget.setSpentAmount(spent);
                    budgetRepository.save(budget);
                    log.debug("Updated Budget [{}] spent amount to {}", budget.getId(), spent);
                });
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    private Category validateCategory(Long userId, Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .filter(c -> !c.getIsDeleted())
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

        if (category.getUser() != null && !category.getUser().getId().equals(userId)) {
            throw new AppException(ErrorCode.CATEGORY_NOT_OWNED);
        }
        return category;
    }

    private Transaction findTransactionByIdAndUser(Long transactionId, Long userId) {
        Transaction tx = transactionRepository.findById(transactionId)
                .filter(t -> !t.getIsDeleted())
                .orElseThrow(() -> new AppException(ErrorCode.TRANSACTION_NOT_FOUND));
                
        if (!tx.getUser().getId().equals(userId)) {
            // Không được chạm vào transaction của người khác
            throw new AppException(ErrorCode.TRANSACTION_NOT_OWNED);
        }
        
        return tx;
    }
}
