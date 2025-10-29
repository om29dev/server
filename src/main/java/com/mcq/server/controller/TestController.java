package com.mcq.server.controller;

import com.mcq.server.model.Classroom;
import com.mcq.server.model.Test;
import com.mcq.server.repository.ClassroomRepository;
import com.mcq.server.repository.TestRepository;
import com.mcq.server.service.SavePDFService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/tests")
public class TestController {

    @Autowired
    private TestRepository testRepository;

    @Autowired
    private ClassroomRepository classroomRepository;

    @Autowired
    private SavePDFService savePDFService;

    @PostMapping("/create")
    public ResponseEntity<Test> createTest(@RequestParam("testname") String testname,
                                           @RequestParam("pdfFile") MultipartFile pdfFile,
                                           @RequestParam("classroomCode") String classroomCode) {
        try {
            String pdfPath = savePDFService.savePDF(pdfFile);

            Optional<Classroom> classroom = classroomRepository.findById(classroomCode);
            if (classroom.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            Test test = new Test();
            test.setTestname(testname);
            test.setQuestionsPdfPath(pdfPath);
            test.setClassroom(classroom.orElse(null));

            Test savedTest = testRepository.save(test);
            return ResponseEntity.ok(savedTest);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/classroom/{classroomCode}")
    public ResponseEntity<List<Test>> getTestsByClassroom(@PathVariable String classroomCode) {
        List<Test> tests = testRepository.findByClassroomCode(classroomCode);
        return ResponseEntity.ok(tests);
    }
}