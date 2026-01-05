package nl.fontys.db3.backend.mapper;

import nl.fontys.db3.backend.dto.PublicUserDTO;
import nl.fontys.db3.backend.dto.UserDTO;
import nl.fontys.db3.backend.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    // /me -> full user info
    @Mapping(source = "role.name", target = "roleName")
    @Mapping(target = "createdAt", expression = "java(user.getCreatedAt() != null ? user.getCreatedAt().toString() : null)")
    UserDTO toUserDTO(User user);

    // public profile -> safe info only
    @Mapping(source = "username", target = "username")
    @Mapping(source = "name", target = "name")
    PublicUserDTO toPublicUserDTO(User user);
}
