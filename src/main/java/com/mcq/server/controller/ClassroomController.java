package com.mcq.server.controller;

import com.mcq.server.model.Classroom;
import com.mcq.server.repository.ClassroomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/classrooms") // Base URL for all endpoints in this controller
@CrossOrigin(origins = "*") // Adjust origins as needed for security
public class ClassroomController {

    @Autowired
    private ClassroomRepository classroomRepository;

    // GET /api/classrooms - Retrieve all classrooms
    @GetMapping
    public ResponseEntity<List<Classroom>> getAllClassrooms() {
        List<Classroom> classrooms = classroomRepository.findAll();
        return new ResponseEntity<>(classrooms, HttpStatus.OK);
    }

    // GET /api/classrooms/{code} - Retrieve a classroom by its unique code (case-sensitive)
    @GetMapping("/{code}")
    public ResponseEntity<Classroom> getClassroomByCode(@PathVariable String code) {
        Optional<Classroom> classroomOptional = classroomRepository.findById(code);
        if (classroomOptional.isPresent()) {
            return new ResponseEntity<>(classroomOptional.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // GET /api/classrooms/name?q={name} - Retrieve a classroom by its unique name (case-insensitive)
    @GetMapping("/name")
    public ResponseEntity<Classroom> getClassroomByName(@RequestParam String q) { // @RequestParam for query param 'q'
        Optional<Classroom> classroomOptional = classroomRepository.findByClassroomnameIgnoreCase(q); // Use the new method
        if (classroomOptional.isPresent()) {
            return new ResponseEntity<>(classroomOptional.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // POST /api/classrooms - Create a new classroom
    @PostMapping
    public ResponseEntity<Classroom> createClassroom(@RequestBody Classroom classroom) {
        try {
            // Ensure classroomstudents list is initialized if null (optional, depending on your logic)
            if (classroom.getClassroomstudents() == null) {
                classroom.setClassroomstudents(java.util.Collections.emptyList()); // Or handle as needed
            }
            Classroom savedClassroom = classroomRepository.save(classroom);
            return new ResponseEntity<>(savedClassroom, HttpStatus.CREATED);
        } catch (Exception e) {
            // Log the error (consider using a logger)
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // PUT /api/classrooms/{code} - Update an existing classroom using its unique code from the path (case-sensitive)
    @PutMapping("/{code}")
    public ResponseEntity<Classroom> updateClassroomByCode(@PathVariable String code, @RequestBody Classroom classroomDetails) {
        Optional<Classroom> classroomOptional = classroomRepository.findById(code);

        if (classroomOptional.isPresent()) {
            Classroom classroom = classroomOptional.get();
            classroom.setClassroomname(classroomDetails.getClassroomname());
            // Potentially prevent changing the code itself if it's meant to be immutable for this endpoint
            // classroom.setCode(classroomDetails.getCode()); // Uncomment if code updates are allowed here
            classroom.setClassroomteacher(classroomDetails.getClassroomteacher());
            classroom.setClassroomstudents(classroomDetails.getClassroomstudents());

            Classroom updatedClassroom = classroomRepository.save(classroom);
            return new ResponseEntity<>(updatedClassroom, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // DELETE /api/classrooms/{code} - Delete a classroom using its unique code from the path (case-sensitive)
    @DeleteMapping("/{code}")
    public ResponseEntity<HttpStatus> deleteClassroomByCode(@PathVariable String code) {
        Optional<Classroom> classroomOptional = classroomRepository.findById(code);

        if (classroomOptional.isPresent()) {
            Classroom classroom = classroomOptional.get();
            classroomRepository.delete(classroom); // Delete the found entity object
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}