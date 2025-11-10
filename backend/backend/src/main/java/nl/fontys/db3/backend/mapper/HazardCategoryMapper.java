package nl.fontys.db3.backend.mapper;

import nl.fontys.db3.backend.dto.HazardCategoryDTO;
import nl.fontys.db3.backend.entity.HazardCategory;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface HazardCategoryMapper {

    HazardCategoryDTO toDTO(HazardCategory entity);

    HazardCategory toEntity(HazardCategoryDTO dto);

    List<HazardCategoryDTO> toDTOList(List<HazardCategory> list);
}
