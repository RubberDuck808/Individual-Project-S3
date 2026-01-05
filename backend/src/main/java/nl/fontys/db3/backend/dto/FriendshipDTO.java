package nl.fontys.db3.backend.dto;

import nl.fontys.db3.backend.entity.FriendshipStatus;

import java.time.LocalDateTime;

public record FriendshipDTO(
        FriendshipStatus status,
        LocalDateTime createdAt,
        String requesterUsername,
        String addresseeUsername
) {}
