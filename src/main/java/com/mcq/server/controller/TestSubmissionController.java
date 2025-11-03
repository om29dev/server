package com.mcq.server.controller;

import com.mcq.server.dto.StudentResultDTO;
import com.mcq.server.dto.TeacherResultsDTO;
import com.mcq.server.model.Classroom;
import com.mcq.server.model.Test;
import com.mcq.server.model.TestSubmission;
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
import java.util.stream.Collectors;

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

    @GetMapping("/my")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public ResponseEntity<?> getMySubmission(@PathVariable String classroomCode,
                                             @PathVariable String testname,
                                             Authentication authentication) {

        String username = authentication.getName();

        Optional<Test> testOpt = testRepository.findByTestnameIgnoreCaseAndClassroomCode(testname, classroomCode);
        if (testOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Test not found.");
        }
        Test test = testOpt.get();

        Optional<TestSubmission> submissionOpt = testSubmissionRepository
                .findByTestnameAndClassroomCodeAndUserUsername(testname, classroomCode, username)
                .stream().findFirst();

        if (submissionOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("You have not made a submission for this test.");
        }
        TestSubmission submission = submissionOpt.get();

        StudentResultDTO resultDTO = new StudentResultDTO(submission, test);
        return ResponseEntity.ok(resultDTO);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_TEACHER', 'ROLE_ADMIN')")
    public ResponseEntity<?> getAllSubmissions(@PathVariable String classroomCode,
                                               @PathVariable String testname,
                                               Authentication authentication) {

        String username = authentication.getName();

        Optional<Test> testOpt = testRepository.findByTestnameIgnoreCaseAndClassroomCode(testname, classroomCode);
        if (testOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Test not found.");
        }
        Test test = testOpt.get();

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
                    .body("Only the teacher or an admin can view all submissions.");
        }

        List<TestSubmission> submissions = testSubmissionRepository
                .findByTestnameAndClassroomCode(testname, classroomCode);

        List<StudentResultDTO> submissionDTOs = submissions.stream()
                .map(sub -> new StudentResultDTO(sub, test))
                .collect(Collectors.toList());

        TeacherResultsDTO results = new TeacherResultsDTO(
                test.getCorrectAnswers(),
                test.getQuestionCount(),
                submissionDTOs
        );

        return ResponseEntity.ok(results);
    }

    @PostMapping("/update")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public ResponseEntity<?> updateAnswers(@PathVariable String classroomCode,
                                           @PathVariable String testname,
                                           @RequestBody List<String> userAnswers,
                                           Authentication authentication) {
        String username = authentication.getName();

        Optional<Test> testOpt = testRepository.findByTestnameIgnoreCaseAndClassroomCode(testname, classroomCode);
        if (testOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Test not found.");
        }
        Test test = testOpt.get();

        if (!"ACTIVE".equals(test.getStatus())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Can only update answers for an active test.");
        }

        if (userAnswers.size() != test.getQuestionCount()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Update failed: Expected " + test.getQuestionCount() + " answers, but received " + userAnswers.size() + ".");
        }

        Optional<TestSubmission> submissionOpt =
                testSubmissionRepository.findByTestnameAndClassroomCodeAndUserUsername(testname, classroomCode, username)
                        .stream().findFirst();

        if (submissionOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Submission not found.");
        }

        TestSubmission submission = submissionOpt.get();
        submission.setUserAnswers(userAnswers);
        testSubmissionRepository.save(submission);

        return ResponseEntity.ok(Map.of("message", "Answers saved."));
    }


    @PostMapping("/submit")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public ResponseEntity<?> submitAnswers(@PathVariable String classroomCode,
                                           @PathVariable String testname,
                                           @RequestBody List<String> userAnswers,
                                           Authentication authentication) {

        String username = authentication.getName();

        Optional<Test> testOpt = testRepository.findByTestnameIgnoreCaseAndClassroomCode(testname, classroomCode);
        if (testOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Test not found.");
        }

        Test test = testOpt.get();

        if (!"ENDED".equals(test.getStatus())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Test is not yet over.");
        }

        if (userAnswers.size() != test.getQuestionCount()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Submission failed: Expected " + test.getQuestionCount() + " answers, but received " + userAnswers.size() + ".");
        }

        Optional<TestSubmission> submissionOpt =
                testSubmissionRepository.findByTestnameAndClassroomCodeAndUserUsername(testname, classroomCode, username)
                        .stream().findFirst();

        if (submissionOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Submission not found. Test may not have been started correctly by the teacher.");
        }

        TestSubmission submission = submissionOpt.get();

        submission.setUserAnswers(userAnswers);
        testSubmissionRepository.save(submission);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Answers submitted successfully.");

        return ResponseEntity.ok(response);
    }
}