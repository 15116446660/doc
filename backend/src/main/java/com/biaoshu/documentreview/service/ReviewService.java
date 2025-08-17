package com.biaoshu.documentreview.service;

import com.biaoshu.documentreview.entity.ReviewProcess;

/**
 * 审核服务接口
 */
public interface ReviewService {

    /**
     * 提交文档进行审核
     * @param documentId 要审核的文档ID
     * @param templateId 使用的审核模板ID
     * @param initiatorId 发起人ID
     * @return 创建的审核流程实例
     */
    ReviewProcess submitForReview(Long documentId, Long templateId, Long initiatorId);

    /**
     * 批准一个审核步骤
     * @param stepId 审核步骤ID
     * @param reviewerId 审核人ID
     * @param comment 审核意见
     */
    void approveStep(Long stepId, Long reviewerId, String comment);

    /**
     * 驳回一个审核步骤
     * @param stepId 审核步骤ID
     * @param reviewerId 审核人ID
     * @param comment 驳回理由
     */
    void rejectStep(Long stepId, Long reviewerId, String comment);

}
