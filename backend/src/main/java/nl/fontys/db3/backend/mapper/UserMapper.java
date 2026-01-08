package nl.fontys.db3.backend.mapper;

import nl.fontys.db3.backend.dto.PublicUserDTO;
import nl.fontys.db3.backend.dto.UserDTO;
import nl.fontys.db3.backend.entity.User;
import nl.fontys.db3.backend.service.storage.StorageUrlService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public abstract class UserMapper {

    @Autowired
    protected StorageUrlService storageUrlService;

    @Mapping(source = "role.name", target = "roleName")
    @Mapping(target = "createdAt",
            expression = "java(user.getCreatedAt() != null ? user.getCreatedAt().toString() : null)")
    @Mapping(target = "avatarName",
            expression = "java(user.getAvatar() != null ? user.getAvatar().getName() : null)")
    @Mapping(target = "avatarUrl",
            expression = "java(storageUrlService.publicUrlFromPath(user.getAvatar() != null ? user.getAvatar().getImagePath() : null))")
    @Mapping(target = "backgroundName",
            expression = "java(user.getBackground() != null ? user.getBackground().getName() : null)")
    @Mapping(target = "backgroundUrl",
            expression = "java(storageUrlService.publicUrlFromPath(user.getBackground() != null ? user.getBackground().getImagePath() : null))")
    public abstract UserDTO toUserDTO(User user);

    @Mapping(source = "username", target = "username")
    @Mapping(source = "name", target = "name")
    @Mapping(target = "avatarName",
            expression = "java(user.getAvatar() != null ? user.getAvatar().getName() : null)")
    @Mapping(target = "avatarUrl",
            expression = "java(storageUrlService.publicUrlFromPath(user.getAvatar() != null ? user.getAvatar().getImagePath() : null))")
    @Mapping(target = "backgroundName",
            expression = "java(user.getBackground() != null ? user.getBackground().getName() : null)")
    @Mapping(target = "backgroundUrl",
            expression = "java(storageUrlService.publicUrlFromPath(user.getBackground() != null ? user.getBackground().getImagePath() : null))")
    public abstract PublicUserDTO toPublicUserDTO(User user);
}
