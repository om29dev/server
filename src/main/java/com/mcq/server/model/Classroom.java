package com.mcq.server.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "classrooms")
public class Classroom {

    @Id
    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false, unique = true)
    private String classroomname;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_username", nullable = false)
    private User classroomteacher;

    @ElementCollection
    @CollectionTable(
            name = "classroom_students",
            joinColumns = @JoinColumn(name = "classroom_code")
    )
    @Column(name = "student_username")
    private List<String> classroomstudents = new ArrayList<>();

    @OneToMany(mappedBy = "classroom", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Test> tests = new ArrayList<>();

    public Classroom() {}

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getClassroomname() {
        return classroomname;
    }

    public void setClassroomname(String classroomname) {
        this.classroomname = classroomname;
    }

    public User getClassroomteacher() {
        return classroomteacher;
    }

    public void setClassroomteacher(User classroomteacher) {
        this.classroomteacher = classroomteacher;
    }

    public List<String> getClassroomstudents() {
        return classroomstudents;
    }

    public void setClassroomstudents(List<String> classroomstudents) {
        this.classroomstudents = classroomstudents;
    }

    public List<Test> getTests() {
        return tests;
    }

    public void setTests(List<Test> tests) {
        this.tests = tests;
    }
}