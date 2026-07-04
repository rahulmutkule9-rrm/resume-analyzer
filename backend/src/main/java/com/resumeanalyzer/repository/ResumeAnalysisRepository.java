package com.resumeanalyzer.repository;

import com.resumeanalyzer.model.ResumeAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ResumeAnalysisRepository extends JpaRepository<ResumeAnalysis, Integer> {
    Optional<ResumeAnalysis> findByResumeId(Integer resumeId);
    List<ResumeAnalysis> findByUserId(Integer userId);
    List<ResumeAnalysis> findByUserIdOrderByAnalyzedAtDesc(Integer userId);
}