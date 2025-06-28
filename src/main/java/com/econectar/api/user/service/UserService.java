package com.econectar.api.user.service;

import com.econectar.api.auth.UserRegisterRequest;
import com.econectar.api.shared.exception.EmailAlreadyExistsException;
import com.econectar.api.shared.exception.UserCreationException;
import com.econectar.api.shared.exception.UserNotFoundException;
import com.econectar.api.user.dto.UserDTO;
import com.econectar.api.user.model.Role;
import com.econectar.api.user.model.User;
import com.econectar.api.user.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, ModelMapper modelMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.modelMapper = modelMapper;
    }

    public void createUser(UserRegisterRequest userRequest) {
        // Validar si el email ya existe
        if (userRepository.findByEmail(userRequest.getEmail()) != null) {
            throw new EmailAlreadyExistsException("El correo electrónico ya está registrado: " + userRequest.getEmail());
        }
        try {
            User newUser = getNewUser(userRequest);
            userRepository.save(newUser);
        }
        catch (Exception e) {
            throw new UserCreationException("Error al crear usuario: " + userRequest.getEmail(), e);
        }
    }

    private User getNewUser(UserRegisterRequest user) {
        User newUser = modelMapper.map(user, User.class);
        newUser.setPassword(passwordEncoder.encode(user.getPassword()));
        if (newUser.getRole() == null) {
            newUser.setRole(Role.USER); // Asignar rol USER por defecto si no se especifica
        }
        return newUser;
    }

    public User findUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
    }

    public UserDTO findUserByEmail(String email) {
        try {
            User user = userRepository.findByEmail(email);
            if (user == null) {
                throw new UserNotFoundException("User not found with email: " + email);
            }
            return modelMapper.map(user, UserDTO.class);
        }
        catch (Exception e) {
            throw new UserNotFoundException("Error retrieving user by email: " + email, e);
        }
    }

    @Override
    public User loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found with email: " + username);
        }
        return user;
    }
}
