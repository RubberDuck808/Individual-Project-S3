package nl.fontys.db3.backend.mapper;

import nl.fontys.db3.backend.dto.HazardCategoryDTO;
import nl.fontys.db3.backend.entity.HazardCategory;
import nl.fontys.db3.backend.service.storage.StorageUrlService;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper(componentModel = "spring")
public abstract class HazardCategoryMapper {

    @SuppressWarnings("java:S6813") // MapStruct requires @Autowired for dependency injection in abstract mappers
    @Autowired
    protected StorageUrlService storageUrlService;

    @Mapping(target = "iconUrl",
             expression = "java(storageUrlService.publicUrlFromPath(entity.getIconPath()))")
    public abstract HazardCategoryDTO toDTO(HazardCategory entity);

    public abstract List<HazardCategoryDTO> toDTOList(List<HazardCategory> list);
}
