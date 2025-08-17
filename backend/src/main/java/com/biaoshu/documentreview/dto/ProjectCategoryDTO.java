package com.biaoshu.documentreview.dto;

import com.biaoshu.documentreview.entity.ProjectCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "项目分类数据传输对象")
@Data
public class ProjectCategoryDTO {

    @Schema(description = "创建请求")
    @Data
    public static class Create {
        @Schema(description = "分类名称", required = true)
        @NotBlank(message = "分类名称不能为空")
        private String name;

        @Schema(description = "分类类型", required = true)
        @NotNull(message = "分类类型不能为空")
        private ProjectCategory.CategoryType type;

        @Schema(description = "父分类ID，顶级分类可不传")
        private Long parentId;

        @Schema(description = "排序值")
        private Integer sortOrder = 0;
    }

    @Schema(description = "更新请求")
    @Data
    public static class Update {
        @Schema(description = "分类名称")
        private String name;

        @Schema(description = "父分类ID")
        private Long parentId;

        @Schema(description = "排序值")
        private Integer sortOrder;
    }

    @Schema(description = "分类树节点")
    @Data
    public static class TreeNode {
        @Schema(description = "ID")
        private Long id;

        @Schema(description = "名称")
        private String name;

        @Schema(description = "类型")
        private ProjectCategory.CategoryType type;

        @Schema(description = "父ID")
        private Long parentId;

        @Schema(description = "排序值")
        private Integer sortOrder;

        @Schema(description = "创建时间")
        private LocalDateTime createTime;

        @Schema(description = "子节点")
        private List<TreeNode> children;

        @Schema(description = "是否有子节点")
        private boolean hasChildren;
    }
}
