package com.rikkeisoft.bank.config;

import com.rikkeisoft.bank.entity.Role;
import com.rikkeisoft.bank.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DatabaseInitializer implements CommandLineRunner {
    private final RoleRepository roleRepository;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        // Clean up obsolete columns from users table
        dropColumnQuietly("users", "enabled");
        dropColumnQuietly("users", "full_name");
        dropColumnQuietly("users", "fullName");
        dropColumnQuietly("users", "phone");

        // Clean up obsolete columns from kyc_profiles table
        dropColumnQuietly("kyc_profiles", "identity_number");
        dropColumnQuietly("kyc_profiles", "identityNumber");
        dropColumnQuietly("kyc_profiles", "front_image_url");
        dropColumnQuietly("kyc_profiles", "frontImageUrl");
        dropColumnQuietly("kyc_profiles", "back_image_url");
        dropColumnQuietly("kyc_profiles", "backImageUrl");
        dropColumnQuietly("kyc_profiles", "selfie_image_url");
        dropColumnQuietly("kyc_profiles", "selfieImageUrl");

        // Clean up obsolete columns from token_blacklist table
        dropColumnQuietly("token_blacklist", "token");
        dropColumnQuietly("token_blacklist", "expiry_date");
        dropColumnQuietly("token_blacklist", "expiryDate");

        // Clean up obsolete columns from transactions table
        dropColumnQuietly("transactions", "type");

        // Initialize roles
        initializeRole("CUSTOMER", "Customer role");
        initializeRole("ADMIN", "Administrator role");
        initializeRole("STAFF", "Staff role");

        // Set default role for existing users with role_id = 0 or null
        try {
            jdbcTemplate.execute("UPDATE users SET role_id = (SELECT id FROM roles WHERE name = 'CUSTOMER' LIMIT 1) WHERE role_id = 0 OR role_id IS NULL");
        } catch (Exception e) {
            // Ignore if database/tables are not ready yet
        }
    }

    private void initializeRole(String name, String description) {
        if (roleRepository.findByName(name).isEmpty()) {
            roleRepository.save(Role.builder()
                    .name(name)
                    .description(description)
                    .build());
        }
    }

    private void dropColumnQuietly(String tableName, String columnName) {
        try {
            jdbcTemplate.execute("ALTER TABLE " + tableName + " DROP COLUMN " + columnName);
        } catch (Exception e) {
            // Ignore if column doesn't exist or table doesn't exist yet
        }
    }
}
