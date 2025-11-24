package ru.practicum.shareit.user.dto;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.user.User;

@UtilityClass
public class UserMapper {

    public static UserDto toUserDto(User user) {
        return new UserDto(
                user.getId(),
                user.getName(),
                user.getEmail()
        );
    }

    public static User toUser(UserDto userDto) {
        return new User(
                userDto.id(),
                userDto.name(),
                userDto.email()
        );
    }

    public static User updateUserFromDto(User user, UserDto userDto) {
        if (userDto.name() != null && !userDto.name().isBlank()) {
            user.setName(userDto.name());
        }
        if (userDto.email() != null && !userDto.email().isBlank()) {
            user.setEmail(userDto.email());
        }
        return user;
    }

}