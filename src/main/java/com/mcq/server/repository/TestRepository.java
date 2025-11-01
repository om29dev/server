package com.mcq.server.repository;

import com.mcq.server.model.Test;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TestRepository extends JpaRepository<Test, Long> {
    List<Test> findByClassroomCode(String classroomCode);
    boolean existsByTestnameIgnoreCaseAndClassroomCode(String testname, String classroomCode);
}