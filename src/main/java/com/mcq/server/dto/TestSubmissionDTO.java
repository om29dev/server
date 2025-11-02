package com.mcq.server.dto;

import com.mcq.server.model.TestSubmission;
import java.util.List;

public class TestSubmissionDTO {

    private Long id;
    private String classroomCode;
    private String testname;
    private UserDTO user; // <-- This is the fix
    private List<String> userAnswers;

    public TestSubmissionDTO(TestSubmission submission) {
        this.id = submission.getId();
        this.classroomCode = submission.getClassroomCode();
        this.testname = submission.getTestname();
        this.userAnswers = submission.getUserAnswers();

        // Safely handle the lazy-loaded User by converting it to a DTO
        if (submission.getUser() != null) {
            this.user = new UserDTO(submission.getUser());
        }
    }

    // --- Getters ---

    public Long getId() {
        return id;
    }

    public String getClassroomCode() {
        return classroomCode;
    }

    public String getTestname() {
        return testname;
    }

    public UserDTO getUser() {
        return user;
    }

    public List<String> getUserAnswers() {
        return userAnswers;
    }
}