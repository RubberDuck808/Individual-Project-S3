package nl.fontys.db3.backend.integration.repository;

import nl.fontys.db3.backend.entity.HazardCategory;
import nl.fontys.db3.backend.entity.HazardReport;
import nl.fontys.db3.backend.entity.HazardStatus;
import nl.fontys.db3.backend.entity.Role;
import nl.fontys.db3.backend.entity.User;
import nl.fontys.db3.backend.entity.Vote;
import nl.fontys.db3.backend.entity.VoteType;
import nl.fontys.db3.backend.repository.HazardCategoryRepository;
import nl.fontys.db3.backend.repository.HazardReportRepository;
import nl.fontys.db3.backend.repository.RoleRepository;
import nl.fontys.db3.backend.repository.UserRepository;
import nl.fontys.db3.backend.repository.VoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@AutoConfigureTestDatabase(replace = Replace.NONE)
@ActiveProfiles("test")
@DataJpaTest(excludeAutoConfiguration = {
    org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
    org.springframework.boot.autoconfigure.websocket.servlet.WebSocketServletAutoConfiguration.class
})
class VoteRepositoryIT {

    @Autowired
    VoteRepository voteRepository;

    @Autowired
    HazardReportRepository hazardReportRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    HazardCategoryRepository hazardCategoryRepository;

    private User user1;
    private User user2;
    private HazardReport hazard;

    @BeforeEach
    void setUp() {
        try {
            voteRepository.deleteAll();
            hazardReportRepository.deleteAll();
            userRepository.deleteAll();
            hazardCategoryRepository.deleteAll();
            roleRepository.deleteAll();
        } catch (Exception ignored) {
            // Tables may not exist yet
        }

        Role role = Role.builder().name("USER").build();
        roleRepository.save(role);

        user1 = User.builder()
                .username("voter1")
                .email("voter1@test.com")
                .name("Voter 1")
                .password("encoded")
                .role(role)
                .build();
        userRepository.save(user1);

        user2 = User.builder()
                .username("voter2")
                .email("voter2@test.com")
                .name("Voter 2")
                .password("encoded")
                .role(role)
                .build();
        userRepository.save(user2);

        HazardCategory category = HazardCategory.builder()
                .name("Pothole")
                .iconPath("/icons/pothole.svg")
                .active(true)
                .build();
        hazardCategoryRepository.save(category);

        hazard = HazardReport.builder()
                .latitude(51.4416)
                .longitude(5.4697)
                .category(category)
                .createdBy(user1)
                .status(HazardStatus.OPEN)
                .build();
        hazardReportRepository.save(hazard);
    }

    @Test
    void saveAndFindById() {
        Vote vote = Vote.builder()
                .voteType(VoteType.UPVOTE)
                .user(user1)
                .hazardReport(hazard)
                .build();

        Vote saved = voteRepository.save(vote);
        assertNotNull(saved.getId());
        assertNotNull(saved.getCreatedAt());

        Vote found = voteRepository.findById(saved.getId()).orElseThrow();
        assertEquals(VoteType.UPVOTE, found.getVoteType());
        assertEquals(user1.getId(), found.getUser().getId());
        assertEquals(hazard.getId(), found.getHazardReport().getId());
    }

    @Test
    void findByHazardReport_Id() {
        Vote vote1 = Vote.builder()
                .voteType(VoteType.UPVOTE)
                .user(user1)
                .hazardReport(hazard)
                .build();
        voteRepository.save(vote1);

        Vote vote2 = Vote.builder()
                .voteType(VoteType.DOWNVOTE)
                .user(user2)
                .hazardReport(hazard)
                .build();
        voteRepository.save(vote2);

        List<Vote> votes = voteRepository.findByHazardReport_Id(hazard.getId());
        assertEquals(2, votes.size());
    }

    @Test
    void existsByHazardReport_IdAndUser_Id_Exists() {
        Vote vote = Vote.builder()
                .voteType(VoteType.UPVOTE)
                .user(user1)
                .hazardReport(hazard)
                .build();
        voteRepository.save(vote);

        boolean exists = voteRepository.existsByHazardReport_IdAndUser_Id(hazard.getId(), user1.getId());
        assertTrue(exists);
    }

