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
import com.yawar.next_forge_ai.security.JwtService;
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
    private final JwtService jwtService;

    @Override
    public ProjectResponse createProject(ProjectRequest projectRequest) {

        User user = userRepository.findById(jwtService.getLoggedInUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", jwtService.getLoggedInUserId()));



        Project newProject = Project.builder()
                .user(user)
                .name(projectRequest.getProjectName())
                .build();

        newProject = projectRepository.save(newProject);

        ProjectMember projectMember = ProjectMember.builder()
                .project(newProject)
                .user(user)
                .projectMemberRole(ProjectMemberRole.OWNER)
                .build();

        projectMemberRepository.save(projectMember);

        projectTemplateService.initializeProjectTemplate(newProject.getId());

        return modelMapper.map(newProject,ProjectResponse.class);
    }

    @Override
    public ProjectSummaryResponse getProjectById(String projectId) {

        String userId = jwtService.getLoggedInUserId();

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
        String userId = jwtService.getLoggedInUserId();
        Project project = projectRepository.findAccessibleProject(projectId,userId)
                .orElseThrow(() -> new ResourceNotFoundException("Project",projectId));

        project.setName(projectRequest.getProjectName());

        return modelMapper.map(project,ProjectResponse.class);
    }

    @Transactional
    @Override
    public void deleteProject(String projectId) {
        String userId = jwtService.getLoggedInUserId();
        Project project = projectRepository.findAccessibleProject(projectId,userId)
                .orElseThrow(() -> new ResourceNotFoundException("Project",projectId));

        project.setDeleted(true);
    }

    @Override
    public List<ProjectSummaryResponse> getAllAccessibleProject() {
        String userId = jwtService.getLoggedInUserId();
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
