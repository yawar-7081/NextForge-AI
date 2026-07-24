package com.yawar.next_forge_ai.service.impl;

import com.yawar.next_forge_ai.dto.ProjectRequest;
import com.yawar.next_forge_ai.dto.ProjectResponse;
import com.yawar.next_forge_ai.dto.ProjectSummaryResponse;
import com.yawar.next_forge_ai.entity.Project;
import com.yawar.next_forge_ai.entity.ProjectMember;
import com.yawar.next_forge_ai.entity.User;
import com.yawar.next_forge_ai.entity.enums.ProjectRole;
import com.yawar.next_forge_ai.entity.enums.Provider;
import com.yawar.next_forge_ai.error.ResourceNotFoundException;
import com.yawar.next_forge_ai.projection.ProjectWithRole;
import com.yawar.next_forge_ai.repository.ProjectMemberRepository;
import com.yawar.next_forge_ai.repository.ProjectRepository;
import com.yawar.next_forge_ai.repository.UserRepository;
import com.yawar.next_forge_ai.service.ProjectService;
import com.yawar.next_forge_ai.service.ProjectTemplateService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.nio.ReadOnlyBufferException;
import java.util.List;


@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectTemplateService projectTemplateService;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @Override
    public ProjectResponse createProject(ProjectRequest projectRequest) {

        // User ID
        User tempUser = userRepository.findByEmail("admin@nextforge.ai").orElseThrow(
                () -> new ResourceNotFoundException("User","admin@nextforge.ai")
        );

        Project newProject = Project.builder()
                .user(tempUser)
                .name(projectRequest.getProjectName())
                .build();

        newProject = projectRepository.save(newProject);

        ProjectMember projectMember = ProjectMember.builder()
                .project(newProject)
                .user(tempUser)
                .projectRole(ProjectRole.OWNER)
                .build();

        projectMemberRepository.save(projectMember);

        projectTemplateService.initializeProjectTemplate(newProject.getId());

        return modelMapper.map(newProject,ProjectResponse.class);
    }

    @Override
    public ProjectSummaryResponse getProjectById(String projectId) {

        String userId = "1e3924fa-c453-4f34-8430-187c04ee57c8";

        ProjectWithRole project = projectRepository.getInMemberProject(projectId,userId)
                .orElseThrow(() -> new ResourceNotFoundException("Project",projectId));

        return new ProjectSummaryResponse(
                project.getProject().getId(),
                project.getProject().getName(),
                project.getRole(),
                project.getProject().getCreatedAt(),
                project.getProject().getUpdatedAt()
        );
    }

    @Transactional
    @Override
    public ProjectResponse updateProject(String projectId, ProjectRequest projectRequest) {
        String userId = "3edf66bd-ca86-4b0c-b3fb-b1443168b830";

        Project project = projectRepository.findAccessibleProject(projectId,userId)
                .orElseThrow(() -> new ResourceNotFoundException("Project",projectId));

        project.setName(projectRequest.getProjectName());

        return modelMapper.map(project,ProjectResponse.class);
    }

    @Transactional
    @Override
    public void deleteProject(String projectId) {
        String userId = "1e3924fa-c453-4f34-8430-187c04ee57c8";
        Project project = projectRepository.findAccessibleProject(projectId,userId)
                .orElseThrow(() -> new ResourceNotFoundException("Project",projectId));

        project.setDeleted(true);
    }

    @Override
    public List<ProjectSummaryResponse> getAllAccessibleProject() {
        String userId = "1e3924fa-c453-4f34-8430-187c04ee57c8";
        List<ProjectWithRole> projects = projectRepository.findAllAccessibleProjects(userId);
        return projects.stream().map(project -> new ProjectSummaryResponse(
                project.getProject().getId(),
                project.getProject().getName(),
                project.getRole(),
                project.getProject().getCreatedAt(),
                project.getProject().getUpdatedAt()
        )).toList();
    }

}
