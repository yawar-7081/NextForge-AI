package com.yawar.next_forge_ai.service.impl;

import com.yawar.next_forge_ai.entity.Project;
import com.yawar.next_forge_ai.entity.ProjectFile;
import com.yawar.next_forge_ai.error.ResourceNotFoundException;
import com.yawar.next_forge_ai.repository.ProjectFileRepository;
import com.yawar.next_forge_ai.repository.ProjectRepository;
import com.yawar.next_forge_ai.service.ProjectTemplateService;
import io.minio.*;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectTemplateServiceImpl implements ProjectTemplateService {

    private final ProjectFileRepository projectFileRepository;
    private final ProjectRepository projectRepository;
    private final MinioClient minioClient;


    private static final String TEMPLATE_BUCKET = "starter-project";
    private static final String TARGET_BUCKET = "projects";
    private static final String TEMPLATE_NAME = "react-starter-template-project";

    @Override
    public void initializeProjectTemplate(String projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project",projectId));

        try{
            Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(TEMPLATE_BUCKET)
                        .prefix(TEMPLATE_NAME+"/")
                        .recursive(true)
                        .build()
            );

            //for meta data in postgres db
            List<ProjectFile> fileToSave = new ArrayList<>();

            for(Result<Item> result : results){
                Item item = result.get();
                String sourceKey = result.get().objectName();

                String cleanPath = sourceKey.replace(TEMPLATE_NAME+"/","");
                String destKey = projectId + "/" + cleanPath;

                minioClient.copyObject(
                    CopyObjectArgs.builder()
                            .bucket(TARGET_BUCKET)
                            .object(destKey)
                            .source(CopySource.builder()
                                    .bucket(TEMPLATE_BUCKET)
                                    .object(sourceKey)
                                    .build())
                            .build()
                );

                ProjectFile projectFile = ProjectFile.builder()
                        .project(project)
                        .path(cleanPath)
                        .minioObjectKey(destKey)
                        .build();

                fileToSave.add(projectFile);
            }

            projectFileRepository.saveAll(fileToSave);

        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize project from template", e);
        }

    }
}
