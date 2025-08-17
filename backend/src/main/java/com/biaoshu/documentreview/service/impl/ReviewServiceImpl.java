package com.biaoshu.documentreview.service.impl;

import com.biaoshu.documentreview.entity.*;
import com.biaoshu.documentreview.enums.DocumentStatus;
import com.biaoshu.documentreview.enums.ReviewProcessStatus;
import com.biaoshu.documentreview.enums.ReviewStepStatus;
import com.biaoshu.documentreview.exception.BusinessException;
import com.biaoshu.documentreview.repository.*;
import com.biaoshu.documentreview.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final DocumentRepository documentRepository;
    private final ReviewTemplateRepository templateRepository;
    private final ReviewProcessRepository processRepository;
    private final ReviewStepRepository stepRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ReviewProcess submitForReview(Long documentId, Long templateId, Long initiatorId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new BusinessException("文档不存在"));
        ReviewTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new BusinessException("审核模板不存在"));
        User initiator = userRepository.findById(initiatorId)
                .orElseThrow(() -> new BusinessException("发起人不存在"));

        // 1. 创建审核流程实例
        ReviewProcess process = new ReviewProcess();
        process.setProcessNumber("RP-" + System.currentTimeMillis());
        process.setDocument(document);
        process.setReviewTemplate(template);
        process.setInitiator(initiator);
        process.setStatus(ReviewProcessStatus.PROCESSING);
        process.setStartedAt(LocalDateTime.now());

        ReviewProcess savedProcess = processRepository.save(process);

        // 2. 根据模板创建审核步骤
        List<ReviewStep> steps = new ArrayList<>();
        for (ReviewTemplateStep templateStep : template.getSteps()) {
            ReviewStep step = new ReviewStep();
            step.setReviewProcess(savedProcess);
            step.setStepName(templateStep.getStepName());
            step.setStepOrder(templateStep.getStepOrder());
            // TODO: Implement actual assignee logic based on template
            step.setAssigneeId(1L); // Placeholder assignee
            step.setStatus(ReviewStepStatus.PENDING);
            steps.add(step);
        }

        // Mark the first step as active
        if (!steps.isEmpty()) {
            steps.get(0).setStatus(ReviewStepStatus.ACTIVE);
        }

        stepRepository.saveAll(steps);
        savedProcess.setReviewSteps(steps);

        // 3. 更新文档状态
        document.setStatus(DocumentStatus.IN_REVIEW);
        documentRepository.save(document);

        return savedProcess;
    }

    @Override
    @Transactional
    public void approveStep(Long stepId, Long reviewerId, String comment) {
        ReviewStep currentStep = stepRepository.findById(stepId)
                .orElseThrow(() -> new BusinessException("审核步骤不存在"));

        if (currentStep.getStatus() != ReviewStepStatus.ACTIVE) {
            throw new BusinessException("步骤不是活动状态，无法批准");
        }

        // 1. 更新当前步骤状态
        currentStep.setStatus(ReviewStepStatus.COMPLETED);
        currentStep.setResult("APPROVED");
        currentStep.setComment(comment);
        currentStep.setReviewerId(reviewerId);
        currentStep.setCompletedAt(LocalDateTime.now());
        stepRepository.save(currentStep);

        ReviewProcess process = currentStep.getReviewProcess();

        // 2. 查找并激活下一步
        ReviewStep nextStep = stepRepository.findFirstByReviewProcessAndStepOrderGreaterThanOrderByStepOrderAsc(process, currentStep.getStepOrder())
            .orElse(null);

        if (nextStep != null) {
            nextStep.setStatus(ReviewStepStatus.ACTIVE);
            stepRepository.save(nextStep);
        } else {
            // 3. 如果没有下一步，则流程结束
            process.setStatus(ReviewProcessStatus.COMPLETED);
            process.setResult("APPROVED");
            process.setCompletedAt(LocalDateTime.now());
            processRepository.save(process);

            // 4. 更新文档状态
            Document document = process.getDocument();
            document.setStatus(DocumentStatus.PUBLISHED);
            documentRepository.save(document);
        }
    }

    @Override
    @Transactional
    public void rejectStep(Long stepId, Long reviewerId, String comment) {
        ReviewStep currentStep = stepRepository.findById(stepId)
                .orElseThrow(() -> new BusinessException("审核步骤不存在"));

        if (currentStep.getStatus() != ReviewStepStatus.ACTIVE) {
            throw new BusinessException("步骤不是活动状态，无法驳回");
        }

        // 1. 更新当前步骤状态
        currentStep.setStatus(ReviewStepStatus.COMPLETED);
        currentStep.setResult("REJECTED");
        currentStep.setComment(comment);
        currentStep.setReviewerId(reviewerId);
        currentStep.setCompletedAt(LocalDateTime.now());
        stepRepository.save(currentStep);

        // 2. 流程结束
        ReviewProcess process = currentStep.getReviewProcess();
        process.setStatus(ReviewProcessStatus.REJECTED);
        process.setResult("REJECTED");
        process.setResultReason(comment);
        process.setCompletedAt(LocalDateTime.now());
        processRepository.save(process);

        // 3. 更新文档状态
        Document document = process.getDocument();
        document.setStatus(DocumentStatus.DRAFT); // Or a specific 'REJECTED' status
        documentRepository.save(document);
    }
}
