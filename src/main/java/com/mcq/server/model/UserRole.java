package com.mcq.server.model;

// Define the possible roles a user can have
public enum UserRole {
    ADMIN,    // Full access and system management capabilities
    TEACHER,  // Can create, edit, and manage MCQs and possibly view student results
    STUDENT   // Can only attempt quizzes and view their own results
}