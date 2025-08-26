package com.biaoshu.documentreview.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 响应状态码枚举
 * 
 * @author biaoshu
 */
@Getter
public enum ResultCode {

    // 通用状态码
    SUCCESS(200, "操作成功"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "禁止访问"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不允许"),
    INTERNAL_SERVER_ERROR(500, "服务器内部错误"),

    // 用户相关状态码 (1000-1999)
    USER_NOT_FOUND(1001, "用户不存在"),
    USER_ALREADY_EXISTS(1002, "用户已存在"),
    USER_DISABLED(1003, "用户已被禁用"),
    INVALID_CREDENTIALS(1004, "用户名或密码错误"),
    PASSWORD_NOT_MATCH(1005, "密码不匹配"),
    USER_NOT_ACTIVE(1006, "用户未激活"),

    // 认证相关状态码 (2000-2999)
    TOKEN_INVALID(2001, "Token无效"),
    TOKEN_EXPIRED(2002, "Token已过期"),
    TOKEN_MISSING(2003, "Token缺失"),
    REFRESH_TOKEN_INVALID(2004, "刷新Token无效"),
    PERMISSION_DENIED(2005, "权限不足"),

    // 文档相关状态码 (3000-3999)
    DOCUMENT_NOT_FOUND(3001, "文档不存在"),
    DOCUMENT_LOCKED(3002, "文档已被锁定"),
    DOCUMENT_VERSION_CONFLICT(3003, "文档版本冲突"),
    DOCUMENT_UPLOAD_FAILED(3004, "文档上传失败"),
    DOCUMENT_FORMAT_NOT_SUPPORTED(3005, "不支持的文档格式"),
    DOCUMENT_SIZE_EXCEEDED(3006, "文档大小超出限制"),
    DOCUMENT_ACCESS_DENIED(3007, "文档访问权限不足"),

    // 审核流程相关状态码 (4000-4999)
    REVIEW_PROCESS_NOT_FOUND(4001, "审核流程不存在"),
    REVIEW_PROCESS_ALREADY_STARTED(4002, "审核流程已启动"),
    REVIEW_PROCESS_COMPLETED(4003, "审核流程已完成"),
    REVIEW_STEP_NOT_FOUND(4004, "审核步骤不存在"),
    REVIEW_PERMISSION_DENIED(4005, "审核权限不足"),
    REVIEW_TIMEOUT(4006, "审核超时"),
    REVIEW_CONFLICT(4007, "审核冲突"),
    REVIEWER_NOT_AVAILABLE(4008, "审核人员不可用"),
    REVIEW_TEMPLATE_NOT_FOUND(4009, "审核模板不存在"),
    REVIEW_ALREADY_COMPLETED(4010, "审核已完成"),

    // 工作流相关状态码 (5000-5999)
    WORKFLOW_NOT_FOUND(5001, "工作流不存在"),
    WORKFLOW_DEFINITION_ERROR(5002, "工作流定义错误"),
    WORKFLOW_EXECUTION_ERROR(5003, "工作流执行错误"),
    WORKFLOW_TASK_NOT_FOUND(5004, "工作流任务不存在"),
    WORKFLOW_TASK_ALREADY_COMPLETED(5005, "工作流任务已完成"),

    // AI相关状态码 (6000-6999)
    AI_SERVICE_UNAVAILABLE(6001, "AI服务不可用"),
    AI_ANALYSIS_FAILED(6002, "AI分析失败"),
    AI_TOKEN_LIMIT_EXCEEDED(6003, "AI Token限制超出"),
    AI_MODEL_NOT_FOUND(6004, "AI模型不存在"),

    // 文件相关状态码 (7000-7999)
    FILE_NOT_FOUND(7001, "文件不存在"),
    FILE_UPLOAD_FAILED(7002, "文件上传失败"),
    FILE_DELETE_FAILED(7003, "文件删除失败"),
    FILE_TYPE_NOT_ALLOWED(7004, "文件类型不允许"),
    FILE_SIZE_EXCEEDED(7005, "文件大小超出限制"),

    // 通知相关状态码 (8000-8999)
    NOTIFICATION_SEND_FAILED(8001, "通知发送失败"),
    EMAIL_SEND_FAILED(8002, "邮件发送失败"),
    SMS_SEND_FAILED(8003, "短信发送失败"),

    // 系统配置相关状态码 (9000-9999)
    CONFIG_NOT_FOUND(9001, "配置不存在"),
    CONFIG_UPDATE_FAILED(9002, "配置更新失败"),
    SYSTEM_MAINTENANCE(9003, "系统维护中");

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
