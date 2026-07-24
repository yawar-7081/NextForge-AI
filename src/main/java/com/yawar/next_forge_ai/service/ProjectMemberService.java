package com.yawar.next_forge_ai.service;


import com.yawar.next_forge_ai.dto.AddMemberRequest;
import com.yawar.next_forge_ai.dto.MemberResponse;
import com.yawar.next_forge_ai.dto.RemoveProjectMemberRequest;
import com.yawar.next_forge_ai.dto.UpdateProjectMemberRoleRequest;
import com.yawar.next_forge_ai.entity.enums.ProjectMemberRole;

import java.util.List;

public interface ProjectMemberService {
    MemberResponse addMember(String projectId, AddMemberRequest request);

    List<MemberResponse> getProjectMember(String projectId);

    MemberResponse updateMemberRole(String projectId, UpdateProjectMemberRoleRequest request);

    void removeMember(String projectId, RemoveProjectMemberRequest request);
}
