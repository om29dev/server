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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}