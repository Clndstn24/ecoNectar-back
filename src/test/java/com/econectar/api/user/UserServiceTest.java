package com.econectar.api.user;

import com.econectar.api.auth.UserRegisterRequest;
import com.econectar.api.user.dto.UserDTO;
import com.econectar.api.user.model.User;
import com.econectar.api.user.repository.UserRepository;
import com.econectar.api.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private UserService userService;

    @Test
    void shouldCreateUserFromRegisterRequest() {
        // GIVEN
        UserRegisterRequest request = new UserRegisterRequest();
        request.setFirstName("joel");
        request.setPassword("securepass");
        request.setEmail("joel@mail.com");

        User mappedUser = new User();
        mappedUser.setFirstName("joel");
        mappedUser.setPassword("securepass");
        mappedUser.setEmail("joel@mail.com");

        UUID uuid = UUID.randomUUID();
        User savedUser = new User();
        savedUser.setId(uuid);
        savedUser.setFirstName("joel");
        savedUser.setEmail("joel@mail.com");

        // Mock del passwordEncoder
        when(passwordEncoder.encode(any())).thenReturn("hashedpass");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // WHEN
        User result = userService.createUser(request);

        // THEN
        assertNotNull(result);
        assertEquals("joel", result.getFirstName());
        assertEquals("joel@mail.com", result.getEmail());
        verify(passwordEncoder).encode("securepass");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldFindUserById() {
        UUID uuid = UUID.randomUUID();
        User user = new User();
        user.setId(uuid);

        when(userRepository.findById(uuid)).thenReturn(java.util.Optional.of(user));

        User result = userService.findUserById(uuid);

        assertNotNull(result);
        assertEquals(uuid, result.getId());
        verify(userRepository).findById(uuid);
    }

    @Test
    void shouldFindUserDTOByEmail() {
        // GIVEN
        String email = "test@example.com";
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail(email);
        user.setFirstName("Test");
        user.setLastName("User");

        UserDTO mockDto = new UserDTO();
        mockDto.setFirstName("Test");
        mockDto.setLastName("User");

        // Mock repository
        when(userRepository.findByEmail(email)).thenReturn(user);
        when(modelMapper.map(user, UserDTO.class)).thenReturn(mockDto);

        // WHEN
        UserDTO result = userService.findUserByEmail(email);

        // THEN
        assertNotNull(result);
        assertEquals("Test", result.getFirstName());
        assertEquals("User", result.getLastName());
        verify(userRepository).findByEmail(email);
        verify(modelMapper).map(user, UserDTO.class);
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        // GIVEN
        String email = "nonexistent@example.com";
        when(userRepository.findByEmail(email)).thenReturn(null);

        // WHEN & THEN
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.findUserByEmail(email);
        });

        assertTrue(exception.getMessage().contains("Error retrieving user by email"));
        assertTrue(exception.getCause() instanceof UsernameNotFoundException);
        verify(userRepository).findByEmail(email);
    }

    @Test
    void shouldWrapExceptionWhenRepositoryThrowsException() {
        // GIVEN
        String email = "test@example.com";
        when(userRepository.findByEmail(email)).thenThrow(new RuntimeException("Database error"));

        // WHEN & THEN
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.findUserByEmail(email);
        });

        assertTrue(exception.getMessage().contains("Error retrieving user by email: " + email));
        verify(userRepository).findByEmail(email);
    }


}
