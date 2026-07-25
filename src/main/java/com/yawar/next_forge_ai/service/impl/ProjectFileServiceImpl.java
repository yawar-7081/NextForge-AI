package com.yawar.next_forge_ai.service.impl;

import com.yawar.next_forge_ai.dto.ProjectFileContentResponse;
import com.yawar.next_forge_ai.dto.ProjectFileResponse;
import com.yawar.next_forge_ai.entity.Project;
import com.yawar.next_forge_ai.entity.ProjectFile;
import com.yawar.next_forge_ai.error.BadRequestException;
import com.yawar.next_forge_ai.repository.ProjectFileRepository;
import com.yawar.next_forge_ai.repository.ProjectRepository;
import com.yawar.next_forge_ai.security.JwtService;
import com.yawar.next_forge_ai.service.ProjectFileService;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectFileServiceImpl implements ProjectFileService {

    private final ProjectRepository projectRepository;
    private final ProjectFileRepository projectFileRepository;
    private final MinioClient minioClient;
    private final JwtService jwtService;

    private static final String BUCKET_NAME = "projects";

    @Override
    public List<ProjectFileResponse> getProjectFilePaths(String projectId) {
        String userId = jwtService.getLoggedInUserId();
        Project project = projectRepository.findAccessibleProject(projectId,userId)
                .orElseThrow(() -> new BadRequestException("You can't access this project file tree"));

        List<ProjectFile> projectFiles = projectFileRepository.findByProjectId(projectId);

        return projectFiles
                .stream()
                .map(pf -> ProjectFileResponse.builder().path(pf.getPath()).build())
                .toList();
    }

    @Override
    public ProjectFileContentResponse getProjectPathContent(String projectId, String path) {

        String userId = jwtService.getLoggedInUserId();
        Project project = projectRepository.findAccessibleProject(projectId,userId)
                .orElseThrow(() -> new BadRequestException("You can't access this project file tree"));

        String objectName = projectId + "/" +  path;

        try{
            InputStream is = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(BUCKET_NAME)
                            .object(objectName)
                            .build()
            );

            String content = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            return ProjectFileContentResponse.builder()
                    .path(path)
                    .content(content)
                    .build();
        } catch (Exception e) {
            log.error("Error While Fetching Path Content");
            throw new RuntimeException(e);
        }

    }
}
