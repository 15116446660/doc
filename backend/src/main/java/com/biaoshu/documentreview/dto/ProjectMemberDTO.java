package com.biaoshu.documentreview.dto;

import com.biaoshu.documentreview.entity.ProjectMember;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 项目成员数据传输对象
 */
@Data
@Schema(description = "项目成员信息")
public class ProjectMemberDTO {

    @Schema(description = "成员ID")
    private Long id;

    @Schema(description = "项目ID")
    private Long projectId;

    @Schema(description = "项目名称")
    private String projectName;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "用户姓名")
    private String realName;

    @Schema(description = "用户邮箱")
    private String email;

    @Schema(description = "用户部门")
    private String department;

    @Schema(description = "成员角色")
    private ProjectMember.MemberRole role;

    @Schema(description = "成员权限")
    private ProjectMember.MemberPermission permission;

    @Schema(description = "加入时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime joinedTime;

    @Schema(description = "离开时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime leftTime;

    @Schema(description = "是否激活")
    private Boolean isActive;

    @Schema(description = "备注")
    private String remark;

    /**
     * 添加项目成员请求DTO
     */
    @Data
    @Schema(description = "添加项目成员请求")
    public static class AddRequest {
        @Schema(description = "用户ID")
        @NotNull(message = "用户ID不能为空")
        private Long userId;

        @Schema(description = "成员角色")
        @NotNull(message = "成员角色不能为空")
        private ProjectMember.MemberRole role;

        @Schema(description = "成员权限")
        private ProjectMember.MemberPermission permission = ProjectMember.MemberPermission.READ;

        @Schema(description = "备注")
        private String remark;
    }

    /**
     * 批量添加项目成员请求DTO
     */
    @Data
    @Schema(description = "批量添加项目成员请求")
    public static class BatchAddRequest {
        @Schema(description = "成员信息列表")
        @NotNull(message = "成员信息列表不能为空")
        private List<MemberInfo> members;

        @Data
        @Schema(description = "成员信息")
        public static class MemberInfo {
            @Schema(description = "用户ID")
            @NotNull(message = "用户ID不能为空")
            private Long userId;

            @Schema(description = "成员角色")
            @NotNull(message = "成员角色不能为空")
            private ProjectMember.MemberRole role;

            @Schema(description = "成员权限")
            private ProjectMember.MemberPermission permission = ProjectMember.MemberPermission.READ;

            @Schema(description = "备注")
            private String remark;
        }
    }

    /**
     * 更新项目成员请求DTO
     */
    @Data
    @Schema(description = "更新项目成员请求")
    public static class UpdateRequest {
        @Schema(description = "成员角色")
        private ProjectMember.MemberRole role;

        @Schema(description = "成员权限")
        private ProjectMember.MemberPermission permission;

        @Schema(description = "是否激活")
        private Boolean isActive;

        @Schema(description = "备注")
        private String remark;
    }

    /**
     * 项目成员查询请求DTO
     */
    @Data
    @Schema(description = "项目成员查询请求")
    public static class QueryRequest {
        @Schema(description = "项目ID")
        private Long projectId;

        @Schema(description = "用户ID")
        private Long userId;

        @Schema(description = "用户名（模糊查询）")
        private String username;

        @Schema(description = "成员角色")
        private ProjectMember.MemberRole role;

        @Schema(description = "成员权限")
        private ProjectMember.MemberPermission permission;

        @Schema(description = "是否激活")
        private Boolean isActive = true;

        @Schema(description = "页码", example = "1")
        private Integer page = 1;

        @Schema(description = "每页大小", example = "10")
        private Integer size = 10;
    }

    /**
     * 项目成员统计DTO
     */
    @Data
    @Schema(description = "项目成员统计")
    public static class Statistics {
        @Schema(description = "总成员数")
        private Long totalMembers;

        @Schema(description = "活跃成员数")
        private Long activeMembers;

        @Schema(description = "项目经理数")
        private Long managers;

        @Schema(description = "普通成员数")
        private Long members;

        @Schema(description = "审核员数")
        private Long reviewers;

        @Schema(description = "观察者数")
        private Long observers;

        @Schema(description = "管理员权限数")
        private Long admins;

        @Schema(description = "读写权限数")
        private Long writers;

        @Schema(description = "只读权限数")
        private Long readers;
    }
}
