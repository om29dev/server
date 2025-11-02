package com.mcq.server.repository;

import com.mcq.server.model.Test;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TestRepository extends JpaRepository<Test, Long> {

    boolean existsByTestnameIgnoreCaseAndClassroomCode(String testname, String classroomCode);

    List<Test> findByClassroomCode(String classroomCode);

    Optional<Test> findByTestnameIgnoreCaseAndClassroomCode(String testname, String classroomCode);
}
