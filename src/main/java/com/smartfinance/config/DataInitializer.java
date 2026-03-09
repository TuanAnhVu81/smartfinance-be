package com.smartfinance.config;

import com.smartfinance.entity.Category;
import com.smartfinance.entity.Role;
import com.smartfinance.enums.CategoryType;
import com.smartfinance.enums.RoleName;
import com.smartfinance.repository.CategoryRepository;
import com.smartfinance.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final CategoryRepository categoryRepository;

    @Override
    public void run(String... args) throws Exception {
        log.info("Starting Data Initialization...");
        seedRoles();
        seedDefaultCategories();
        log.info("Data Initialization completed!");
    }

    private void seedRoles() {
        if (roleRepository.count() == 0) {
            Role userRole = Role.builder().name(RoleName.ROLE_USER).build();
            Role adminRole = Role.builder().name(RoleName.ROLE_ADMIN).build();
            roleRepository.saveAll(List.of(userRole, adminRole));
            log.info("Seeded ROLE_USER and ROLE_ADMIN.");
        }
    }

    private void seedDefaultCategories() {
        if (categoryRepository.count() == 0) {
            List<Category> defaultCategories = List.of(
                    // EXPENSE
                    Category.builder().name("Food").type(CategoryType.EXPENSE).icon("🍔").isDefault(true).build(),
                    Category.builder().name("Transport").type(CategoryType.EXPENSE).icon("🚗").isDefault(true).build(),
                    Category.builder().name("Shopping").type(CategoryType.EXPENSE).icon("🛍️").isDefault(true).build(),
                    Category.builder().name("Entertainment").type(CategoryType.EXPENSE).icon("🎮").isDefault(true).build(),
                    Category.builder().name("Health").type(CategoryType.EXPENSE).icon("💊").isDefault(true).build(),
                    Category.builder().name("Education").type(CategoryType.EXPENSE).icon("📚").isDefault(true).build(),
                    Category.builder().name("Housing").type(CategoryType.EXPENSE).icon("🏠").isDefault(true).build(),
                    Category.builder().name("Others").type(CategoryType.EXPENSE).icon("📦").isDefault(true).build(),
                    
                    // INCOME
                    Category.builder().name("Salary").type(CategoryType.INCOME).icon("💼").isDefault(true).build(),
                    Category.builder().name("Investment").type(CategoryType.INCOME).icon("📈").isDefault(true).build(),
                    Category.builder().name("Bonus").type(CategoryType.INCOME).icon("🎁").isDefault(true).build(),
                    Category.builder().name("Others").type(CategoryType.INCOME).icon("💰").isDefault(true).build()
            );
            categoryRepository.saveAll(defaultCategories);
            log.info("Seeded default Categories.");
        }
    }
}
