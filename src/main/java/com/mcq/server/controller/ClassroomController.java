package com.mcq.server.controller;

import com.mcq.server.dto.ClassroomDTO;
import com.mcq.server.dto.TestDTO;
import com.mcq.server.model.Classroom;
import com.mcq.server.model.Test;
import com.mcq.server.model.User;
import com.mcq.server.repository.ClassroomRepository;
import com.mcq.server.repository.MyUserDetails;
import com.mcq.server.repository.TestRepository;
import com.mcq.server.service.UniqueCodeGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/classrooms")
@CrossOrigin(origins = "*")
public class ClassroomController {

    @Autowired
    private ClassroomRepository classroomRepository;

    @Autowired
    private UniqueCodeGenerator uniqueCodeGenerator;

    @Autowired
    private TestRepository testRepository;

    @GetMapping("/student/active-test")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public ResponseEntity<?> getActiveTestForStudent(Authentication authentication) {
        String username = authentication.getName();

        List<Classroom> classrooms = classroomRepository.findAllByClassroomstudentsContaining(username);

        for (Classroom classroom : classrooms) {
            List<Test> tests = testRepository.findByClassroomCode(classroom.getCode());
            for (Test test : tests) {
                if ("ACTIVE".equals(test.getStatus())) {
                    return ResponseEntity.ok(new TestDTO(test));
                }
            }
        }

        return ResponseEntity.noContent().build();
    }


    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TEACHER', 'ROLE_STUDENT')")
    public ResponseEntity<?> getClassrooms(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String filter,
            Authentication authentication) {

        if (name != null && !name.isEmpty()) {
            Optional<Classroom> classroomOptional = classroomRepository.findByClassroomnameIgnoreCase(name);
            return classroomOptional.map(classroom -> new ResponseEntity<>(new ClassroomDTO(classroom), HttpStatus.OK)).orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
        }

        if ("mine".equalsIgnoreCase(filter)) {
            String username = authentication.getName();
            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
            List<Classroom> classrooms;
            if (authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_TEACHER"))) {
                classrooms = classroomRepository.findAll().stream()
                        .filter(c -> c.getClassroomteacher().getUsername().equals(username))
                        .collect(Collectors.toList());
            } else {
                classrooms = classroomRepository.findAllByClassroomstudentsContaining(username);
            }
            List<ClassroomDTO> classroomDTOs = classrooms.stream().map(ClassroomDTO::new).collect(Collectors.toList());
            return new ResponseEntity<>(classroomDTOs, HttpStatus.OK);
        }

        List<Classroom> classrooms = classroomRepository.findAll();
        List<ClassroomDTO> classroomDTOs = classrooms.stream().map(ClassroomDTO::new).collect(Collectors.toList());
        return new ResponseEntity<>(classroomDTOs, HttpStatus.OK);

    }


