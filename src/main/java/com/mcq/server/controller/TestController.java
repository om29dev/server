package com.mcq.server.controller;

import com.mcq.server.dto.TestDTO;
import com.mcq.server.model.Classroom;
import com.mcq.server.model.Test;
import com.mcq.server.model.TestSubmission;
import com.mcq.server.model.User;
import com.mcq.server.repository.ClassroomRepository;
import com.mcq.server.repository.TestRepository;
import com.mcq.server.repository.TestSubmissionRepository;
import com.mcq.server.repository.UserRepository;
import com.mcq.server.service.SavePDFService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/classrooms/{classroomCode}/tests")
public class TestController {

    @Autowired
    private TestRepository testRepository;

    @Autowired
    private ClassroomRepository classroomRepository;

    @Autowired
    private SavePDFService savePDFService;

    @Autowired
    private TestSubmissionRepository testSubmissionRepository;

    @Autowired
    private UserRepository userRepository;

    private static final Pattern ANSWER_PATTERN = Pattern.compile("^[A-D]$");

    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN') or @classroomRepository.findById(#classroomCode).get().getClassroomteacher().getUsername() == authentication.name")
    public ResponseEntity<?> createTest(@PathVariable String classroomCode,
                                        @RequestParam("testname") String testname,
                                        @RequestParam("pdfFile") MultipartFile pdfFile,
                                        @RequestParam("correctAnswers") List<String> correctAnswers) {
        try {
            Optional<Classroom> classroomOpt = classroomRepository.findById(classroomCode);
            if (classroomOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Classroom not found.");
            }

            if (testRepository.existsByTestnameIgnoreCaseAndClassroomCode(testname, classroomCode)) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("A test with this name already exists in this classroom.");
            }

            SavePDFService.PdfInfo pdfInfo;
            try {
                pdfInfo = savePDFService.savePDF(pdfFile);
            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to save PDF: " + e.getMessage());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
            }

            if (pdfInfo.pageCount() != correctAnswers.size()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Validation failed: The number of questions (" + pdfInfo.pageCount() +
                                ") does not match the number of answers provided (" + correctAnswers.size() + ").");
            }

            for (String answer : correctAnswers) {
                if (!ANSWER_PATTERN.matcher(answer).matches()) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body("Validation failed: Invalid answer '" + answer + "'. Answers must be A, B, C, or D.");
                }
            }

            Test test = new Test();
            test.setTestname(testname);
            test.setQuestionsPdfPath(pdfInfo.path());
            test.setQuestionCount(pdfInfo.pageCount());
            test.setCorrectAnswers(correctAnswers);
            test.setClassroom(classroomOpt.get());

            Test savedTest = testRepository.save(test);

