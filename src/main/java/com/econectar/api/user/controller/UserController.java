package com.econectar.api.user.controller;

import com.econectar.api.auth.UserRegisterRequest;
import com.econectar.api.user.model.User;
import com.econectar.api.user.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.findUserById(id));
    }

    @PostMapping
    public ResponseEntity<HttpStatus> createUser(@RequestBody UserRegisterRequest user) {
        User createdUser = userService.createUser(user);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdUser.getId() != null ? HttpStatus.CREATED : HttpStatus.BAD_REQUEST);
    }
}
