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
            Optional<Classroom> classroomOpt = classroomRepository.findById(classroomCode);
            if (classroomOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Classroom not found.");
            }

            if (testRepository.existsByTestnameIgnoreCaseAndClassroomCode(testname, classroomCode)) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("A test with this name already exists in this classroom.");
            }

            String pdfPath = savePDFService.savePDF(pdfFile);

            Test test = new Test();
            test.setTestname(testname);
            test.setQuestionsPdfPath(pdfPath);
            test.setCorrectAnswers(correctAnswers);
            test.setClassroom(classroomOpt.get());

            Test savedTest = testRepository.save(test);
            return new ResponseEntity<>(savedTest, HttpStatus.CREATED);

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
        return ResponseEntity.ok(tests);
    }
    @GetMapping("/{testname}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TEACHER', 'ROLE_STUDENT')")
    public ResponseEntity<?> getTestByName(@PathVariable String classroomCode,
                                           @PathVariable String testname,
                                           Authentication authentication) {

        // ✅ Check if classroom exists
        Optional<Classroom> classroomOpt = classroomRepository.findById(classroomCode);
        if (classroomOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Classroom not found.");
        }

        Classroom classroom = classroomOpt.get();
        String username = authentication.getName();

        // ✅ Role verification
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isTeacher = classroom.getClassroomteacher().getUsername().equals(username);
        boolean isStudent = classroom.getClassroomstudents().contains(username);

        if (!isAdmin && !isTeacher && !isStudent) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("You are not authorized to view this test.");
        }

        // ✅ Find test by name and classroom code
        Optional<Test> testOpt = testRepository.findByTestnameIgnoreCaseAndClassroomCode(testname, classroomCode);
        if (testOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Test not found with that name in this classroom.");
        }

        return ResponseEntity.ok(testOpt.get());
    }

    // 🟥 Delete Test by Name (for teacher of that classroom or admin)
    @DeleteMapping("/{testname}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or @classroomRepository.findById(#classroomCode).get().getClassroomteacher().getUsername() == authentication.name")
    public ResponseEntity<?> deleteTestByName(@PathVariable String classroomCode,
                                              @PathVariable String testname,
                                              Authentication authentication) {

        String username = authentication.getName();

        // ✅ Find test
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

        testRepository.delete(test);
        return ResponseEntity.noContent().build();
    }


    // 🟩 START TEST
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
        return ResponseEntity.ok("Test started successfully.");
    }

    // 🟥 END TEST
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
}
