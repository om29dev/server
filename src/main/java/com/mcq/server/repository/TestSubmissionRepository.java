package com.mcq.server.repository;

import com.mcq.server.model.TestSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestSubmissionRepository extends JpaRepository<TestSubmission, Long> {

    List<TestSubmission> findByTestnameAndClassroomCode(String testname, String classroomCode);

    List<TestSubmission> findByTestnameAndClassroomCodeAndUserUsername(String testname, String classroomCode, String username);

    boolean existsByTestnameAndClassroomCodeAndUserUsername(String testname, String classroomCode, String username);
}
