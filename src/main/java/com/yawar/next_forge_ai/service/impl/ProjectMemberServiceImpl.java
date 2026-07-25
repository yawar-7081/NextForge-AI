package com.yawar.next_forge_ai.service.impl;

import com.yawar.next_forge_ai.dto.AddMemberRequest;
import com.yawar.next_forge_ai.dto.MemberResponse;
import com.yawar.next_forge_ai.dto.RemoveProjectMemberRequest;
import com.yawar.next_forge_ai.dto.UpdateProjectMemberRoleRequest;
import com.yawar.next_forge_ai.entity.Project;
import com.yawar.next_forge_ai.entity.ProjectMember;
import com.yawar.next_forge_ai.entity.User;
import com.yawar.next_forge_ai.error.BadRequestException;
import com.yawar.next_forge_ai.error.ResourceNotFoundException;
import com.yawar.next_forge_ai.repository.ProjectMemberRepository;
import com.yawar.next_forge_ai.repository.ProjectRepository;
import com.yawar.next_forge_ai.repository.UserRepository;
import com.yawar.next_forge_ai.security.JwtService;
import com.yawar.next_forge_ai.service.ProjectMemberService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


@RequiredArgsConstructor
@Service
public class ProjectMemberServiceImpl implements ProjectMemberService {

    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;


    @Override
    public MemberResponse addMember(String projectId, AddMemberRequest request) {
        String userId = jwtService.getLoggedInUserId();
        Project project = getProject(projectId,userId);

        User invitee = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User",request.getUsername()));

        if(project.getUser().getUsername().equals(request.getUsername())){
            throw new BadRequestException("Can Not Add YourSelf");
        }

        ProjectMember projectMember = projectMemberRepository.findProjectMemberByProjectIdAndUserId(project.getId(), invitee.getId()).get();

        if(projectMember!=null){
            if(!projectMember.isDeleted()) {
                throw new BadRequestException("User is Already Present - "+invitee.getUsername());
            }
            projectMember.setDeleted(false);
        }else{
            projectMember.setProject(project);
            projectMember.setUser(invitee);
            projectMember.setProjectMemberRole(request.getRole());
        }



        projectMember = projectMemberRepository.save(projectMember);
        return MemberResponse.builder()
                .id(projectMember.getId())
                .projectMemberRole(projectMember.getProjectMemberRole())
                .email(projectMember.getUser().getEmail())
                .username(projectMember.getUser().getUsername())
                .createdAt(projectMember.getCreatedAt())
                .build();
    }

    @Override
    public List<MemberResponse> getProjectMember(String projectId) {
        return projectMemberRepository.getProjectMembers(projectId)
                .stream().map(pm -> MemberResponse.builder()
                        .id(pm.getId())
                        .projectMemberRole(pm.getProjectMemberRole())
                        .email(pm.getUser().getEmail())
                        .username(pm.getUser().getUsername())
                        .createdAt(pm.getCreatedAt())
                        .build())
                .toList();
    }

    @Transactional
    @Override
    public MemberResponse updateMemberRole(String projectId, UpdateProjectMemberRoleRequest request) {
        String userId = jwtService.getLoggedInUserId();
        Project project = getProject(projectId,userId);

        ProjectMember projectMember = projectMemberRepository.findByProjectIdAndMemberId(project.getId(),request.getProjectMemberId())
                .orElseThrow(() -> new BadRequestException("Invalid Project Member Id - "+request.getProjectMemberId()));

        if(projectMember.getProjectMemberRole().name().equals(request.getRole().name())){
            throw new BadRequestException("Member Already Has This Role "+request.getRole());
        }

        projectMember.setProjectMemberRole(request.getRole());

        return MemberResponse.builder()
                .id(projectMember.getId())
                .projectMemberRole(projectMember.getProjectMemberRole())
                .email(projectMember.getUser().getEmail())
                .username(projectMember.getUser().getUsername())
                .createdAt(projectMember.getCreatedAt())
                .build();
    }

    @Transactional
    @Override
    public void removeMember(String projectId, RemoveProjectMemberRequest request) {
        String userId = jwtService.getLoggedInUserId();
        Project project = getProject(projectId,userId);
        ProjectMember projectMember = projectMemberRepository.findByProjectIdAndMemberId(project.getId(),request.getProjectMemberId())
                .orElseThrow(() -> new BadRequestException("Invalid Project Member Id - "+request.getProjectMemberId()));

        projectMember.setDeleted(true);
    }


    // INTERNAL METHOD
    private Project getProject(String projectId,String userId){
        return projectRepository.getOwnerProject(projectId,userId)
                .orElseThrow(() -> new ResourceNotFoundException("Project",projectId));
    }

}
