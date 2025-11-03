package com.mcq.server.dto;

import com.mcq.server.model.Test;
import java.util.List;

public class TestDTO {

    private Long id;
    private String testname;
    private List<String> correctAnswers;
    private String status;
    private ClassroomDTO classroom;
    private int questionCount;

    public TestDTO(Test test) {
        this.id = test.getId();
        this.testname = test.getTestname();
        this.correctAnswers = test.getCorrectAnswers();
        this.status = test.getStatus();
        this.questionCount = test.getQuestionCount();

        if (test.getClassroom() != null) {
            this.classroom = new ClassroomDTO(test.getClassroom());
        }
    }

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

    public int getQuestionCount() {
        return questionCount;
    }


}