            return new ResponseEntity<>(new TestDTO(savedTest), HttpStatus.CREATED);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating test: " + e.getMessage());
        }
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TEACHER', 'ROLE_STUDENT')")
    public ResponseEntity<?> getTestsByClassroom(@PathVariable String classroomCode, Authentication authentication) {
        Optional<Classroom> classroomOpt = classroomRepository.findById(classroomCode);
        if (classroomOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Classroom not found.");
        }

        String username = authentication.getName();
        Classroom classroom = classroomOpt.get();

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isTeacher = classroom.getClassroomteacher().getUsername().equals(username);
        boolean isStudent = classroom.getClassroomstudents().contains(username);

        if (!isAdmin && !isTeacher && !isStudent) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("You are not authorized to view tests for this classroom.");
        }

        List<Test> tests = testRepository.findByClassroomCode(classroomCode);

        List<TestDTO> testDTOs = tests.stream()
                .map(TestDTO::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(testDTOs);
    }
    @GetMapping("/{testname}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TEACHER', 'ROLE_STUDENT')")
    public ResponseEntity<?> getTestByName(@PathVariable String classroomCode,
                                           @PathVariable String testname,
                                           Authentication authentication) {

        Optional<Classroom> classroomOpt = classroomRepository.findById(classroomCode);
        if (classroomOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Classroom not found.");
        }

        Classroom classroom = classroomOpt.get();
        String username = authentication.getName();

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isTeacher = classroom.getClassroomteacher().getUsername().equals(username);
        boolean isStudent = classroom.getClassroomstudents().contains(username);

        if (!isAdmin && !isTeacher && !isStudent) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("You are not authorized to view this test.");
        }

        Optional<Test> testOpt = testRepository.findByTestnameIgnoreCaseAndClassroomCode(testname, classroomCode);
        if (testOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Test not found with that name in this classroom.");
        }

        TestDTO testDTO = new TestDTO(testOpt.get());

        return ResponseEntity.ok(testDTO);
    }

    @DeleteMapping("/{testname}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or @classroomRepository.findById(#classroomCode).get().getClassroomteacher().getUsername() == authentication.name")
    public ResponseEntity<?> deleteTestByName(@PathVariable String classroomCode,
                                              @PathVariable String testname,
                                              Authentication authentication) {

        String username = authentication.getName();

        Optional<Test> testOpt = testRepository.findByTestnameIgnoreCaseAndClassroomCode(testname, classroomCode);
        if (testOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Test not found in this classroom.");
        }

        Test test = testOpt.get();
        Classroom classroom = test.getClassroom();

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isTeacher = classroom.getClassroomteacher().getUsername().equals(username);

        if (!isAdmin && !isTeacher) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("You are not authorized to delete this test.");
        }

        List<TestSubmission> submissions = testSubmissionRepository
                .findByTestnameAndClassroomCode(test.getTestname(), test.getClassroom().getCode());

        if (!submissions.isEmpty()) {
            testSubmissionRepository.deleteAll(submissions);
        }

        testRepository.delete(test);
        return ResponseEntity.noContent().build();
    }


    @PostMapping("/{testname}/start")
    @PreAuthorize("hasRole('ROLE_ADMIN') or @classroomRepository.findById(#classroomCode).get().getClassroomteacher().getUsername() == authentication.name")
    public ResponseEntity<?> startTest(@PathVariable String classroomCode,
                                       @PathVariable String testname) {

        Optional<Test> testOpt = testRepository.findByTestnameIgnoreCaseAndClassroomCode(testname, classroomCode);
        if (testOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Test not found.");
        }

        Test test = testOpt.get();
        if ("ACTIVE".equals(test.getStatus())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Test is already active.");
        }

        test.setStatus("ACTIVE");
        testRepository.save(test);

        Classroom classroom = test.getClassroom();
        List<String> studentUsernames = classroom.getClassroomstudents();

        for (String username : studentUsernames) {
            if (!testSubmissionRepository.existsByTestnameAndClassroomCodeAndUserUsername(testname, classroomCode, username)) {
                Optional<User> userOpt = userRepository.findByUsername(username);
                if (userOpt.isPresent()) {
                    TestSubmission submission = new TestSubmission();
                    submission.setClassroomCode(classroomCode);
                    submission.setTestname(testname);
                    submission.setUser(userOpt.get());
                    submission.setUserAnswers(new ArrayList<>());
                    testSubmissionRepository.save(submission);
                }
            }
        }

        return ResponseEntity.ok("Test started successfully. Submissions created for all students.");
    }

    @PostMapping("/{testname}/end")
    @PreAuthorize("hasRole('ROLE_ADMIN') or @classroomRepository.findById(#classroomCode).get().getClassroomteacher().getUsername() == authentication.name")
    public ResponseEntity<?> endTest(@PathVariable String classroomCode,
                                     @PathVariable String testname) {

        Optional<Test> testOpt = testRepository.findByTestnameIgnoreCaseAndClassroomCode(testname, classroomCode);
        if (testOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Test not found.");
        }

        Test test = testOpt.get();
        if (!"ACTIVE".equals(test.getStatus())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Cannot end a test that is not active.");
        }

        test.setStatus("ENDED");
        testRepository.save(test);
        return ResponseEntity.ok("Test ended successfully. Students can now submit answers.");
    }

    @GetMapping("/{testname}/pdf")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public ResponseEntity<?> getTestPDF(@PathVariable String classroomCode,
                                        @PathVariable String testname,
                                        Authentication authentication) {

        String username = authentication.getName();

        Optional<Test> testOpt = testRepository.findByTestnameIgnoreCaseAndClassroomCode(testname, classroomCode);
        if (testOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Test not found.");
        }

        Test test = testOpt.get();
        Classroom classroom = test.getClassroom();

        if (!classroom.getClassroomstudents().contains(username)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("You are not enrolled in this classroom.");
        }

        if (!"ACTIVE".equals(test.getStatus()) && !"ENDED".equals(test.getStatus())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Test is not active or has not ended yet.");
        }

        String pdfPath = test.getQuestionsPdfPath();
        if (pdfPath == null || pdfPath.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("PDF file not available for this test.");
        }

        try {
            File pdfFile = new File(pdfPath);
            if (!pdfFile.exists()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("PDF file not found on server.");
            }

            byte[] pdfBytes = Files.readAllBytes(pdfFile.toPath());

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + pdfFile.getName() + "\"")
                    .body(pdfBytes);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error reading PDF file: " + e.getMessage());
        }
    }

}