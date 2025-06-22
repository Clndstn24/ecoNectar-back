package com.econectar.api.user;

import com.econectar.api.auth.UserRegisterRequest;
import com.econectar.api.user.model.User;
import com.econectar.api.user.repository.UserRepository;
import com.econectar.api.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void shouldCreateUserFromRegisterRequest() {
        // GIVEN
        UserRegisterRequest request = new UserRegisterRequest();
        request.setFirstName("joel");
        request.setPassword("securepass");
        request.setEmail("joel@mail.com");

        // Simulamos que el mapper va a hacer esto (puede usarse real o mockearlo también)
        User mappedUser = new User();
        mappedUser.setFirstName("joel");
        mappedUser.setPassword("securepass");
        mappedUser.setEmail("joel@mail.com");

        UUID uuid = UUID.randomUUID();
        User savedUser = new User();
        savedUser.setId(uuid);
        savedUser.setFirstName("joel");
        savedUser.setEmail("joel@mail.com");

        // WHEN
        // Mockito espera que se llame a save con un user que tenga los datos mapeados
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        User result = userService.createUser(request);

        // THEN
        assertNotNull(result);
        assertEquals(uuid, result.getId());
        assertEquals("joel", result.getFirstName());
        assertEquals("joel@mail.com", result.getEmail());

        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldFindUserById() {
        UUID uuid = UUID.randomUUID();
        User user = new User();
        user.setId(uuid);

        when(userRepository.findById(1L)).thenReturn(java.util.Optional.of(user));

        User result = userService.findUserById(1L);

        assertNotNull(result);
        assertEquals(uuid, result.getId());
        verify(userRepository).findById(1L);
    }


}
