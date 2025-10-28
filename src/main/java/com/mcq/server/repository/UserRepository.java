package com.mcq.server.repository;

import org.springframework.data.repository.CrudRepository;

import com.mcq.server.model.User;

import java.util.Optional;
import java.util.UUID;

// This will be AUTO IMPLEMENTED by Spring into a Bean called userRepository
// CRUD refers Create, Read, Update, Delete

public interface UserRepository extends CrudRepository<User, UUID> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByResetPasswordToken(String token);
}