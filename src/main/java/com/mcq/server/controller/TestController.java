package com.mcq.server.controller;

import com.mcq.server.model.Classroom;
import com.mcq.server.model.Test;
import com.mcq.server.repository.ClassroomRepository;
import com.mcq.server.repository.TestRepository;
import com.mcq.server.service.SavePDFService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@RestController
// All routes in this controller are now prefixed with this
@RequestMapping("/api/classrooms/{classroomCode}/tests")
public class TestController {

    @Autowired
    private TestRepository testRepository;

    @Autowired
    private ClassroomRepository classroomRepository;

    @Autowired
    private SavePDFService savePDFService;

    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN') or @classroomRepository.findById(#classroomCode).get().getClassroomteacher().getUsername() == authentication.name")
    public ResponseEntity<?> createTest(@PathVariable String classroomCode,
                                        @RequestParam("testname") String testname,
                                        @RequestParam("pdfFile") MultipartFile pdfFile,
                                        @RequestParam("correctAnswers") List<String> correctAnswers) {
        try {
            // 1. Check if classroom exists
            Optional<Classroom> classroomOpt = classroomRepository.findById(classroomCode);
            if (classroomOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Classroom not found.");
            }

            // 2. NEW: Check if testname is unique *for this classroom*
            if (testRepository.existsByTestnameIgnoreCaseAndClassroomCode(testname, classroomCode)) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("A test with this name already exists in this classroom.");
            }

            // 3. Save PDF and create test
            String pdfPath = savePDFService.savePDF(pdfFile);

            Test test = new Test();
            test.setTestname(testname);
            test.setQuestionsPdfPath(pdfPath);
            test.setCorrectAnswers(correctAnswers);
            test.setClassroom(classroomOpt.get()); // Use the smart setter

            Test savedTest = testRepository.save(test);
            return new ResponseEntity<>(savedTest, HttpStatus.CREATED);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error creating test: " + e.getMessage());
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
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not authorized to view tests for this classroom.");
        }

        List<Test> tests = testRepository.findByClassroomCode(classroomCode);
        return ResponseEntity.ok(tests);
    }

    @GetMapping("/{testId}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TEACHER', 'ROLE_STUDENT')")
    public ResponseEntity<?> getTestById(@PathVariable String classroomCode,
                                         @PathVariable Long testId,
                                         Authentication authentication) {

        Optional<Test> testOpt = testRepository.findById(testId);
        if (testOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Test not found.");
        }

        Test test = testOpt.get();

        if (!test.getClassroom().getCode().equals(classroomCode)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Test not found in this classroom.");
        }

        String username = authentication.getName();
        Classroom classroom = test.getClassroom();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isTeacher = classroom.getClassroomteacher().getUsername().equals(username);
        boolean isStudent = classroom.getClassroomstudents().contains(username);

        if (!isAdmin && !isTeacher && !isStudent) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not authorized to view this test.");
        }

        return ResponseEntity.ok(test);
    }

    @DeleteMapping("/{testId}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or @testRepository.findById(#testId).get().getClassroom().getClassroomteacher().getUsername() == authentication.name")
    public ResponseEntity<?> deleteTest(@PathVariable String classroomCode, @PathVariable Long testId) {

        Optional<Test> testOpt = testRepository.findById(testId);
        if (testOpt.isEmpty() || !testOpt.get().getClassroom().getCode().equals(classroomCode)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Test not found in this classroom.");
        }

        testRepository.deleteById(testId);
        return ResponseEntity.noContent().build();
    }
}