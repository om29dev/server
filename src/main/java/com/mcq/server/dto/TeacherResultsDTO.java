package com.mcq.server.dto;

import java.util.List;

public class TeacherResultsDTO {
    private List<String> correctAnswers;
    private int totalQuestions;
    private List<StudentResultDTO> submissions;

    public TeacherResultsDTO(List<String> correctAnswers, int totalQuestions, List<StudentResultDTO> submissions) {
        this.correctAnswers = correctAnswers;
        this.totalQuestions = totalQuestions;
        this.submissions = submissions;
    }

    public List<String> getCorrectAnswers() { return correctAnswers; }
    public int getTotalQuestions() { return totalQuestions; }
    public List<StudentResultDTO> getSubmissions() { return submissions; }
}