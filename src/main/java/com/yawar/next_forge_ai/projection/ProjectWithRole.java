package com.yawar.next_forge_ai.projection;

import com.yawar.next_forge_ai.entity.Project;
import com.yawar.next_forge_ai.entity.ProjectMember;
import com.yawar.next_forge_ai.entity.enums.ProjectRole;

public interface ProjectWithRole {
    Project getProject();
    ProjectRole getRole();
}
