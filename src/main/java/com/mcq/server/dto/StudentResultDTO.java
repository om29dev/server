package com.mcq.server.dto;

import com.mcq.server.model.Test;
import com.mcq.server.model.TestSubmission;

import java.util.List;

public class StudentResultDTO {
    private UserDTO user;
    private List<String> userAnswers;
    private List<String> correctAnswers;
    private int score;
    private int totalQuestions;
    private String pdfPath; // To show the PDF next to the results

    public StudentResultDTO(TestSubmission submission, Test test) {
        this.user = new UserDTO(submission.getUser());
        this.userAnswers = submission.getUserAnswers();
        this.correctAnswers = test.getCorrectAnswers();
        this.totalQuestions = test.getQuestionCount();
        this.pdfPath = test.getQuestionsPdfPath(); // Include the PDF path

        // Calculate score
        this.score = 0;
        for (int i = 0; i < this.totalQuestions; i++) {
            if (i < userAnswers.size() && userAnswers.get(i) != null &&
                    userAnswers.get(i).equals(correctAnswers.get(i))) {
                this.score++;
            }
        }
    }

    // Getters
    public UserDTO getUser() { return user; }
    public List<String> getUserAnswers() { return userAnswers; }
    public List<String> getCorrectAnswers() { return correctAnswers; }
    public int getScore() { return score; }
    public int getTotalQuestions() { return totalQuestions; }
    public String getPdfPath() { return pdfPath; }
}