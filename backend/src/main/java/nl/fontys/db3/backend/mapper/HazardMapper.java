package nl.fontys.db3.backend.mapper;

import nl.fontys.db3.backend.dto.HazardReportDTO;
import nl.fontys.db3.backend.entity.HazardReport;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface HazardMapper {

    @Mapping(source = "category.name", target = "category")
    @Mapping(source = "createdBy.username", target = "createdBy")
    @Mapping(target = "upvotes", expression = "java(hazard.getUpvoteCount())")
    @Mapping(target = "downvotes", expression = "java(hazard.getDownvoteCount())")
    @Mapping(target = "score", expression = "java(hazard.getScore())")
    HazardReportDTO toDTO(HazardReport hazard);

    List<HazardReportDTO> toDTOList(List<HazardReport> hazards);
}
