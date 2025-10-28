package com.mcq.server.controller;

import com.mcq.server.dto.LoginRequest;
import com.mcq.server.model.User;
import com.mcq.server.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping(path = "/api/auth")
public class LoginController {

    @Autowired
    private UserRepository userRepository;

    @PostMapping(path = "/login")
    public UUID getUUIDbyLogin(@RequestBody LoginRequest loginRequest) {
        Optional<User> userOptional = userRepository.findByUsername(loginRequest.getUsername());
        try {
            if (userOptional.isPresent()) {
                if (userOptional.get().getPassword().equals(loginRequest.getPassword())) {
                    return userOptional.get().getUuid();
                }
                else  {
                    throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE);
                }
            } else  {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND);
            }
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), ex);
        }
    }
}
