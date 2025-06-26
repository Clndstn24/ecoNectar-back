package com.econectar.api.user.controller;

import com.econectar.api.auth.UserRegisterRequest;
import com.econectar.api.user.dto.UserDTO;
import com.econectar.api.user.model.User;
import com.econectar.api.user.service.UserService;
import org.modelmapper.internal.bytebuddy.build.HashCodeAndEqualsPlugin;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{email}")
    public ResponseEntity<UserDTO> getUser(@PathVariable String email) {
        return ResponseEntity.ok(userService.findUserByEmail(email));
    }
}