    @Test
    void existsByHazardReport_IdAndUser_Id_NotExists() {
        boolean exists = voteRepository.existsByHazardReport_IdAndUser_Id(hazard.getId(), user1.getId());
        assertFalse(exists);
    }

    @Test
    void countByHazardReport_IdAndVoteType() {
        Vote upvote1 = Vote.builder()
                .voteType(VoteType.UPVOTE)
                .user(user1)
                .hazardReport(hazard)
                .build();
        voteRepository.save(upvote1);

        Vote upvote2 = Vote.builder()
                .voteType(VoteType.UPVOTE)
                .user(user2)
                .hazardReport(hazard)
                .build();
        voteRepository.save(upvote2);

        // Create a third user for downvote (same user can't vote twice on same hazard)
        User user3 = User.builder()
                .username("voter3")
                .email("voter3@test.com")
                .name("Voter 3")
                .password("encoded")
                .role(user1.getRole())
                .build();
        userRepository.save(user3);

        Vote downvote = Vote.builder()
                .voteType(VoteType.DOWNVOTE)
                .user(user3)
                .hazardReport(hazard)
                .build();
        voteRepository.save(downvote);

        long upvoteCount = voteRepository.countByHazardReport_IdAndVoteType(hazard.getId(), VoteType.UPVOTE);
        assertEquals(2, upvoteCount);

        long downvoteCount = voteRepository.countByHazardReport_IdAndVoteType(hazard.getId(), VoteType.DOWNVOTE);
        assertEquals(1, downvoteCount);
    }

    @Test
    void findByHazardReport_IdAndUser_Id() {
        Vote vote = Vote.builder()
                .voteType(VoteType.UPVOTE)
                .user(user1)
                .hazardReport(hazard)
                .build();
        voteRepository.save(vote);

        Optional<Vote> found = voteRepository.findByHazardReport_IdAndUser_Id(hazard.getId(), user1.getId());
        assertTrue(found.isPresent());
        assertEquals(VoteType.UPVOTE, found.get().getVoteType());
    }

    @Test
    void findByHazardReport_IdAndUser_Id_NotFound() {
        Optional<Vote> found = voteRepository.findByHazardReport_IdAndUser_Id(hazard.getId(), user1.getId());
        assertFalse(found.isPresent());
    }

    @Test
    void countVotesCastByUser() {
        HazardCategory category2 = HazardCategory.builder()
                .name("Debris")
                .iconPath("/icons/debris.svg")
                .active(true)
                .build();
        hazardCategoryRepository.save(category2);

        HazardReport hazard2 = HazardReport.builder()
                .latitude(51.4500)
                .longitude(5.4800)
                .category(category2)
                .createdBy(user2)
                .status(HazardStatus.OPEN)
                .build();
        hazardReportRepository.save(hazard2);

        Vote vote1 = Vote.builder()
                .voteType(VoteType.UPVOTE)
                .user(user1)
                .hazardReport(hazard)
                .build();
        voteRepository.save(vote1);

        Vote vote2 = Vote.builder()
                .voteType(VoteType.DOWNVOTE)
                .user(user1)
                .hazardReport(hazard2)
                .build();
        voteRepository.save(vote2);

        long count = voteRepository.countVotesCastByUser(user1.getUsername());
        assertEquals(2, count);
    }

    @Test
    void uniqueConstraint_HazardAndUser() {
        Vote vote1 = Vote.builder()
                .voteType(VoteType.UPVOTE)
                .user(user1)
                .hazardReport(hazard)
                .build();
        voteRepository.save(vote1);

        Vote vote2 = Vote.builder()
                .voteType(VoteType.DOWNVOTE)
                .user(user1)
                .hazardReport(hazard)
                .build();

        assertThrows(Exception.class, () -> voteRepository.saveAndFlush(vote2));
    }
}
