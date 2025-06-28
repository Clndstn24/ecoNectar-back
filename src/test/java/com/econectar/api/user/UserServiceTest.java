package com.econectar.api.user;

import com.econectar.api.auth.UserRegisterRequest;
import com.econectar.api.shared.exception.EmailAlreadyExistsException;
import com.econectar.api.shared.exception.UserCreationException;
import com.econectar.api.shared.exception.UserNotFoundException;
import com.econectar.api.user.dto.UserDTO;
import com.econectar.api.user.model.Role;
import com.econectar.api.user.model.User;
import com.econectar.api.user.repository.UserRepository;
import com.econectar.api.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UUID userId;
    private UserRegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        userId = UUID.randomUUID();
        testUser = new User();
        testUser.setId(userId);
        testUser.setEmail("test@example.com");
        testUser.setPassword("encoded_password");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setRole(Role.USER);

        registerRequest = new UserRegisterRequest();
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setFirstName("Test");
        registerRequest.setLastName("User");

        when(passwordEncoder.encode(anyString())).thenReturn("encoded_password");
        when(modelMapper.map(any(UserRegisterRequest.class), eq(User.class))).thenReturn(testUser);
    }

    @Test
    @DisplayName("Crear usuario exitosamente")
    void createUser_Success() {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(null);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        assertDoesNotThrow(() -> userService.createUser(registerRequest));

        // Assert
        verify(userRepository).findByEmail("test@example.com");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Crear usuario con email existente lanza EmailAlreadyExistsException")
    void createUser_WithExistingEmail_ThrowsEmailAlreadyExistsException() {
        // Arrange
        when(userRepository.findByEmail("test@example.com")).thenReturn(testUser);

        // Act & Assert
        assertThrows(EmailAlreadyExistsException.class, () -> userService.createUser(registerRequest));
        verify(userRepository).findByEmail("test@example.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Buscar usuario por ID exitosamente")
    void findUserById_Success() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        // Act
        User result = userService.findUserById(userId);

        // Assert
        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals("test@example.com", result.getEmail());
    }

    @Test
    @DisplayName("Buscar usuario por ID inexistente lanza RuntimeException")
    void findUserById_NonExistent_ThrowsRuntimeException() {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        when(userRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> userService.findUserById(nonExistentId));
    }

    @Test
    @DisplayName("Buscar usuario por email exitosamente")
    void findUserByEmail_Success() {
        // Arrange
        when(userRepository.findByEmail("test@example.com")).thenReturn(testUser);

        UserDTO userDTO = new UserDTO();
        userDTO.setEmail("test@example.com");
        when(modelMapper.map(testUser, UserDTO.class)).thenReturn(userDTO);

        // Act
        UserDTO result = userService.findUserByEmail("test@example.com");

        // Assert
        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
    }

    @Test
    @DisplayName("Buscar usuario por email inexistente lanza UsernameNotFoundException")
    void findUserByEmail_NonExistent_ThrowsUsernameNotFoundException() {
        // Arrange
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(null);

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> userService.findUserByEmail("nonexistent@example.com"));
    }

    @Test
    @DisplayName("LoadUserByUsername exitosamente")
    void loadUserByUsername_Success() {
        // Arrange
        when(userRepository.findByEmail("test@example.com")).thenReturn(testUser);

        // Act
        User result = userService.loadUserByUsername("test@example.com");

        // Assert
        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
    }

    @Test
    @DisplayName("LoadUserByUsername con email inexistente lanza UsernameNotFoundException")
    void loadUserByUsername_NonExistent_ThrowsUsernameNotFoundException() {
        // Arrange
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(null);

        // Act & Assert
        assertThrows(UsernameNotFoundException.class, () -> userService.loadUserByUsername("nonexistent@example.com"));
    }
}