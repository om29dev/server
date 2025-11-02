package com.mcq.server.controller;

import com.mcq.server.model.Classroom;
import com.mcq.server.model.Test;
import com.mcq.server.model.TestSubmission;
import com.mcq.server.model.User;
import com.mcq.server.repository.ClassroomRepository;
import com.mcq.server.repository.TestRepository;
import com.mcq.server.repository.TestSubmissionRepository;
import com.mcq.server.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/classrooms/{classroomCode}/tests/{testname}/submissions")
public class TestSubmissionController {

    @Autowired
    private TestRepository testRepository;

    @Autowired
    private ClassroomRepository classroomRepository;

    @Autowired
    private TestSubmissionRepository testSubmissionRepository;

    @Autowired
    private UserRepository userRepository;

    // 🟩 2. STUDENT views own submission
    @GetMapping("/my")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public ResponseEntity<?> getMySubmission(@PathVariable String classroomCode,
                                             @PathVariable String testname,
                                             Authentication authentication) {

        String username = authentication.getName();

        Optional<Classroom> classroomOpt = classroomRepository.findById(classroomCode);
        if (classroomOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Classroom not found.");
        }

        if (!classroomOpt.get().getClassroomstudents().contains(username)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("You are not a member of this classroom.");
        }

        List<TestSubmission> submissions = testSubmissionRepository
                .findByTestnameAndClassroomCodeAndUserUsername(testname, classroomCode, username);

        if (submissions.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("You have not submitted this test yet.");
        }

        return ResponseEntity.ok(submissions.get(0));
    }

    // 🟩 3. TEACHER or ADMIN views all submissions for a test
    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_TEACHER', 'ROLE_ADMIN')")
    public ResponseEntity<?> getAllSubmissions(@PathVariable String classroomCode,
                                               @PathVariable String testname,
                                               Authentication authentication) {

        String username = authentication.getName();

        Optional<Classroom> classroomOpt = classroomRepository.findById(classroomCode);
        if (classroomOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Classroom not found.");
        }

        Classroom classroom = classroomOpt.get();

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isTeacher = classroom.getClassroomteacher().getUsername().equals(username);

        if (!isAdmin && !isTeacher) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Only teachers or admins can view all submissions.");
        }

        List<TestSubmission> submissions = testSubmissionRepository
                .findByTestnameAndClassroomCode(testname, classroomCode);

        return ResponseEntity.ok(submissions);
    }

    @PostMapping("/submit")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public ResponseEntity<?> submitAnswers(@PathVariable String classroomCode,
                                           @PathVariable String testname,
                                           @RequestBody List<String> userAnswers,
                                           Authentication authentication) {

        String username = authentication.getName();

        // Find the TestSubmission record for this student
        Optional<TestSubmission> submissionOpt =
                testSubmissionRepository.findByTestnameAndClassroomCodeAndUserUsername(testname, classroomCode, username)
                        .stream().findFirst();

        if (submissionOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Submission not found. Make sure the test has been started and ended by the teacher.");
        }

        TestSubmission submission = submissionOpt.get();

        // Store student answers
        submission.setUserAnswers(userAnswers);
        testSubmissionRepository.save(submission);

        // Fetch correct answers from Test
        Optional<Test> testOpt = testRepository.findByTestnameIgnoreCaseAndClassroomCode(testname, classroomCode);
        if (testOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Test not found.");
        }

        List<String> correctAnswers = testOpt.get().getCorrectAnswers();

        // Prepare response
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Answers submitted successfully.");
        response.put("submittedAnswers", userAnswers);
        response.put("correctAnswers", correctAnswers);

        return ResponseEntity.ok(response);
    }



}
