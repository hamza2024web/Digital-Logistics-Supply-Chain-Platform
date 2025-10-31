package com.spring.digital_logistics.mapper;

import com.spring.digital_logistics.dto.request.UserCreateDTO;
import com.spring.digital_logistics.dto.response.UserDTO;
import com.spring.digital_logistics.entity.User;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDTO toUserDTO(User user);

    User toUser(UserCreateDTO userCreateDTO);
}
