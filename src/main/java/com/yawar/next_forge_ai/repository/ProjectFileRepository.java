package com.yawar.next_forge_ai.repository;

import com.yawar.next_forge_ai.entity.ProjectFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectFileRepository extends JpaRepository<ProjectFile,String> {
    List<ProjectFile> findByProjectId(String projectId);

    Optional<ProjectFile> findByProjectIdAndPath(String projectId, String cleanPath);
}
