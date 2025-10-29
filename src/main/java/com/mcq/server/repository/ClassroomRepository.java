package com.mcq.server.repository;

import com.mcq.server.model.Classroom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClassroomRepository extends JpaRepository<Classroom, String> {

    // Find a classroom by its unique name (case-insensitive)
    Optional<Classroom> findByClassroomnameIgnoreCase(String classroomname);

    // Find all classrooms a student is enrolled in
    List<Classroom> findAllByClassroomstudentsContaining(String studentUsername);
}