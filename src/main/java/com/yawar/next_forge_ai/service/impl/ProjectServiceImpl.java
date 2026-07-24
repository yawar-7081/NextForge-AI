package com.yawar.next_forge_ai.service.impl;

import com.yawar.next_forge_ai.dto.ProjectRequest;
import com.yawar.next_forge_ai.dto.ProjectResponse;
import com.yawar.next_forge_ai.dto.ProjectSummaryResponse;
import com.yawar.next_forge_ai.entity.Project;
import com.yawar.next_forge_ai.entity.ProjectMember;
import com.yawar.next_forge_ai.entity.User;
import com.yawar.next_forge_ai.entity.enums.ProjectMemberRole;
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

import java.util.List;


@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectTemplateService projectTemplateService;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    private final String userId = "50ca1f20-e0c0-4bae-aff6-6f13d6137512";

    @Override
    public ProjectResponse createProject(ProjectRequest projectRequest) {

        // User ID
        User tempUser = userRepository.findByEmail("admin1@nextforge.ai").orElseThrow(
                () -> new ResourceNotFoundException("User","admin1@nextforge.ai")
        );

        Project newProject = Project.builder()
                .user(tempUser)
                .name(projectRequest.getProjectName())
                .build();

        newProject = projectRepository.save(newProject);

        ProjectMember projectMember = ProjectMember.builder()
                .project(newProject)
                .user(tempUser)
                .projectMemberRole(ProjectMemberRole.OWNER)
                .build();

        projectMemberRepository.save(projectMember);

        projectTemplateService.initializeProjectTemplate(newProject.getId());

        return modelMapper.map(newProject,ProjectResponse.class);
    }

    @Override
    public ProjectSummaryResponse getProjectById(String projectId) {


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

        Project project = projectRepository.findAccessibleProject(projectId,userId)
                .orElseThrow(() -> new ResourceNotFoundException("Project",projectId));

        project.setName(projectRequest.getProjectName());

        return modelMapper.map(project,ProjectResponse.class);
    }

    @Transactional
    @Override
    public void deleteProject(String projectId) {
        Project project = projectRepository.findAccessibleProject(projectId,userId)
                .orElseThrow(() -> new ResourceNotFoundException("Project",projectId));

        project.setDeleted(true);
    }

    @Override
    public List<ProjectSummaryResponse> getAllAccessibleProject() {
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
