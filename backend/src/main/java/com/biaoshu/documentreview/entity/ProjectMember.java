package com.biaoshu.documentreview.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 项目成员实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "project_member", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"project_id", "user_id"})
})
@Schema(description = "项目成员信息")
public class ProjectMember extends BaseEntity {

    @Schema(description = "项目ID")
    @NotNull(message = "项目ID不能为空")
    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Schema(description = "项目")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", insertable = false, updatable = false)
    private Project project;

    @Schema(description = "用户ID")
    @NotNull(message = "用户ID不能为空")
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Schema(description = "用户")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @Schema(description = "成员角色：MANAGER-项目经理，MEMBER-普通成员，REVIEWER-审核员，OBSERVER-观察者")
    @NotNull(message = "成员角色不能为空")
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private MemberRole role;

    @Schema(description = "权限：READ-只读，WRITE-读写，ADMIN-管理员")
    @Enumerated(EnumType.STRING)
    @Column(name = "permission")
    private MemberPermission permission = MemberPermission.READ;

    @Schema(description = "加入时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "joined_time")
    private LocalDateTime joinedTime;

    @Schema(description = "离开时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "left_time")
    private LocalDateTime leftTime;

    @Schema(description = "是否激活")
    @Column(name = "is_active")
    private Boolean isActive = true;

    @Schema(description = "备注")
    @Column(name = "remark", length = 500)
    private String remark;

    /**
     * 成员角色枚举
     */
    public enum MemberRole {
        MANAGER("项目经理"),
        MEMBER("普通成员"),
        REVIEWER("审核员"),
        OBSERVER("观察者");

        private final String description;

        MemberRole(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 成员权限枚举
     */
    public enum MemberPermission {
        READ("只读"),
        WRITE("读写"),
        ADMIN("管理员");

        private final String description;

        MemberPermission(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    @PrePersist
    public void prePersist() {
        if (joinedTime == null) {
            joinedTime = LocalDateTime.now();
        }
    }
}
