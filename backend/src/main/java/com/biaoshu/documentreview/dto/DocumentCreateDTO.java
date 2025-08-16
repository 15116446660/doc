package com.biaoshu.documentreview.dto;

import com.biaoshu.documentreview.enums.DocumentType;
import com.biaoshu.documentreview.enums.SecurityLevel;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 文档创建DTO
 * 
 * @author biaoshu
 */
@Data
public class DocumentCreateDTO {

    /**
     * 文档标题
     */
    @NotBlank(message = "文档标题不能为空")
    @Size(max = 200, message = "文档标题长度不能超过200个字符")
    private String title;

    /**
     * 文档摘要
     */
    @Size(max = 1000, message = "文档摘要长度不能超过1000个字符")
    private String summary;

    /**
     * 文档类型
     */
    @NotNull(message = "文档类型不能为空")
    private DocumentType documentType;

    /**
     * 安全级别
     */
    private SecurityLevel securityLevel = SecurityLevel.PUBLIC;

    /**
     * 项目ID
     */
    private Long projectId;

    /**
     * 部门ID
     */
    private Long departmentId;

    /**
     * 关联业务编号
     */
    @Size(max = 100, message = "业务编号长度不能超过100个字符")
    private String businessNumber;

    /**
     * 文档标签
     */
    private String tags;

    /**
     * 文档内容
     */
    private String content;

    /**
     * 备注
     */
    @Size(max = 1000, message = "备注长度不能超过1000个字符")
    private String remark;
}
