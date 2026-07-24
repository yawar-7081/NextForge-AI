package com.yawar.next_forge_ai.service;

import com.yawar.next_forge_ai.dto.ProjectRequest;
import com.yawar.next_forge_ai.dto.ProjectResponse;
import com.yawar.next_forge_ai.dto.ProjectSummaryResponse;
import jakarta.validation.Valid;

import java.util.List;

public interface ProjectService {
    ProjectResponse createProject(ProjectRequest projectRequest);

    ProjectSummaryResponse getProjectById(String projectId);

    ProjectResponse updateProject(String projectId, @Valid ProjectRequest projectRequest);

    void deleteProject(String projectId);

    List<ProjectSummaryResponse> getAllAccessibleProject();
}
