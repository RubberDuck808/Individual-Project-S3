package nl.fontys.db3.backend.e2e;

import com.fasterxml.jackson.databind.ObjectMapper;
import nl.fontys.db3.backend.entity.HazardCategory;
import nl.fontys.db3.backend.entity.Role;
import nl.fontys.db3.backend.repository.HazardCategoryRepository;
import nl.fontys.db3.backend.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Base class for E2E tests providing common setup and utilities.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public abstract class BaseE2ETest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected RoleRepository roleRepository;

    @Autowired
    protected HazardCategoryRepository hazardCategoryRepository;

    protected HazardCategory hazardCategory;

    protected void setUpBase() {
        // Ensure USER role exists
        roleRepository.findByName("USER")
                .orElseGet(() -> {
                    Role role = Role.builder().name("USER").build();
                    return roleRepository.save(role);
                });

        // Setup hazard category
        hazardCategory = hazardCategoryRepository.findAll().stream()
                .findFirst()
                .orElseGet(() -> {
                    HazardCategory category = HazardCategory.builder()
                            .name("Pothole")
                            .iconPath("/icons/pothole.svg")
                            .active(true)
                            .build();
                    return hazardCategoryRepository.save(category);
                });
    }
}
