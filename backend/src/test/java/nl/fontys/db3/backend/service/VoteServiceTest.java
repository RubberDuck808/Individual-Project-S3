package nl.fontys.db3.backend.service;

import nl.fontys.db3.backend.dto.UserStatsDTO;
import nl.fontys.db3.backend.dto.VoteDTO;
import nl.fontys.db3.backend.entity.*;
import nl.fontys.db3.backend.repository.HazardReportRepository;
import nl.fontys.db3.backend.repository.UserRepository;
import nl.fontys.db3.backend.repository.VoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VoteServiceTest {

    @Mock
    private VoteRepository voteRepo;

    @Mock
    private UserRepository userRepo;

    @Mock
    private HazardReportRepository hazardRepo;

    @Mock
    private StatisticsService statisticsService;

    @InjectMocks
    private VoteService voteService;

    private User testUser;
    private User hazardCreator;
    private Role userRole;
    private HazardCategory category;
    private HazardReport hazard;
    private Vote vote;

    @BeforeEach
    void setUp() {
        userRole = new Role(1L, "USER");
        testUser = User.builder()
                .id(1L)
                .username("voter")
                .email("voter@example.com")
                .name("Voter User")
                .password("encoded")
                .role(userRole)
                .build();

        hazardCreator = User.builder()
                .id(2L)
                .username("creator")
                .email("creator@example.com")
                .name("Creator User")
                .password("encoded")
                .role(userRole)
                .build();

        category = HazardCategory.builder()
                .id(1L)
                .name("Pothole")
                .iconPath("/icons/pothole.png")
                .active(true)
                .build();

        hazard = HazardReport.builder()
                .id(1L)
                .latitude(52.0)
                .longitude(5.0)
                .category(category)
                .createdBy(hazardCreator)
                .status(HazardStatus.OPEN)
                .build();

        vote = Vote.builder()
                .id(1L)
                .hazardReport(hazard)
                .user(testUser)
                .voteType(VoteType.UPVOTE)
                .build();
    }

    @Test
    void getAllVotes_success() {
        // Given
        Vote vote2 = Vote.builder()
                .id(2L)
                .hazardReport(hazard)
                .user(hazardCreator)
                .voteType(VoteType.DOWNVOTE)
                .build();
        PageRequest pageable = PageRequest.of(0, 50);
        Page<Vote> votePage = new PageImpl<>(List.of(vote, vote2), pageable, 2);
        when(voteRepo.findAll(pageable)).thenReturn(votePage);

        // When
        Page<VoteDTO> result = voteService.getAllVotes(pageable);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals(1L, result.getContent().get(0).getId());
        assertEquals("UPVOTE", result.getContent().get(0).getVoteType());
        verify(voteRepo).findAll(pageable);
    }

    @Test
    void voteAsDTO_success() {
        // Given
        when(userRepo.findByEmail("voter@example.com")).thenReturn(Optional.of(testUser));
        when(hazardRepo.findById(1L)).thenReturn(Optional.of(hazard));
        when(voteRepo.existsByHazardReport_IdAndUser_Id(1L, 1L)).thenReturn(false);
        when(voteRepo.save(any(Vote.class))).thenAnswer(invocation -> {
            Vote v = invocation.getArgument(0);
            v.setId(1L);
            return v;
        });

        // When
        VoteDTO result = voteService.voteAsDTO("voter@example.com", 1L, VoteType.UPVOTE);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("UPVOTE", result.getVoteType());
        assertEquals(1L, result.getUserId());
        assertEquals(1L, result.getHazardId());
        verify(userRepo).findByEmail("voter@example.com");
        verify(hazardRepo).findById(1L);
        verify(voteRepo).existsByHazardReport_IdAndUser_Id(1L, 1L);
        verify(voteRepo).save(any(Vote.class));
        verify(statisticsService).incrementVotes(1L);
    }


    @Test
    void voteAsDTO_userNotFound_throwsException() {
        // Given
        when(userRepo.findByEmail("voter@example.com")).thenReturn(Optional.empty());

        // When/Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> voteService.voteAsDTO("voter@example.com", 1L, VoteType.UPVOTE));
        assertEquals("User not found", exception.getMessage());
        verify(userRepo).findByEmail("voter@example.com");
        verify(hazardRepo, never()).findById(any());
    }

    @Test
    void voteAsDTO_hazardNotFound_throwsException() {
        // Given
        when(userRepo.findByEmail("voter@example.com")).thenReturn(Optional.of(testUser));
        when(hazardRepo.findById(1L)).thenReturn(Optional.empty());

        // When/Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> voteService.voteAsDTO("voter@example.com", 1L, VoteType.UPVOTE));
        assertEquals("Hazard not found", exception.getMessage());
        verify(userRepo).findByEmail("voter@example.com");
        verify(hazardRepo).findById(1L);
        verify(voteRepo, never()).existsByHazardReport_IdAndUser_Id(any(), any());
    }

    @Test
    void voteAsDTO_ownReport_throwsException() {
        // Given
        hazard.setCreatedBy(testUser); // Same user as voter
        when(userRepo.findByEmail("voter@example.com")).thenReturn(Optional.of(testUser));
        when(hazardRepo.findById(1L)).thenReturn(Optional.of(hazard));

        // When/Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> voteService.voteAsDTO("voter@example.com", 1L, VoteType.UPVOTE));
        assertEquals("You cannot vote on your own report", exception.getMessage());
        verify(userRepo).findByEmail("voter@example.com");
        verify(hazardRepo).findById(1L);
        verify(voteRepo, never()).existsByHazardReport_IdAndUser_Id(any(), any());
        verify(voteRepo, never()).save(any());
    }

    @Test
    void voteAsDTO_alreadyVoted_throwsException() {
        // Given
        when(userRepo.findByEmail("voter@example.com")).thenReturn(Optional.of(testUser));
        when(hazardRepo.findById(1L)).thenReturn(Optional.of(hazard));
        when(voteRepo.existsByHazardReport_IdAndUser_Id(1L, 1L)).thenReturn(true);

        // When/Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> voteService.voteAsDTO("voter@example.com", 1L, VoteType.UPVOTE));
        assertEquals("You already voted on this report", exception.getMessage());
        verify(userRepo).findByEmail("voter@example.com");
        verify(hazardRepo).findById(1L);
        verify(voteRepo).existsByHazardReport_IdAndUser_Id(1L, 1L);
        verify(voteRepo, never()).save(any());
    }

    @Test
    void voteAsDTO_removeVoteType_success() {
        // Given
        when(userRepo.findByEmail("voter@example.com")).thenReturn(Optional.of(testUser));
        when(hazardRepo.findById(1L)).thenReturn(Optional.of(hazard));
        when(voteRepo.existsByHazardReport_IdAndUser_Id(1L, 1L)).thenReturn(false);
        when(voteRepo.save(any(Vote.class))).thenAnswer(invocation -> {
            Vote v = invocation.getArgument(0);
            v.setId(1L);
            return v;
        });

        // When
        VoteDTO result = voteService.voteAsDTO("voter@example.com", 1L, VoteType.DOWNVOTE);

        // Then
        assertNotNull(result);
        assertEquals("DOWNVOTE", result.getVoteType());
        verify(voteRepo).save(any(Vote.class));
    }

    @Test
    void countVotes_success() {
        // Given
        when(voteRepo.countByHazardReport_IdAndVoteType(1L, VoteType.UPVOTE)).thenReturn(5L);

        // When
        long result = voteService.countVotes(1L, VoteType.UPVOTE);

        // Then
        assertEquals(5L, result);
        verify(voteRepo).countByHazardReport_IdAndVoteType(1L, VoteType.UPVOTE);
    }

    @Test
    void countVotes_downvoteType_success() {
        // Given
        when(voteRepo.countByHazardReport_IdAndVoteType(1L, VoteType.DOWNVOTE)).thenReturn(2L);

        // When
        long result = voteService.countVotes(1L, VoteType.DOWNVOTE);

        // Then
        assertEquals(2L, result);
        verify(voteRepo).countByHazardReport_IdAndVoteType(1L, VoteType.DOWNVOTE);
    }

    @Test
    void getLifetimeVotesCastByUser_success() {
        // Given
        UserStatsDTO stats = UserStatsDTO.builder()
                .totalTrips(10)
                .totalDistanceKm(100.0)
                .totalHazardsReported(5)
                .totalVotes(15L)
                .build();

        when(statisticsService.getStatsByUsername("voter")).thenReturn(stats);

        // When
        long result = voteService.getLifetimeVotesCastByUser("voter");

        // Then
        assertEquals(15L, result);
        verify(statisticsService).getStatsByUsername("voter");
    }

    @Test
    void getMyVote_voteExists_success() {
        // Given
        when(userRepo.findByEmail("voter@example.com")).thenReturn(Optional.of(testUser));
        when(voteRepo.findByHazardReport_IdAndUser_Id(1L, 1L)).thenReturn(Optional.of(vote));

        // When
        Map<String, String> result = voteService.getMyVote("voter@example.com", 1L);

        // Then
        assertNotNull(result);
        assertEquals("UPVOTE", result.get("voteType"));
        verify(userRepo).findByEmail("voter@example.com");
        verify(voteRepo).findByHazardReport_IdAndUser_Id(1L, 1L);
    }

    @Test
    void getMyVote_noVote_success() {
        // Given
        when(userRepo.findByEmail("voter@example.com")).thenReturn(Optional.of(testUser));
        when(voteRepo.findByHazardReport_IdAndUser_Id(1L, 1L)).thenReturn(Optional.empty());

        // When
        Map<String, String> result = voteService.getMyVote("voter@example.com", 1L);

        // Then
        assertNotNull(result);
        assertEquals("NONE", result.get("voteType"));
        verify(userRepo).findByEmail("voter@example.com");
        verify(voteRepo).findByHazardReport_IdAndUser_Id(1L, 1L);
    }

    @Test
    void getMyVote_userNotFound_throwsException() {
        // Given
        when(userRepo.findByEmail("voter@example.com")).thenReturn(Optional.empty());

        // When/Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> voteService.getMyVote("voter@example.com", 1L));
        assertEquals("User not found", exception.getMessage());
        verify(userRepo).findByEmail("voter@example.com");
        verify(voteRepo, never()).findByHazardReport_IdAndUser_Id(any(), any());
    }

    @Test
    void getMyVote_downvoteType_success() {
        // Given
        vote.setVoteType(VoteType.DOWNVOTE);
        when(userRepo.findByEmail("voter@example.com")).thenReturn(Optional.of(testUser));
        when(voteRepo.findByHazardReport_IdAndUser_Id(1L, 1L)).thenReturn(Optional.of(vote));

        // When
        Map<String, String> result = voteService.getMyVote("voter@example.com", 1L);

        // Then
        assertNotNull(result);
        assertEquals("DOWNVOTE", result.get("voteType"));
    }
}
