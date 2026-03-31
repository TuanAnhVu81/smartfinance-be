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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.doAnswer;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private User testUser;
    private Category testCategory;
    private CategoryResponse testCategoryResponse;

    @BeforeEach
    void setUp() {
        // Setup shared test objects
        testUser = new User();
        testUser.setId(1L);

        testCategory = new Category();
        testCategory.setId(1L);
        testCategory.setName("Food");
        testCategory.setType(CategoryType.EXPENSE);
        testCategory.setUser(testUser);
        testCategory.setIsDefault(false);
        testCategory.setIsDeleted(false);

        testCategoryResponse = new CategoryResponse(1L, "Food", CategoryType.EXPENSE, "icon", "color", false);
    }

    @Test
    void getAll_Success() {
        // Arrange
        when(categoryRepository.findAllVisibleByUserId(1L)).thenReturn(List.of(testCategory));
        when(categoryMapper.toResponse(testCategory)).thenReturn(testCategoryResponse);

        // Act
        List<CategoryResponse> result = categoryService.getAll(1L);

        // Assert
        assertEquals(1, result.size());
        assertEquals("Food", result.get(0).getName());
        verify(categoryRepository, times(1)).findAllVisibleByUserId(1L);
    }

    @Test
    void create_Success() {
        // Arrange
        CategoryRequest request = new CategoryRequest("NewCat", CategoryType.INCOME, "icon", "color");
        when(categoryRepository.existsByUserIdAndName(1L, "NewCat")).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        
        Category newCategory = new Category();
        when(categoryMapper.toEntity(request)).thenReturn(newCategory);
        when(categoryRepository.save(newCategory)).thenReturn(testCategory); // Mock returning saved with ID
        when(categoryMapper.toResponse(testCategory)).thenReturn(testCategoryResponse);

        // Act
        CategoryResponse response = categoryService.create(1L, request);

        // Assert
        assertNotNull(response);
        verify(categoryRepository, times(1)).save(newCategory);
    }

    @Test
    void create_DuplicatedName_ThrowsException() {
        // Arrange
        CategoryRequest request = new CategoryRequest("Food", CategoryType.EXPENSE, "icon", "color");
        when(categoryRepository.existsByUserIdAndName(1L, "Food")).thenReturn(true);

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> categoryService.create(1L, request));
        assertEquals(ErrorCode.CATEGORY_NAME_DUPLICATED, exception.getErrorCode());
    }

    @Test
    void update_Success() {
        // Arrange
        CategoryRequest request = new CategoryRequest("UpdatedFood", CategoryType.EXPENSE, "icon", "color");
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(categoryRepository.existsByUserIdAndNameExcluding(1L, "UpdatedFood", 1L)).thenReturn(false);
        
        doAnswer(invocation -> {
            Category c = invocation.getArgument(1);
            c.setName("UpdatedFood");
            return null;
        }).when(categoryMapper).updateCategoryFromRequest(request, testCategory);
        
        when(categoryRepository.save(testCategory)).thenReturn(testCategory);
        when(categoryMapper.toResponse(testCategory)).thenReturn(new CategoryResponse(1L, "UpdatedFood", CategoryType.EXPENSE, "icon", "color", false));

        // Act
        CategoryResponse response = categoryService.update(1L, 1L, request);

        // Assert
        assertEquals("UpdatedFood", response.getName());
        verify(categoryRepository, times(1)).save(testCategory);
    }

    @Test
    void delete_Success() {
        // Arrange
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(categoryRepository.hasActiveTransactions(1L)).thenReturn(false);

        // Act
        categoryService.delete(1L, 1L);

        // Assert
        assertTrue(testCategory.getIsDeleted());
        verify(categoryRepository, times(1)).save(testCategory);
    }

    @Test
    void delete_SystemCategoryImmutable_ThrowsException() {
        // Arrange
        testCategory.setIsDefault(true);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> categoryService.delete(1L, 1L));
        assertEquals(ErrorCode.SYSTEM_CATEGORY_IMMUTABLE, exception.getErrorCode());
    }
}
