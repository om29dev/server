package com.mcq.server.controller;

import com.mcq.server.dto.UserDTO;
import com.mcq.server.model.User;
import com.mcq.server.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping(path = "/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;


    @PostMapping
    public ResponseEntity<UserDTO> createNewUser(@RequestBody User newUser) {
        try {
            User savedUser = userRepository.save(newUser);
            return new ResponseEntity<>(new UserDTO(savedUser), HttpStatus.CREATED); // 201
        } catch (Exception e) {
            // Handles DataIntegrityViolationException for duplicate email/username or null values
            System.err.println("Error creating new user: " + e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST); // 400
        }
    }

    @GetMapping
    public @ResponseBody Iterable<UserDTO> getAllUsers() {
        Iterable<User> users = userRepository.findAll();
        return StreamSupport.stream(users.spliterator(), false)
                .map(UserDTO::new)
                .collect(Collectors.toList());
    }

    @GetMapping(path = "/{uuid}")
    public ResponseEntity<User> getUserById(@PathVariable UUID uuid) {
        Optional<User> userData = userRepository.findById(uuid);

        // 200
        // 404
        return userData.map(user -> new ResponseEntity<>(user, HttpStatus.OK)).orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PutMapping(path = "/{uuid}")
    public ResponseEntity<User> updateUser(@PathVariable UUID uuid, @RequestBody User userDetails) {
        Optional<User> userData = userRepository.findById(uuid);

        if (userData.isPresent()) {
            User user = userData.get();

            // Update all non-UUID fields, including the new role
            user.setFirstname(userDetails.getFirstname());
            user.setLastname(userDetails.getLastname());
            user.setEmail(userDetails.getEmail());
            user.setUsername(userDetails.getUsername());
            user.setPassword(userDetails.getPassword());
            user.setRole(userDetails.getRole());

            return new ResponseEntity<>(userRepository.save(user), HttpStatus.OK); // 200
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // 404
        }
    }

    @DeleteMapping(path = "/{uuid}")
    public ResponseEntity<HttpStatus> deleteUser(@PathVariable UUID uuid) {
        try {
            userRepository.deleteById(uuid);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT); // 204 (Success, but no content to return)
        } catch (Exception e) {
           return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR); // 500
        }
    }
}