package com.biaoshu.documentreview.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 项目实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "project")
@Schema(description = "项目信息")
public class Project extends BaseEntity {

    @Schema(description = "项目名称")
    @NotBlank(message = "项目名称不能为空")
    @Size(max = 100, message = "项目名称长度不能超过100个字符")
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Schema(description = "项目编号")
    @Size(max = 50, message = "项目编号长度不能超过50个字符")
    @Column(name = "code", length = 50, unique = true)
    private String code;

    @Schema(description = "项目描述")
    @Size(max = 1000, message = "项目描述长度不能超过1000个字符")
    @Column(name = "description", length = 1000)
    private String description;

    @Schema(description = "项目状态：DRAFT-草稿，IN_PROGRESS-进行中，COMPLETED-已完成，ARCHIVED-已归档")
    @NotNull(message = "项目状态不能为空")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ProjectStatus status = ProjectStatus.DRAFT;

    @Schema(description = "项目优先级：LOW-低，MEDIUM-中，HIGH-高，URGENT-紧急")
    @Enumerated(EnumType.STRING)
    @Column(name = "priority")
    private ProjectPriority priority = ProjectPriority.MEDIUM;

    @Schema(description = "项目负责人ID")
    @Column(name = "manager_id")
    private Long managerId;

    @Schema(description = "所属分类ID")
    @Column(name = "category_id")
    private Long categoryId;

    @Schema(description = "所属分类")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", insertable = false, updatable = false)
    private ProjectCategory category;

    @Schema(description = "项目负责人")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id", insertable = false, updatable = false)
    private User manager;

    @Schema(description = "计划开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "planned_start_time")
    private LocalDateTime plannedStartTime;

    @Schema(description = "计划结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "planned_end_time")
    private LocalDateTime plannedEndTime;

    @Schema(description = "实际开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "actual_start_time")
    private LocalDateTime actualStartTime;

    @Schema(description = "实际结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "actual_end_time")
    private LocalDateTime actualEndTime;

    @Schema(description = "项目预算")
    @Column(name = "budget")
    private Double budget;

    @Schema(description = "项目标签，多个标签用逗号分隔")
    @Size(max = 500, message = "项目标签长度不能超过500个字符")
    @Column(name = "tags", length = 500)
    private String tags;

    @Schema(description = "项目成员")
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ProjectMember> members;

    @Schema(description = "项目里程碑")
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ProjectMilestone> milestones;

    @Schema(description = "项目文档")
    @OneToMany(mappedBy = "projectId", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Document> documents;

    /**
     * 项目状态枚举
     */
    public enum ProjectStatus {
        DRAFT("草稿"),
        IN_PROGRESS("进行中"),
        COMPLETED("已完成"),
        ARCHIVED("已归档");

        private final String description;

        ProjectStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 项目优先级枚举
     */
    public enum ProjectPriority {
        LOW("低"),
        MEDIUM("中"),
        HIGH("高"),
        URGENT("紧急");

        private final String description;

        ProjectPriority(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
