package com.mcq.server.controller;

import com.mcq.server.model.User;
import com.mcq.server.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping(path="/users") // Base mapping for all user endpoints
public class UserController {

    // Inject the UserRepository to interact with the database
    @Autowired
    private UserRepository userRepository;

    // POST /users
    // Used for registering a new user
    @PostMapping
    public ResponseEntity<User> addNewUser (@RequestBody User user) {
        // NOTE: In a production application, the password should be encrypted
        // using a service layer before saving.
        User savedUser = userRepository.save(user);
        return new ResponseEntity<>(savedUser, HttpStatus.CREATED);
    }

    // GET /users
    // Returns all users from the database
    @GetMapping
    public @ResponseBody Iterable<User> getAllUsers() {
        return userRepository.findAll();
    }

    // GET /users/{id}
    // Returns a single user by their UUID
    @GetMapping(path="/{id}")
    public ResponseEntity<User> getUserById(@PathVariable UUID id) {
        // The User model uses UUID as the ID type.
        // The UserRepository should extend CrudRepository<User, UUID> to correctly use findById(UUID).
        Optional<User> userOptional = userRepository.findById(id);

        if (userOptional.isPresent()) {
            return ResponseEntity.ok(userOptional.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}