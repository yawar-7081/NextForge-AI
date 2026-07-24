package com.yawar.next_forge_ai.repository;

import com.yawar.next_forge_ai.entity.ProjectMember;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectMemberRepository extends JpaRepository<ProjectMember,String> {

    @Query("""
        SELECT pm FROM ProjectMember pm
        WHERE pm.project.id = :projectId
        AND pm.user.id = :userId
""")
    Optional<ProjectMember> findProjectMemberByProjectIdAndUserId(
            @NotBlank(message = "'projectId' can't be blank in projectMember repository")
            @Param("projectId") String projectId,
            @NotBlank(message = "'userId' can't be blank in projectMember repository")
            @Param("userId") String userId
            );

    @Query("""
        SELECT pm FROM ProjectMember pm
        WHERE pm.project.id = :projectId
        AND pm.isDeleted = false
""")
    List<ProjectMember> getProjectMembers(@NotBlank(message = "'projectId' can't be blank in projectMember repository")
                                          @Param("projectId") String projectId);

    @Query("""
        SELECT pm FROM ProjectMember pm
        WHERE pm.project.id = :projectId
        AND pm.id = :projectMemberId
        AND pm.isDeleted = false
    """)
    Optional<ProjectMember> findByProjectIdAndMemberId(
            @NotBlank(message = "'projectId' can't be blank in projectMember repository")
           @Param("projectId") String projectId,
           @NotBlank(message = "'projectMemberId' can't be blank in projectMember repository")
           @Param("projectMemberId") String projectMemberId
    );
}
