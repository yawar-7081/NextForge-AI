package com.yawar.next_forge_ai.service;

import com.yawar.next_forge_ai.dto.ProjectFileContentResponse;
import com.yawar.next_forge_ai.dto.ProjectFileResponse;

import java.util.List;

public interface ProjectFileService {
    List<ProjectFileResponse> getProjectFilePaths(String projectId);

    ProjectFileContentResponse getProjectPathContent(String projectId, String path);
}
