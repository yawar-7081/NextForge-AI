package com.yawar.next_forge_ai.controller;

import com.yawar.next_forge_ai.dto.ProjectFileContentResponse;
import com.yawar.next_forge_ai.dto.ProjectFileResponse;
import com.yawar.next_forge_ai.service.ProjectFileService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/project-file/{projectId}")
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class ProjectFileController {

    ProjectFileService projectFileService;

    @GetMapping
    public ResponseEntity<List<ProjectFileResponse>> getProjectPaths(
            @PathVariable(value = "projectId", required = true) String projectId
    ){
        List<ProjectFileResponse> responses = projectFileService.getProjectFilePaths(projectId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/content")
    public ResponseEntity<ProjectFileContentResponse> getProjectPathContent(
            @PathVariable(value = "projectId", required = true) String projectId,
            @RequestParam(value = "path",required = true) String path
    ){
        ProjectFileContentResponse responses = projectFileService.getProjectPathContent(projectId,path);
        return ResponseEntity.ok(responses);
    }

}
