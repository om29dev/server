package com.mcq.server.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Test {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String testname;

    @Column(name = "questions_pdf_path", nullable = false)
    private String questionsPdfPath;

    @ElementCollection
    @CollectionTable(
            name = "test_correct_answers",
            joinColumns = @JoinColumn(name = "test_id")
    )
    @Column(name = "answer", length = 500)
    private List<String> correctAnswers = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classroom_code", nullable = false)
    private Classroom classroom;

    @OneToMany(mappedBy = "test", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TestSubmission> testSubmissions = new ArrayList<>();

    public Test() {}

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

    public String getQuestionsPdfPath() {
        return questionsPdfPath;
    }

    public void setQuestionsPdfPath(String questionsPdfPath) {
        this.questionsPdfPath = questionsPdfPath;
    }

    public List<String> getCorrectAnswers() {
        return correctAnswers;
    }

    public void setCorrectAnswers(List<String> correctAnswers) {
        this.correctAnswers = correctAnswers;
    }

    public Classroom getClassroom() {
        return classroom;
    }

    public void setClassroom(Classroom classroom) {
        this.classroom = classroom;
    }

    public List<TestSubmission> getTestSubmissions() {
        return testSubmissions;
    }

    public void setTestSubmissions(List<TestSubmission> testSubmissions) {
        this.testSubmissions = testSubmissions;
    }
}