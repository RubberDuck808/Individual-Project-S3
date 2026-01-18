package nl.fontys.db3.backend.integration.controller;

import nl.fontys.db3.backend.entity.Role;
import nl.fontys.db3.backend.entity.User;
import nl.fontys.db3.backend.repository.RoleRepository;
import nl.fontys.db3.backend.repository.UserRepository;
import nl.fontys.db3.backend.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AdminControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Role adminRole;
    private Role userRole;
    private User adminUser;
    private User regularUser;
    private String adminToken;
    private String regularUserToken;

    @BeforeEach
    void setUp() {
        // Create ADMIN role
        adminRole = roleRepository.findByName("ADMIN")
                .orElseGet(() -> {
                    Role role = Role.builder().name("ADMIN").build();
                    return roleRepository.save(role);
                });

        // Create USER role
        userRole = roleRepository.findByName("USER")
                .orElseGet(() -> {
                    Role role = Role.builder().name("USER").build();
                    return roleRepository.save(role);
                });

        // Create admin user
        adminUser = User.builder()
                .username("admin")
                .email("admin@test.com")
                .name("Admin User")
                .password(passwordEncoder.encode("password123"))
                .role(adminRole)
                .build();
        adminUser = userRepository.save(adminUser);

        // Create regular user
        regularUser = User.builder()
                .username("regularuser")
                .email("regular@test.com")
                .name("Regular User")
                .password(passwordEncoder.encode("password123"))
                .role(userRole)
                .build();
        regularUser = userRepository.save(regularUser);

        // Generate tokens
        UserDetails adminDetails = userDetailsService.loadUserByUsername("admin@test.com");
        adminToken = jwtService.generateToken(adminDetails.getUsername(), Map.of());

        UserDetails userDetails = userDetailsService.loadUserByUsername("regular@test.com");
        regularUserToken = jwtService.generateToken(userDetails.getUsername(), Map.of());
    }

    @Test
    void getStatistics_success() throws Exception {
        mockMvc.perform(get("/api/admin/statistics")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists());
    }

    @Test
    void getStatistics_unauthorized() throws Exception {
        mockMvc.perform(get("/api/admin/statistics")
                        .header("Authorization", "Bearer " + regularUserToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAllUsers_success() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void getAllUsers_withPagination() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void getUserById_success() throws Exception {
        mockMvc.perform(get("/api/admin/users/" + regularUser.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(regularUser.getId()));
    }

    @Test
    void getUserById_notFound() throws Exception {
        mockMvc.perform(get("/api/admin/users/99999")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateUserRole_success() throws Exception {
        mockMvc.perform(put("/api/admin/users/" + regularUser.getId() + "/role")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("roleName", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roleName").value("ADMIN"));
    }

    @Test
    void updateUserRole_invalidRole() throws Exception {
        mockMvc.perform(put("/api/admin/users/" + regularUser.getId() + "/role")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("roleName", "INVALID_ROLE"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deactivateUser_success() throws Exception {
        mockMvc.perform(delete("/api/admin/users/" + regularUser.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void deactivateUser_notFound() throws Exception {
        mockMvc.perform(delete("/api/admin/users/99999")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }
}
