package com.mcq.server.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "tests",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"classroom_code", "testname"})
        }
)
public class Test {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String testname;

    @Column(name = "questions_pdf_path", nullable = false)
    private String questionsPdfPath;

    // --- NEW FIELD ---
    @Column(name = "question_count", nullable = false)
    private int questionCount;

    @ElementCollection
    @CollectionTable(
            name = "test_correct_answers",
            joinColumns = @JoinColumn(name = "test_id")
    )
    @Column(name = "answers")
    private List<String> correctAnswers = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classroom_code", nullable = false)
    private Classroom classroom;

    // --- FIELD REMOVED ---
    // The @OneToMany testSubmissions list was removed.
    // It was not modeled correctly (using 'testname' as a JoinColumn)
    // and was likely conflicting with the manual deletion logic in TestController.
    // Removing it fixes the 500 Internal Server Error on test deletion.

    @Column(nullable = false)
    private String status = "NOT_STARTED";

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

    // --- NEW GETTER/SETTER ---
    public int getQuestionCount() {
        return questionCount;
    }

    public void setQuestionCount(int questionCount) {
        this.questionCount = questionCount;
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

    // --- GETTER/SETTER REMOVED ---
    // public List<TestSubmission> getTestSubmissions() ...
    // public void setTestSubmissions(List<TestSubmission> testSubmissions) ...

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}