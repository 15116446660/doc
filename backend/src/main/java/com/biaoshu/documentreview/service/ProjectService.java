package com.biaoshu.documentreview.service;

import com.biaoshu.documentreview.dto.ProjectDTO;
import com.biaoshu.documentreview.dto.ProjectMemberDTO;
import com.biaoshu.documentreview.dto.ProjectMilestoneDTO;
import com.biaoshu.documentreview.entity.Project;
import com.biaoshu.documentreview.entity.ProjectMember;
import com.biaoshu.documentreview.entity.ProjectMilestone;
import com.biaoshu.documentreview.entity.User;
import com.biaoshu.documentreview.exception.BusinessException;
import com.biaoshu.documentreview.entity.ProjectCategory;
import com.biaoshu.documentreview.repository.ProjectCategoryRepository;
import com.biaoshu.documentreview.repository.ProjectMemberRepository;
import com.biaoshu.documentreview.repository.ProjectMilestoneRepository;
import com.biaoshu.documentreview.repository.ProjectRepository;
import com.biaoshu.documentreview.repository.UserRepository;
import com.biaoshu.documentreview.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 项目服务类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectMilestoneRepository projectMilestoneRepository;
    private final ProjectCategoryRepository categoryRepository;
    private final UserRepository userRepository;

    /**
     * 创建项目
     */
    @Transactional
    public ProjectDTO createProject(ProjectDTO.CreateRequest request) {
        log.info("创建项目: {}", request.getName());

        // 验证项目编号唯一性
        if (StringUtils.hasText(request.getCode()) && projectRepository.existsByCode(request.getCode())) {
            throw new BusinessException("项目编号已存在");
        }

        // 验证项目负责人
        if (request.getManagerId() != null && !userRepository.existsById(request.getManagerId())) {
            throw new BusinessException("项目负责人不存在");
        }

        // 创建项目
        Project project = new Project();
        BeanUtils.copyProperties(request, project);
        
        // 如果没有提供项目编号，自动生成
        if (!StringUtils.hasText(project.getCode())) {
            project.setCode(generateProjectCode());
        }

        project = projectRepository.save(project);

        // 添加项目成员
        if (request.getMemberIds() != null && !request.getMemberIds().isEmpty()) {
            addProjectMembers(project.getId(), request.getMemberIds());
        }

        // 如果指定了项目负责人，确保负责人也是项目成员
        if (project.getManagerId() != null) {
            ensureManagerIsMember(project.getId(), project.getManagerId());
        }

        return convertToDTO(project);
    }

    /**
     * 更新项目
     */
    @Transactional
    public ProjectDTO updateProject(Long id, ProjectDTO.UpdateRequest request) {
        log.info("更新项目: {}", id);

        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new BusinessException("项目不存在"));

        // 检查权限
        checkProjectPermission(id, "WRITE");

        // 验证项目编号唯一性
        if (StringUtils.hasText(request.getCode()) && 
            projectRepository.existsByCodeAndIdNot(request.getCode(), id)) {
            throw new BusinessException("项目编号已存在");
        }

        // 验证项目负责人
        if (request.getManagerId() != null && !userRepository.existsById(request.getManagerId())) {
            throw new BusinessException("项目负责人不存在");
        }

        // 更新项目信息
        if (StringUtils.hasText(request.getName())) {
            project.setName(request.getName());
        }
        if (StringUtils.hasText(request.getCode())) {
            project.setCode(request.getCode());
        }
        if (StringUtils.hasText(request.getDescription())) {
            project.setDescription(request.getDescription());
        }
        if (request.getStatus() != null) {
            project.setStatus(request.getStatus());
        }
        if (request.getPriority() != null) {
            project.setPriority(request.getPriority());
        }
        if (request.getManagerId() != null) {
            project.setManagerId(request.getManagerId());
            ensureManagerIsMember(id, request.getManagerId());
        }
        if (request.getPlannedStartTime() != null) {
            project.setPlannedStartTime(request.getPlannedStartTime());
        }
        if (request.getPlannedEndTime() != null) {
            project.setPlannedEndTime(request.getPlannedEndTime());
        }
        if (request.getActualStartTime() != null) {
            project.setActualStartTime(request.getActualStartTime());
        }
        if (request.getActualEndTime() != null) {
            project.setActualEndTime(request.getActualEndTime());
        }
        if (request.getBudget() != null) {
            project.setBudget(request.getBudget());
        }
        if (StringUtils.hasText(request.getTags())) {
            project.setTags(request.getTags());
        }

        project = projectRepository.save(project);
        return convertToDTO(project);
    }

    /**
     * 删除项目
     */
    @Transactional
    public void deleteProject(Long id) {
        log.info("删除项目: {}", id);

        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new BusinessException("项目不存在"));

        // 检查权限
        checkProjectPermission(id, "ADMIN");

        // 删除项目成员
        projectMemberRepository.deleteByProjectId(id);

        // 删除项目里程碑
        projectMilestoneRepository.deleteByProjectId(id);

        // 删除项目
        projectRepository.delete(project);
    }

    /**
     * 获取项目详情
     */
    public ProjectDTO getProject(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new BusinessException("项目不存在"));

        // 检查权限
        checkProjectPermission(id, "READ");

        return convertToDTO(project);
    }

    /**
     * 分页查询项目
     */
    public Page<ProjectDTO> getProjects(ProjectDTO.QueryRequest request) {
        // 构建分页参数
        Sort sort = Sort.by(Sort.Direction.fromString(request.getSortDir()), request.getSortBy());
        Pageable pageable = PageRequest.of(request.getPage() - 1, request.getSize(), sort);

        // 处理分类ID
        List<Long> categoryIds = null;
        if (request.getCategoryId() != null) {
            categoryIds = getAllSubCategoryIds(request.getCategoryId());
            categoryIds.add(request.getCategoryId());
        }

        // 查询项目
        Page<Project> projectPage = projectRepository.findProjectsWithConditions(
                request.getName(),
                request.getStatus(),
                request.getManagerId(),
                request.getPriority(),
                categoryIds,
                pageable
        );

        return projectPage.map(this::convertToDTO);
    }

    /**
     * 获取用户参与的项目
     */
    public List<ProjectDTO> getUserProjects(Long userId) {
        List<Project> projects = projectRepository.findProjectsByUserId(userId);
        return projects.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    /**
     * 获取用户管理的项目
     */
    public List<ProjectDTO> getManagedProjects(Long userId) {
        List<Project> projects = projectRepository.findManagedProjectsByUserId(userId);
        return projects.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    /**
     * 获取即将到期的项目
     */
    public List<ProjectDTO> getProjectsNearDeadline(int days) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime deadline = now.plusDays(days);
        List<Project> projects = projectRepository.findProjectsNearDeadline(now, deadline);
        return projects.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    /**
     * 统计项目状态
     */
    public List<Object[]> getProjectStatusStatistics() {
        return projectRepository.countProjectsByStatus();
    }

    private List<Long> getAllSubCategoryIds(Long parentId) {
        List<Long> allIds = new ArrayList<>();
        List<ProjectCategory> directChildren = categoryRepository.findByParentId(parentId);
        for (ProjectCategory child : directChildren) {
            allIds.add(child.getId());
            allIds.addAll(getAllSubCategoryIds(child.getId()));
        }
        return allIds;
    }

    /**
     * 生成项目编号
     */
    private String generateProjectCode() {
        String prefix = "PRJ";
        String timestamp = String.valueOf(System.currentTimeMillis());
        return prefix + timestamp.substring(timestamp.length() - 8);
    }

    /**
     * 添加项目成员
     */
    private void addProjectMembers(Long projectId, List<Long> memberIds) {
        for (Long memberId : memberIds) {
            if (userRepository.existsById(memberId)) {
                // 检查是否已经是成员
                Optional<ProjectMember> existingMember = projectMemberRepository
                        .findByProjectIdAndUserId(projectId, memberId);
                
                if (existingMember.isEmpty()) {
                    ProjectMember member = new ProjectMember();
                    member.setProjectId(projectId);
                    member.setUserId(memberId);
                    member.setRole(ProjectMember.MemberRole.MEMBER);
                    member.setPermission(ProjectMember.MemberPermission.READ);
                    member.setIsActive(true);
                    projectMemberRepository.save(member);
                }
            }
        }
    }

    /**
     * 确保项目负责人是项目成员
     */
    private void ensureManagerIsMember(Long projectId, Long managerId) {
        Optional<ProjectMember> existingMember = projectMemberRepository
                .findByProjectIdAndUserId(projectId, managerId);
        
        if (existingMember.isEmpty()) {
            ProjectMember member = new ProjectMember();
            member.setProjectId(projectId);
            member.setUserId(managerId);
            member.setRole(ProjectMember.MemberRole.MANAGER);
            member.setPermission(ProjectMember.MemberPermission.ADMIN);
            member.setIsActive(true);
            projectMemberRepository.save(member);
        } else {
            // 更新现有成员为管理员
            ProjectMember member = existingMember.get();
            member.setRole(ProjectMember.MemberRole.MANAGER);
            member.setPermission(ProjectMember.MemberPermission.ADMIN);
            member.setIsActive(true);
            projectMemberRepository.save(member);
        }
    }

    /**
     * 检查项目权限
     */
    private void checkProjectPermission(Long projectId, String permission) {
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
                    throw new BusinessException("无权限修改该项目");
                }
                break;
            case "ADMIN":
                if (!projectMemberRepository.hasAdminPermission(projectId, currentUserId)) {
                    throw new BusinessException("无权限管理该项目");
                }
                break;
        }
    }

    /**
     * 转换为DTO
     */
    private ProjectDTO convertToDTO(Project project) {
        ProjectDTO dto = new ProjectDTO();
        BeanUtils.copyProperties(project, dto);

        // 设置负责人姓名
        if (project.getManagerId() != null) {
            userRepository.findById(project.getManagerId())
                    .ifPresent(user -> dto.setManagerName(user.getRealName()));
        }

        // 设置统计信息
        dto.setMemberCount(Math.toIntExact(projectMemberRepository.countActiveMembers(project.getId())));
        dto.setMilestoneCount(projectMilestoneRepository.findByProjectIdOrderBySortOrderAsc(project.getId()).size());
        
        // 计算项目进度
        Double progress = projectMilestoneRepository.calculateProjectProgress(project.getId());
        dto.setProgress(progress != null ? progress : 0.0);

        return dto;
    }
}
