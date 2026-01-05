package nl.fontys.db3.backend.service;

import nl.fontys.db3.backend.dto.VoteDTO;
import nl.fontys.db3.backend.entity.HazardReport;
import nl.fontys.db3.backend.entity.User;
import nl.fontys.db3.backend.entity.Vote;
import nl.fontys.db3.backend.entity.VoteType;
import nl.fontys.db3.backend.entity.Statistics;
import nl.fontys.db3.backend.repository.HazardReportRepository;
import nl.fontys.db3.backend.repository.UserRepository;
import nl.fontys.db3.backend.repository.VoteRepository;
import nl.fontys.db3.backend.dto.UserStatsDTO;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VoteServiceTest {

    @Mock private VoteRepository voteRepo;
    @Mock private UserRepository userRepo;
    @Mock private HazardReportRepository hazardRepo;
    @Mock private StatisticsService statisticsService;

    @InjectMocks
    private VoteService service;

    @Test
    void getAllVotes_mapsEntitiesToDTOs() {
        // Arrange
        User u = mock(User.class);
        when(u.getId()).thenReturn(10L);

        HazardReport h = mock(HazardReport.class);
        when(h.getId()).thenReturn(20L);

        Vote v1 = mock(Vote.class);
        when(v1.getId()).thenReturn(1L);
        when(v1.getVoteType()).thenReturn(VoteType.UPVOTE);
        when(v1.getUser()).thenReturn(u);
        when(v1.getHazardReport()).thenReturn(h);

        Vote v2 = mock(Vote.class);
        when(v2.getId()).thenReturn(2L);
        when(v2.getVoteType()).thenReturn(VoteType.DOWNVOTE);
        when(v2.getUser()).thenReturn(u);
        when(v2.getHazardReport()).thenReturn(h);

        when(voteRepo.findAll()).thenReturn(List.of(v1, v2));

        // Act
        List<VoteDTO> result = service.getAllVotes();

        // Assert
        assertEquals(2, result.size());
        assertEquals("UPVOTE", result.get(0).getVoteType());
        assertEquals(10L, result.get(0).getUserId());
        assertEquals(20L, result.get(0).getHazardId());

        assertEquals("DOWNVOTE", result.get(1).getVoteType());
        verify(voteRepo).findAll();
    }

    @Test
    void voteAsDTO_success_savesVote_incrementsStats_andReturnsDto() {
        // Arrange
        String email = "user@test.com";
        Long hazardId = 99L;

        User user = mock(User.class);
        when(user.getId()).thenReturn(7L);
        when(userRepo.findByEmail(email)).thenReturn(Optional.of(user));

        // hazard creator is someone else
        User creator = mock(User.class);
        when(creator.getId()).thenReturn(123L);

        HazardReport hazard = mock(HazardReport.class);
        when(hazard.getId()).thenReturn(hazardId);
        when(hazard.getCreatedBy()).thenReturn(creator);
        when(hazardRepo.findById(hazardId)).thenReturn(Optional.of(hazard));

        when(voteRepo.existsByHazardReport_IdAndUser_Id(hazardId, 7L)).thenReturn(false);

        // capture what we save
        ArgumentCaptor<Vote> captor = ArgumentCaptor.forClass(Vote.class);
        when(voteRepo.save(captor.capture())).thenAnswer(inv -> {
            Vote saved = inv.getArgument(0);
            // if your Vote entity sets id via DB, mock it like below:
            // when(saved.getId()).thenReturn(1L);  <-- can't on real object
            return saved;
        });

        // Act
        VoteDTO dto = service.voteAsDTO(email, hazardId, VoteType.UPVOTE);

        // Assert
        Vote savedVote = captor.getValue();
        assertNotNull(savedVote);
        assertSame(user, savedVote.getUser());
        assertSame(hazard, savedVote.getHazardReport());
        assertEquals(VoteType.UPVOTE, savedVote.getVoteType());

        verify(statisticsService).incrementVotes(7L);

        // DTO mapping uses vote.getXxx(); since savedVote may have null id, we assert key fields
        assertEquals("UPVOTE", dto.getVoteType());
        assertEquals(7L, dto.getUserId());
        assertEquals(hazardId, dto.getHazardId());
    }

    @Test
    void voteAsDTO_hazardIdNull_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> service.voteAsDTO("user@test.com", null, VoteType.UPVOTE));

        verifyNoInteractions(userRepo, hazardRepo, voteRepo, statisticsService);
    }

    @Test
    void voteAsDTO_userNotFound_throws() {
        when(userRepo.findByEmail("user@test.com")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> service.voteAsDTO("user@test.com", 1L, VoteType.UPVOTE));

        verify(hazardRepo, never()).findById(anyLong());
        verify(voteRepo, never()).save(any());
        verify(statisticsService, never()).incrementVotes(anyLong());
    }

    @Test
    void voteAsDTO_hazardNotFound_throws() {
        // Arrange
        User user = mock(User.class);
        when(userRepo.findByEmail("user@test.com")).thenReturn(Optional.of(user));

        when(hazardRepo.findById(1L)).thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(IllegalArgumentException.class,
                () -> service.voteAsDTO("user@test.com", 1L, VoteType.UPVOTE));

        verify(voteRepo, never()).existsByHazardReport_IdAndUser_Id(anyLong(), anyLong());
        verify(voteRepo, never()).save(any());
        verify(statisticsService, never()).incrementVotes(anyLong());
    }


    @Test
    void voteAsDTO_creatorCannotVoteOnOwnReport_throws() {
        String email = "user@test.com";
        Long hazardId = 1L;

        User user = mock(User.class);
        when(user.getId()).thenReturn(7L);
        when(userRepo.findByEmail(email)).thenReturn(Optional.of(user));

        HazardReport hazard = mock(HazardReport.class);
        when(hazardRepo.findById(hazardId)).thenReturn(Optional.of(hazard));

        // createdBy has same id as user
        User createdBy = mock(User.class);
        when(createdBy.getId()).thenReturn(7L);
        when(hazard.getCreatedBy()).thenReturn(createdBy);

        assertThrows(IllegalArgumentException.class,
                () -> service.voteAsDTO(email, hazardId, VoteType.UPVOTE));

        verify(voteRepo, never()).save(any());
        verify(statisticsService, never()).incrementVotes(anyLong());
    }

    @Test
    void voteAsDTO_alreadyVoted_throws() {
        String email = "user@test.com";
        Long hazardId = 1L;

        User user = mock(User.class);
        when(user.getId()).thenReturn(7L);
        when(userRepo.findByEmail(email)).thenReturn(Optional.of(user));

        HazardReport hazard = mock(HazardReport.class);
        when(hazard.getCreatedBy()).thenReturn(null); // simplify
        when(hazardRepo.findById(hazardId)).thenReturn(Optional.of(hazard));

        when(voteRepo.existsByHazardReport_IdAndUser_Id(hazardId, 7L)).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> service.voteAsDTO(email, hazardId, VoteType.UPVOTE));

        verify(voteRepo, never()).save(any());
        verify(statisticsService, never()).incrementVotes(anyLong());
    }

    @Test
    void countVotes_delegatesToRepo() {
        when(voteRepo.countByHazardReport_IdAndVoteType(5L, VoteType.UPVOTE)).thenReturn(3L);

        long result = service.countVotes(5L, VoteType.UPVOTE);

        assertEquals(3L, result);
        verify(voteRepo).countByHazardReport_IdAndVoteType(5L, VoteType.UPVOTE);
    }

    @Test
    void getLifetimeVotesCastByUser_readsFromStatisticsService() {
        UserStatsDTO stats = mock(UserStatsDTO.class);
        when(stats.getTotalVotes()).thenReturn(42L); // <-- Long
        when(statisticsService.getStatsByUsername("bob")).thenReturn(stats);

        long result = service.getLifetimeVotesCastByUser("bob");

        assertEquals(42L, result);
        verify(statisticsService).getStatsByUsername("bob");
    }



    @Test
    void getMyVote_whenVoteExists_returnsMapWithVoteType() {
        String email = "user@test.com";
        Long hazardId = 5L;

        User user = mock(User.class);
        when(user.getId()).thenReturn(7L);
        when(userRepo.findByEmail(email)).thenReturn(Optional.of(user));

        Vote vote = mock(Vote.class);
        when(vote.getVoteType()).thenReturn(VoteType.DOWNVOTE);

        when(voteRepo.findByHazardReport_IdAndUser_Id(hazardId, 7L)).thenReturn(Optional.of(vote));

        Map<String, String> result = service.getMyVote(email, hazardId);

        assertEquals(Map.of("voteType", "DOWNVOTE"), result);
    }

    @Test
    void getMyVote_whenNoVote_returnsEmptyMap() {
        String email = "user@test.com";
        Long hazardId = 5L;

        User user = mock(User.class);
        when(user.getId()).thenReturn(7L);
        when(userRepo.findByEmail(email)).thenReturn(Optional.of(user));

        when(voteRepo.findByHazardReport_IdAndUser_Id(hazardId, 7L)).thenReturn(Optional.empty());

        Map<String, String> result = service.getMyVote(email, hazardId);

        assertTrue(result.isEmpty());
    }

    @Test
    void getMyVote_userNotFound_throws() {
        when(userRepo.findByEmail("user@test.com")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> service.getMyVote("user@test.com", 1L));

        verifyNoInteractions(voteRepo);
    }
}
