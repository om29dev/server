package com.mcq.server.model;

// Define the possible roles a user can have
public enum UserRole {
    ROLE_ADMIN,    // Full access and system management capabilities
    ROLE_TEACHER,  // Can create, edit, and manage MCQs and possibly view student results
    ROLE_STUDENT   // Can only attempt quizzes and view their own results
}