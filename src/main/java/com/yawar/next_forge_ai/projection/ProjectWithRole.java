package com.yawar.next_forge_ai.projection;

import com.yawar.next_forge_ai.entity.Project;
import com.yawar.next_forge_ai.entity.enums.ProjectMemberRole;

public interface ProjectWithRole {
    Project getProject();
    ProjectMemberRole getRole();
}
