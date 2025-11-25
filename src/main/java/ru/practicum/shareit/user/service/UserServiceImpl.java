package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.repository.InMemoryUserRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final InMemoryUserRepository userRepository;

    @Override
    public UserDto createUser(UserDto userDto) {
        log.debug("Creating user with email: {}", userDto.email());
        validateEmail(userDto.email(), null);

        User user = UserMapper.toUser(userDto);
        user = userRepository.save(user);

        log.info("User created with id: {}", user.getId());
        return UserMapper.toUserDto(user);
    }

    @Override
    public UserDto updateUser(Long userId, UserDto userDto) {
        log.debug("Updating user with id: {}", userId);
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " not found"));

        if (userDto.email() != null && !userDto.email().isBlank()) {
            validateEmail(userDto.email(), userId);
        }

        User updatedUser = UserMapper.updateUserFromDto(existingUser, userDto);
        updatedUser = userRepository.update(updatedUser);

        log.info("User updated with id: {}", userId);
        return UserMapper.toUserDto(updatedUser);
    }

    @Override
    public UserDto getUserById(Long userId) {
        log.debug("Getting user by id: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " not found"));
        return UserMapper.toUserDto(user);
    }

    @Override
    public List<UserDto> getAllUsers() {
        log.debug("Getting all users");
        return userRepository.findAll().stream()
                .map(UserMapper::toUserDto)
                .toList();
    }

    @Override
    public void deleteUser(Long userId) {
        log.debug("Deleting user with id: {}", userId);
        if (userRepository.findById(userId).isEmpty()) {
            throw new NotFoundException("User with id=" + userId + " not found");
        }
        userRepository.deleteById(userId);
        log.info("User deleted with id: {}", userId);
    }

    private void validateEmail(String email, Long userId) {
        if (userId == null) {
            if (userRepository.existsByEmail(email)) {
                throw new ConflictException("User with email=" + email + " already exists");
            }
        } else {
            if (userRepository.existsByEmailAndIdNot(email, userId)) {
                throw new ConflictException("User with email=" + email + " already exists");
            }
        }
    }
}