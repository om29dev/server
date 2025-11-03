package com.mcq.server.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "test_submissions")
public class TestSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "classroom_code", nullable = false)
    private String classroomCode;

    @Column(name = "testname", nullable = false)
    private String testname;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_username", nullable = false)
    private User user;

    @ElementCollection
    @CollectionTable(
            name = "test_submission_answers",
            joinColumns = @JoinColumn(name = "submission_id")
    )
    @Column(name = "answer", length = 500)
    private List<String> userAnswers = new ArrayList<>();

    public TestSubmission() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTestname() {
        return testname;
    }

    public void setTestname(String testname) {
        this.testname = testname;
    }

    public String getClassroomCode() {
        return classroomCode;
    }

    public void setClassroomCode(String classroomCode) {
        this.classroomCode = classroomCode;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<String> getUserAnswers() {
        return userAnswers;
    }

    public void setUserAnswers(List<String> userAnswers) {
        this.userAnswers = userAnswers;
    }
}
