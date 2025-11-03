package com.mcq.server.repository;

import com.mcq.server.model.Classroom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClassroomRepository extends JpaRepository<Classroom, String> {

    Optional<Classroom> findByClassroomnameIgnoreCase(String classroomname);

    List<Classroom> findAllByClassroomstudentsContaining(String studentUsername);
}