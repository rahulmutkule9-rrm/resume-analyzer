package com.resumeanalyzer.repository;

import com.resumeanalyzer.model.Resume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ResumeRepository extends JpaRepository<Resume, Integer> {
    List<Resume> findByUserId(Integer userId);
    Optional<Resume> findById(Integer id);
    List<Resume> findByUserIdOrderByCreatedAtDesc(Integer userId);
}