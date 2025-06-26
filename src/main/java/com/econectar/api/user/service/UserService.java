package com.econectar.api.user.service;

import com.econectar.api.auth.UserRegisterRequest;
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

    public User createUser(UserRegisterRequest user) {
        User newUser = modelMapper.map(user, User.class);
        newUser.setPassword(passwordEncoder.encode(user.getPassword()));
        newUser.setRole(Role.USER);
        return userRepository.save(newUser);
    }

    public User findUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    public UserDTO findUserByEmail(String email) {
        try {
            User user = userRepository.findByEmail(email);
            if (user == null) {
                throw new UsernameNotFoundException("User not found with email: " + email);
            }
            return modelMapper.map(user, UserDTO.class);
        }
        catch (Exception e) {
            throw new RuntimeException("Error retrieving user by email: " + email, e);
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

    // Additional methods for user management can be added here
}
