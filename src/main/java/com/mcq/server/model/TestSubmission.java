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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_id", nullable = false)
    private Test test;

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

    public Test getTest() {
        return test;
    }

    public void setTest(Test test) {
        this.test = test;
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