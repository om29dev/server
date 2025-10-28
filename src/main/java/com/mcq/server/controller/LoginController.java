package com.mcq.server.controller;

import com.mcq.server.model.User;
import com.mcq.server.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;
import java.util.UUID;

import com.mcq.server.repository.UserRepository;

@RestController
@RequestMapping(path = "/api/auth")
public class LoginController {

    @Autowired
    private UserRepository userRepository;

    @PostMapping(path = '/login')
    public ResponseEntity<UUID> getUUIDbyLogin() {
        Iterable<User> users = userRepository.findb();
    }
}