    @GetMapping("/{code}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TEACHER', 'ROLE_STUDENT')")
    public ResponseEntity<ClassroomDTO> getClassroomByCode(@PathVariable String code) {
        Optional<Classroom> classroomOptional = classroomRepository.findById(code);
        return classroomOptional.map(classroom -> new ResponseEntity<>(new ClassroomDTO(classroom), HttpStatus.OK)).orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping("/{code}/join")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public ResponseEntity<String> joinClassroom(@PathVariable String code, Authentication authentication) {
        Optional<Classroom> classroomOptional = classroomRepository.findById(code);
        if (classroomOptional.isPresent()) {
            Classroom classroom = classroomOptional.get();
            String studentUsername = authentication.getName();
            if (!classroom.getClassroomstudents().contains(studentUsername)) {
                classroom.getClassroomstudents().add(studentUsername);
                classroomRepository.save(classroom);
                return new ResponseEntity<>("Successfully joined the classroom.", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("You are already enrolled in this classroom.", HttpStatus.BAD_REQUEST);
            }
        } else {
            return new ResponseEntity<>("Classroom not found.", HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{code}/leave")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public ResponseEntity<String> leaveClassroom(@PathVariable String code, Authentication authentication) {
        Optional<Classroom> classroomOptional = classroomRepository.findById(code);
        if (classroomOptional.isPresent()) {
            Classroom classroom = classroomOptional.get();
            String studentUsername = authentication.getName();
            if (classroom.getClassroomstudents().contains(studentUsername)) {
                classroom.getClassroomstudents().remove(studentUsername);
                classroomRepository.save(classroom);
                return new ResponseEntity<>("Successfully left the classroom.", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("You are not enrolled in this classroom.", HttpStatus.BAD_REQUEST);
            }
        } else {
            return new ResponseEntity<>("Classroom not found.", HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{code}/remove/{studentUsername}")
    @PreAuthorize("hasRole('ROLE_TEACHER') and @classroomRepository.findById(#code).get().getClassroomteacher().getUsername() == authentication.name")
    public ResponseEntity<String> removeStudentFromClassroom(@PathVariable String code, @PathVariable String studentUsername) {
        Optional<Classroom> classroomOptional = classroomRepository.findById(code);
        if (classroomOptional.isPresent()) {
            Classroom classroom = classroomOptional.get();
            if (classroom.getClassroomstudents().contains(studentUsername)) {
                classroom.getClassroomstudents().remove(studentUsername);
                classroomRepository.save(classroom);
                return new ResponseEntity<>("Successfully removed the student.", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Student is not enrolled in this classroom.", HttpStatus.BAD_REQUEST);
            }
        } else {
            return new ResponseEntity<>("Classroom not found.", HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TEACHER')")
    public ResponseEntity<?> createClassroom(@RequestBody Map<String, String> request, Authentication authentication) {
        try {
            String classroomname = request.get("classroomname");
            if (classroomname == null || classroomname.trim().isEmpty()) {
                return new ResponseEntity<>("Classroom name cannot be empty.", HttpStatus.BAD_REQUEST);
            }

            if (classroomRepository.findByClassroomnameIgnoreCase(classroomname).isPresent()) {
                return new ResponseEntity<>("Classroom with this name already exists.", HttpStatus.CONFLICT);
            }

            Classroom classroom = new Classroom();
            classroom.setClassroomname(classroomname);

            String code = uniqueCodeGenerator.generateUniqueCode();
            classroom.setCode(code);

            MyUserDetails userDetails = (MyUserDetails) authentication.getPrincipal();
            User currentUser = userDetails.getUser();
            classroom.setClassroomteacher(currentUser);

            classroom.setClassroomstudents(Collections.emptyList());

            Classroom savedClassroom = classroomRepository.save(classroom);

            return new ResponseEntity<>(new ClassroomDTO(savedClassroom), HttpStatus.CREATED);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @PutMapping("/{code}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or @classroomRepository.findById(#code).get().getClassroomteacher().getUsername() == authentication.name")
    public ResponseEntity<ClassroomDTO> updateClassroomByCode(@PathVariable String code, @RequestBody Classroom classroomDetails) {
        Optional<Classroom> classroomOptional = classroomRepository.findById(code);

        if (classroomOptional.isPresent()) {
            Classroom classroom = classroomOptional.get();
            classroom.setClassroomname(classroomDetails.getClassroomname());
            classroom.setClassroomteacher(classroomDetails.getClassroomteacher());
            classroom.setClassroomstudents(classroomDetails.getClassroomstudents());
            Classroom updatedClassroom = classroomRepository.save(classroom);
            return new ResponseEntity<>(new ClassroomDTO(updatedClassroom), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{code}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or @classroomRepository.findById(#code).get().getClassroomteacher().getUsername() == authentication.name")
    public ResponseEntity<HttpStatus> deleteClassroomByCode(@PathVariable String code) {
        Optional<Classroom> classroomOptional = classroomRepository.findById(code);

        if (classroomOptional.isPresent()) {
            classroomRepository.delete(classroomOptional.get());
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}