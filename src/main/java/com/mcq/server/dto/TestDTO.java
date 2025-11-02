package com.mcq.server.dto;

import com.mcq.server.model.Test;
import java.util.List;

public class TestDTO {

    private Long id;
    private String testname;
    private List<String> correctAnswers;
    private String status;
    private ClassroomDTO classroom; // Uses ClassroomDTO to avoid nested proxy issues

    public TestDTO(Test test) {
        this.id = test.getId();
        this.testname = test.getTestname();
        this.correctAnswers = test.getCorrectAnswers();
        this.status = test.getStatus();

        // This is the key fix: We convert the Classroom entity to a ClassroomDTO.
        if (test.getClassroom() != null) {
            this.classroom = new ClassroomDTO(test.getClassroom());
        }
    }

    // --- Getters ---

    public Long getId() {
        return id;
    }

    public String getTestname() {
        return testname;
    }

    public List<String> getCorrectAnswers() {
        return correctAnswers;
    }

    public String getStatus() {
        return status;
    }

    public ClassroomDTO getClassroom() {
        return classroom;
    }
}