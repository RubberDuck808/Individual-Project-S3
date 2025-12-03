package nl.fontys.db3.backend.mapper;

import nl.fontys.db3.backend.dto.VoteDTO;
import nl.fontys.db3.backend.entity.Vote;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface VoteMapper {

    @Mapping(target = "voteType", source = "voteType") // enum->String handled automatically
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "hazardId", source = "hazardReport.id")
    VoteDTO toDTO(Vote vote);
}
