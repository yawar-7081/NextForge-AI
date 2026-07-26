package com.yawar.next_forge_ai.service.impl;

import com.yawar.next_forge_ai.dto.FileNode;
import com.yawar.next_forge_ai.dto.ProjectFileContentResponse;
import com.yawar.next_forge_ai.dto.ProjectFileResponse;
import com.yawar.next_forge_ai.entity.Project;
import com.yawar.next_forge_ai.entity.ProjectFile;
import com.yawar.next_forge_ai.error.BadRequestException;
import com.yawar.next_forge_ai.error.ResourceNotFoundException;
import com.yawar.next_forge_ai.repository.ProjectFileRepository;
import com.yawar.next_forge_ai.repository.ProjectRepository;
import com.yawar.next_forge_ai.security.JwtService;
import com.yawar.next_forge_ai.service.ProjectFileService;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
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
    public ProjectFileResponse getProjectFilePaths(String projectId) {
        String userId = jwtService.getLoggedInUserId();
        Project project = projectRepository.findAccessibleProject(projectId,userId)
                .orElseThrow(() -> new BadRequestException("You can't access this project file tree"));

        List<ProjectFile> projectFiles = projectFileRepository.findByProjectId(projectId);

        return new ProjectFileResponse(
                projectFiles
                .stream()
                .map(pf -> new FileNode(pf.getPath()))
                .toList()
        );
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



    @Override
    public void saveFile(String projectId, String path, String fileContent) {
        log.info("Saving file: {}",path);

        Project project = projectRepository.findById(projectId).orElseThrow(
                () -> new ResourceNotFoundException("Project",projectId.toString())
        );

        String cleanPath = path.startsWith("/") ? path.substring(1) : path;
        String objectKey = projectId + "/" + cleanPath;

        byte[] contentBytes = fileContent.getBytes(StandardCharsets.UTF_8);
        InputStream inputStream = new ByteArrayInputStream(contentBytes);
        try {
            // saving the file content
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(BUCKET_NAME)
                            .object(objectKey)
                            .stream(inputStream,contentBytes.length,-1)
                            .contentType(determineContentType(path))
                            .build()
            );

            // saving the meta data
            ProjectFile file = projectFileRepository.findByProjectIdAndPath(projectId,cleanPath)
                    .orElseGet(() -> ProjectFile.builder()
                            .project(project)
                            .path(cleanPath)
                            .minioObjectKey(objectKey)
                            .createdAt(Instant.now())
                            .build());

            file.setUpdatedAt(Instant.now());
            projectFileRepository.save(file);

        } catch (Exception e) {
            log.error("Failed to save file {}/{}",project,cleanPath,e);
            throw new RuntimeException(e);
        }

    }

    private String determineContentType(String path){
        String type = URLConnection.guessContentTypeFromName(path);

        if(type != null) return type;
        if(path.endsWith(".jsx") || path.endsWith(".ts") || path.endsWith(".tsx")) return "text/javascript";
        if(path.endsWith(".json")) return "application/json";
        if(path.endsWith(".css")) return "text/css";

        return "text/plain";
    }
}
