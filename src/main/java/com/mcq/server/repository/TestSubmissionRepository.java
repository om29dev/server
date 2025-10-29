package com.mcq.server.repository;

import com.mcq.server.model.TestSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TestSubmissionRepository extends JpaRepository<TestSubmission, Long> {
    List<TestSubmission> findByTestId(Long testId);
    List<TestSubmission> findByTestIdAndUserUsername(Long testId, String username);
    boolean existsByTestIdAndUserUsername(Long testId, String username);
}