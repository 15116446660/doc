package com.biaoshu.documentreview.service;

import com.biaoshu.documentreview.dto.ProjectMemberDTO;
import com.biaoshu.documentreview.entity.ProjectMember;
import com.biaoshu.documentreview.entity.User;
import com.biaoshu.documentreview.exception.BusinessException;
import com.biaoshu.documentreview.repository.ProjectMemberRepository;
import com.biaoshu.documentreview.repository.ProjectRepository;
import com.biaoshu.documentreview.repository.UserRepository;
import com.biaoshu.documentreview.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 项目成员服务类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectMemberService {

    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    /**
     * 添加项目成员
     */
    @Transactional
    public ProjectMemberDTO addProjectMember(Long projectId, ProjectMemberDTO.AddRequest request) {
        log.info("添加项目成员: 项目ID={}, 用户ID={}", projectId, request.getUserId());

        // 验证项目存在
        if (!projectRepository.existsById(projectId)) {
            throw new BusinessException("项目不存在");
        }

        // 验证用户存在
        if (!userRepository.existsById(request.getUserId())) {
            throw new BusinessException("用户不存在");
        }

        // 检查权限
        checkProjectMemberPermission(projectId, "ADMIN");

        // 检查是否已经是成员
        Optional<ProjectMember> existingMember = projectMemberRepository
                .findByProjectIdAndUserId(projectId, request.getUserId());

        if (existingMember.isPresent()) {
            ProjectMember member = existingMember.get();
            if (member.getIsActive()) {
                throw new BusinessException("用户已经是项目成员");
            } else {
                // 重新激活成员
                member.setRole(request.getRole());
                member.setPermission(request.getPermission());
                member.setIsActive(true);
                member.setJoinedTime(LocalDateTime.now());
                member.setLeftTime(null);
                member.setRemark(request.getRemark());
                member = projectMemberRepository.save(member);
                return convertToDTO(member);
            }
        }

        // 创建新成员
        ProjectMember member = new ProjectMember();
        member.setProjectId(projectId);
        member.setUserId(request.getUserId());
        member.setRole(request.getRole());
        member.setPermission(request.getPermission());
        member.setIsActive(true);
        member.setRemark(request.getRemark());

        member = projectMemberRepository.save(member);
        return convertToDTO(member);
    }

    /**
     * 批量添加项目成员
     */
    @Transactional
    public List<ProjectMemberDTO> batchAddProjectMembers(Long projectId, ProjectMemberDTO.BatchAddRequest request) {
        log.info("批量添加项目成员: 项目ID={}, 成员数量={}", projectId, request.getMembers().size());

        // 验证项目存在
        if (!projectRepository.existsById(projectId)) {
            throw new BusinessException("项目不存在");
        }

        // 检查权限
        checkProjectMemberPermission(projectId, "ADMIN");

        return request.getMembers().stream()
                .map(memberInfo -> {
                    ProjectMemberDTO.AddRequest addRequest = new ProjectMemberDTO.AddRequest();
                    addRequest.setUserId(memberInfo.getUserId());
                    addRequest.setRole(memberInfo.getRole());
                    addRequest.setPermission(memberInfo.getPermission());
                    addRequest.setRemark(memberInfo.getRemark());
                    
                    try {
                        return addProjectMember(projectId, addRequest);
                    } catch (BusinessException e) {
                        log.warn("添加成员失败: 用户ID={}, 原因={}", memberInfo.getUserId(), e.getMessage());
                        return null;
                    }
                })
                .filter(member -> member != null)
                .collect(Collectors.toList());
    }

    /**
     * 更新项目成员
     */
    @Transactional
    public ProjectMemberDTO updateProjectMember(Long projectId, Long userId, ProjectMemberDTO.UpdateRequest request) {
        log.info("更新项目成员: 项目ID={}, 用户ID={}", projectId, userId);

        ProjectMember member = projectMemberRepository.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new BusinessException("项目成员不存在"));

        // 检查权限
        checkProjectMemberPermission(projectId, "ADMIN");

        // 更新成员信息
        if (request.getRole() != null) {
            member.setRole(request.getRole());
        }
        if (request.getPermission() != null) {
            member.setPermission(request.getPermission());
        }
        if (request.getIsActive() != null) {
            member.setIsActive(request.getIsActive());
            if (!request.getIsActive()) {
                member.setLeftTime(LocalDateTime.now());
            } else {
                member.setLeftTime(null);
            }
        }
        if (request.getRemark() != null) {
            member.setRemark(request.getRemark());
        }

        member = projectMemberRepository.save(member);
        return convertToDTO(member);
    }

    /**
     * 移除项目成员
     */
    @Transactional
    public void removeProjectMember(Long projectId, Long userId) {
        log.info("移除项目成员: 项目ID={}, 用户ID={}", projectId, userId);

        ProjectMember member = projectMemberRepository.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new BusinessException("项目成员不存在"));

        // 检查权限
        checkProjectMemberPermission(projectId, "ADMIN");

        // 软删除：设置为非活跃状态
        member.setIsActive(false);
        member.setLeftTime(LocalDateTime.now());
        projectMemberRepository.save(member);
    }

    /**
     * 批量移除项目成员
     */
    @Transactional
    public void batchRemoveProjectMembers(Long projectId, List<Long> userIds) {
        log.info("批量移除项目成员: 项目ID={}, 用户数量={}", projectId, userIds.size());

        // 检查权限
        checkProjectMemberPermission(projectId, "ADMIN");

        projectMemberRepository.deactivateMembers(projectId, userIds);
    }

    /**
     * 获取项目成员列表
     */
    public List<ProjectMemberDTO> getProjectMembers(Long projectId, Boolean isActive) {
        // 检查权限
        checkProjectMemberPermission(projectId, "READ");

        List<ProjectMember> members;
        if (isActive != null && isActive) {
            members = projectMemberRepository.findByProjectIdAndIsActiveTrue(projectId);
        } else {
            members = projectMemberRepository.findByProjectId(projectId);
        }

        return members.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    /**
     * 分页查询项目成员
     */
    public Page<ProjectMemberDTO> getProjectMembers(ProjectMemberDTO.QueryRequest request) {
        // 如果指定了项目ID，检查权限
        if (request.getProjectId() != null) {
            checkProjectMemberPermission(request.getProjectId(), "READ");
        }

        Pageable pageable = PageRequest.of(request.getPage() - 1, request.getSize());
        
        // 这里需要实现复杂查询，暂时简化处理
        List<ProjectMember> members = projectMemberRepository.findByProjectIdAndIsActiveTrue(request.getProjectId());
        
        // 简单的内存分页（生产环境应该使用数据库分页）
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), members.size());
        List<ProjectMember> pageMembers = members.subList(start, end);
        
        List<ProjectMemberDTO> memberDTOs = pageMembers.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return new org.springframework.data.domain.PageImpl<>(memberDTOs, pageable, members.size());
    }

    /**
     * 获取项目成员统计
     */
    public ProjectMemberDTO.Statistics getProjectMemberStatistics(Long projectId) {
        // 检查权限
        checkProjectMemberPermission(projectId, "READ");

        List<ProjectMember> allMembers = projectMemberRepository.findByProjectId(projectId);
        List<ProjectMember> activeMembers = projectMemberRepository.findByProjectIdAndIsActiveTrue(projectId);

        ProjectMemberDTO.Statistics statistics = new ProjectMemberDTO.Statistics();
        statistics.setTotalMembers((long) allMembers.size());
        statistics.setActiveMembers((long) activeMembers.size());

        // 统计各角色数量
        statistics.setManagers(activeMembers.stream()
                .filter(m -> m.getRole() == ProjectMember.MemberRole.MANAGER)
                .count());
        statistics.setMembers(activeMembers.stream()
                .filter(m -> m.getRole() == ProjectMember.MemberRole.MEMBER)
                .count());
        statistics.setReviewers(activeMembers.stream()
                .filter(m -> m.getRole() == ProjectMember.MemberRole.REVIEWER)
                .count());
        statistics.setObservers(activeMembers.stream()
                .filter(m -> m.getRole() == ProjectMember.MemberRole.OBSERVER)
                .count());

        // 统计各权限数量
        statistics.setAdmins(activeMembers.stream()
                .filter(m -> m.getPermission() == ProjectMember.MemberPermission.ADMIN)
                .count());
        statistics.setWriters(activeMembers.stream()
                .filter(m -> m.getPermission() == ProjectMember.MemberPermission.WRITE)
                .count());
        statistics.setReaders(activeMembers.stream()
                .filter(m -> m.getPermission() == ProjectMember.MemberPermission.READ)
                .count());

        return statistics;
    }

    /**
     * 检查项目成员权限
     */
    private void checkProjectMemberPermission(Long projectId, String permission) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null) {
            throw new BusinessException("用户未登录");
        }

        // 系统管理员拥有所有权限
        if (SecurityUtils.hasRole("ADMIN")) {
            return;
        }

        // 检查是否是项目成员
        if (!projectMemberRepository.isProjectMember(projectId, currentUserId)) {
            throw new BusinessException("无权限访问该项目");
        }

        // 检查具体权限
        switch (permission) {
            case "READ":
                // 项目成员都有读权限
                break;
            case "WRITE":
                if (!projectMemberRepository.hasWritePermission(projectId, currentUserId)) {
                    throw new BusinessException("无权限修改项目成员");
                }
                break;
            case "ADMIN":
                if (!projectMemberRepository.hasAdminPermission(projectId, currentUserId)) {
                    throw new BusinessException("无权限管理项目成员");
                }
                break;
        }
    }

    /**
     * 转换为DTO
     */
    private ProjectMemberDTO convertToDTO(ProjectMember member) {
        ProjectMemberDTO dto = new ProjectMemberDTO();
        BeanUtils.copyProperties(member, dto);

        // 设置用户信息
        if (member.getUserId() != null) {
            userRepository.findById(member.getUserId()).ifPresent(user -> {
                dto.setUsername(user.getUsername());
                dto.setRealName(user.getRealName());
                dto.setEmail(user.getEmail());
                // dto.setDepartment(user.getDepartment()); // 如果User实体有部门字段
            });
        }

        // 设置项目信息
        if (member.getProjectId() != null) {
            projectRepository.findById(member.getProjectId()).ifPresent(project -> {
                dto.setProjectName(project.getName());
            });
        }

        return dto;
    }
}
