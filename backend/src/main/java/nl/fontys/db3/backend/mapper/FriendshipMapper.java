package nl.fontys.db3.backend.mapper;

import nl.fontys.db3.backend.dto.FriendshipDTO;
import nl.fontys.db3.backend.entity.Friendship;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FriendshipMapper {

    public FriendshipDTO toDTO(Friendship f) {
        return new FriendshipDTO(
                f.getStatus(),
                f.getCreatedAt(),
                f.getRequester().getUsername(),
                f.getAddressee().getUsername()
        );
    }

    public List<FriendshipDTO> toDTOList(List<Friendship> list) {
        return list.stream().map(this::toDTO).toList();
    }
}
