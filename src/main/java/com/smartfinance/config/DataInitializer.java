package com.smartfinance.config;

import com.smartfinance.entity.Category;
import com.smartfinance.entity.Role;
import com.smartfinance.entity.User;
import com.smartfinance.enums.CategoryType;
import com.smartfinance.enums.RoleName;
import com.smartfinance.repository.CategoryRepository;
import com.smartfinance.repository.RoleRepository;
import com.smartfinance.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;



import java.util.List;
import java.util.Set;

@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.default-password}")
    private String adminDefaultPassword;

    @Override
    public void run(String... args) throws Exception {
        log.info("Starting Data Initialization...");
        seedRoles();
        seedAdminAccount();
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

    private void seedAdminAccount() {
        String admin = "admin";
        if (!userRepository.existsByUsername(admin)) {
            Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN)
                    .orElseThrow(() -> new RuntimeException("ROLE_ADMIN not found, should be seeded first"));
            
            User adminAccount = User.builder()
                    .username(admin)
                    .email("admin@smartfinance.com")
                    .password(passwordEncoder.encode(adminDefaultPassword))
                    .fullName("System Administrator")
                    .roles(Set.of(adminRole))
                    .build();

            userRepository.save(adminAccount);
            log.info("Seeded default Admin account (username: {}, password: {})", admin, adminDefaultPassword);
        }
    }

    private void seedDefaultCategories() {
        if (categoryRepository.count() == 0) {
            List<Category> defaultCategories = List.of(
                    // EXPENSE
                    Category.builder().name("Food").type(CategoryType.EXPENSE).icon("🍔").color("#FF8042").isDefault(true).build(),
                    Category.builder().name("Transport").type(CategoryType.EXPENSE).icon("🚗").color("#0088FE").isDefault(true).build(),
                    Category.builder().name("Shopping").type(CategoryType.EXPENSE).icon("🛍️").color("#00C49F").isDefault(true).build(),
                    Category.builder().name("Entertainment").type(CategoryType.EXPENSE).icon("🎮").color("#FFBB28").isDefault(true).build(),
                    Category.builder().name("Health").type(CategoryType.EXPENSE).icon("💊").color("#FF4842").isDefault(true).build(),
                    Category.builder().name("Education").type(CategoryType.EXPENSE).icon("📚").color("#1890FF").isDefault(true).build(),
                    Category.builder().name("Housing").type(CategoryType.EXPENSE).icon("🏠").color("#722ED1").isDefault(true).build(),
                    Category.builder().name("Others").type(CategoryType.EXPENSE).icon("📦").color("#94A3B8").isDefault(true).build(),
                    
                    // INCOME
                    Category.builder().name("Salary").type(CategoryType.INCOME).icon("💼").color("#52C41A").isDefault(true).build(),
                    Category.builder().name("Investment").type(CategoryType.INCOME).icon("📈").color("#13C2C2").isDefault(true).build(),
                    Category.builder().name("Bonus").type(CategoryType.INCOME).icon("🎁").color("#FADB14").isDefault(true).build(),
                    Category.builder().name("Others").type(CategoryType.INCOME).icon("💰").color("#8C8C8C").isDefault(true).build()
            );
            categoryRepository.saveAll(defaultCategories);
            log.info("Seeded default Categories.");
        }
    }
}
