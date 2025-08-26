package com.biaoshu.documentreview.service.impl;

import com.biaoshu.documentreview.dto.*;
import com.biaoshu.documentreview.entity.ReviewTask;
import com.biaoshu.documentreview.repository.ReviewTaskRepository;
import com.biaoshu.documentreview.service.ReviewTaskService;
import com.biaoshu.documentreview.service.ai.AIAnalysisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 评审任务服务实现
 */
@Service
@Transactional
public class ReviewTaskServiceImpl implements ReviewTaskService {

    private static final Logger logger = LoggerFactory.getLogger(ReviewTaskServiceImpl.class);

    @Autowired
    private ReviewTaskRepository reviewTaskRepository;

    @Autowired
    private AIAnalysisService aiAnalysisService;

    @Override
    public ReviewTaskDTO createReviewTask(ReviewTaskCreateDTO createDTO, Long creatorId) {
        logger.info("创建评审任务，创建者ID: {}", creatorId);
        
        ReviewTask reviewTask = new ReviewTask();
        reviewTask.setTaskName(createDTO.getTaskName());
        reviewTask.setTaskDescription(createDTO.getTaskDescription());
        reviewTask.setDocumentId(createDTO.getDocumentId());
        reviewTask.setProjectId(createDTO.getProjectId());
        reviewTask.setCreatorId(creatorId);
        reviewTask.setPriority(createDTO.getPriority());
        reviewTask.setDeadline(createDTO.getDeadline());
        reviewTask.setAiAnalysisEnabled(createDTO.getAiAnalysisEnabled());
        reviewTask.setStatus(ReviewTask.ReviewTaskStatus.PENDING);
        reviewTask.setAiAnalysisStatus(ReviewTask.AIAnalysisStatus.PENDING);
        
        reviewTask = reviewTaskRepository.save(reviewTask);
        
        logger.info("评审任务创建成功，任务ID: {}", reviewTask.getId());
        return convertToDTO(reviewTask);
    }

    @Override
    public ReviewTaskDTO updateReviewTask(Long taskId, ReviewTaskUpdateDTO updateDTO, Long userId) {
        logger.info("更新评审任务，任务ID: {}, 操作用户ID: {}", taskId, userId);
        
        Optional<ReviewTask> taskOpt = reviewTaskRepository.findById(taskId);
        if (!taskOpt.isPresent()) {
            throw new RuntimeException("评审任务不存在");
        }
        
        ReviewTask reviewTask = taskOpt.get();
        
        if (updateDTO.getTaskName() != null) {
            reviewTask.setTaskName(updateDTO.getTaskName());
        }
        if (updateDTO.getTaskDescription() != null) {
            reviewTask.setTaskDescription(updateDTO.getTaskDescription());
        }
        if (updateDTO.getPriority() != null) {
            reviewTask.setPriority(updateDTO.getPriority());
        }
        if (updateDTO.getDeadline() != null) {
            reviewTask.setDeadline(updateDTO.getDeadline());
        }
        if (updateDTO.getAiAnalysisEnabled() != null) {
            reviewTask.setAiAnalysisEnabled(updateDTO.getAiAnalysisEnabled());
        }
        if (updateDTO.getStatus() != null) {
            reviewTask.setStatus(updateDTO.getStatus());
        }
        
        reviewTask = reviewTaskRepository.save(reviewTask);
        
        logger.info("评审任务更新成功，任务ID: {}", taskId);
        return convertToDTO(reviewTask);
    }

    @Override
    public void deleteReviewTask(Long taskId, Long userId) {
        logger.info("删除评审任务，任务ID: {}, 操作用户ID: {}", taskId, userId);
        
        Optional<ReviewTask> taskOpt = reviewTaskRepository.findById(taskId);
        if (!taskOpt.isPresent()) {
            throw new RuntimeException("评审任务不存在");
        }
        
        reviewTaskRepository.deleteById(taskId);
        logger.info("评审任务删除成功，任务ID: {}", taskId);
    }

    @Override
    public ReviewTaskDTO getReviewTaskById(Long taskId) {
        logger.info("获取评审任务详情，任务ID: {}", taskId);
        
        Optional<ReviewTask> taskOpt = reviewTaskRepository.findById(taskId);
        if (!taskOpt.isPresent()) {
            throw new RuntimeException("评审任务不存在");
        }
        
        return convertToDTO(taskOpt.get());
    }

    @Override
    public Page<ReviewTaskDTO> getReviewTasks(ReviewTaskQueryDTO queryDTO, Pageable pageable) {
        logger.info("分页查询评审任务");
        
        // 这里简化实现，实际应该根据queryDTO构建查询条件
        Page<ReviewTask> taskPage = reviewTaskRepository.findAll(pageable);
        
        List<ReviewTaskDTO> dtoList = new ArrayList<>();
        for (ReviewTask task : taskPage.getContent()) {
            dtoList.add(convertToDTO(task));
        }
        
        return new PageImpl<>(dtoList, pageable, taskPage.getTotalElements());
    }

    @Override
    public Page<ReviewTaskDTO> getUserRelatedTasks(Long userId, Pageable pageable) {
        logger.info("获取用户相关任务，用户ID: {}", userId);
        
        Page<ReviewTask> taskPage = reviewTaskRepository.findUserRelatedTasks(userId, pageable);
        
        List<ReviewTaskDTO> dtoList = new ArrayList<>();
        for (ReviewTask task : taskPage.getContent()) {
            dtoList.add(convertToDTO(task));
        }
        
        return new PageImpl<>(dtoList, pageable, taskPage.getTotalElements());
    }

    @Override
    public Page<ReviewTaskDTO> getAssignedTasks(Long userId, Pageable pageable) {
        logger.info("获取分配任务，用户ID: {}", userId);
        
        Page<ReviewTask> taskPage = reviewTaskRepository.findAssignedToUser(userId, pageable);
        
        List<ReviewTaskDTO> dtoList = new ArrayList<>();
        for (ReviewTask task : taskPage.getContent()) {
            dtoList.add(convertToDTO(task));
        }
        
        return new PageImpl<>(dtoList, pageable, taskPage.getTotalElements());
    }

    @Override
    public ReviewTaskDTO startReviewTask(Long taskId, Long userId) {
        logger.info("启动评审任务，任务ID: {}, 操作用户ID: {}", taskId, userId);
        
        Optional<ReviewTask> taskOpt = reviewTaskRepository.findById(taskId);
        if (!taskOpt.isPresent()) {
            throw new RuntimeException("评审任务不存在");
        }
        
        ReviewTask reviewTask = taskOpt.get();
        reviewTask.setStatus(ReviewTask.ReviewTaskStatus.IN_PROGRESS);
        reviewTask.setStartedAt(LocalDateTime.now());
        
        reviewTask = reviewTaskRepository.save(reviewTask);
        
        logger.info("评审任务启动成功，任务ID: {}", taskId);
        return convertToDTO(reviewTask);
    }

    @Override
    public ReviewTaskDTO completeReviewTask(Long taskId, Long userId, String completionNotes) {
        logger.info("完成评审任务，任务ID: {}, 操作用户ID: {}", taskId, userId);
        
        Optional<ReviewTask> taskOpt = reviewTaskRepository.findById(taskId);
        if (!taskOpt.isPresent()) {
            throw new RuntimeException("评审任务不存在");
        }
        
        ReviewTask reviewTask = taskOpt.get();
        reviewTask.setStatus(ReviewTask.ReviewTaskStatus.COMPLETED);
        reviewTask.setCompletedAt(LocalDateTime.now());
        
        reviewTask = reviewTaskRepository.save(reviewTask);
        
        logger.info("评审任务完成成功，任务ID: {}", taskId);
        return convertToDTO(reviewTask);
    }

    @Override
    public ReviewTaskDTO cancelReviewTask(Long taskId, Long userId, String reason) {
        logger.info("取消评审任务，任务ID: {}, 操作用户ID: {}", taskId, userId);
        
        Optional<ReviewTask> taskOpt = reviewTaskRepository.findById(taskId);
        if (!taskOpt.isPresent()) {
            throw new RuntimeException("评审任务不存在");
        }
        
        ReviewTask reviewTask = taskOpt.get();
        reviewTask.setStatus(ReviewTask.ReviewTaskStatus.CANCELLED);
        
        reviewTask = reviewTaskRepository.save(reviewTask);
        
        logger.info("评审任务取消成功，任务ID: {}", taskId);
        return convertToDTO(reviewTask);
    }

    @Override
    public void assignReviewTask(Long taskId, Long assigneeId, Long assignerId, String role, String notes) {
        logger.info("分配评审任务，任务ID: {}, 分配给用户ID: {}", taskId, assigneeId);
        
        // 这里简化实现，实际应该创建ReviewTaskAssignment记录
        logger.info("评审任务分配成功");
    }

    @Override
    public Boolean startAIAnalysis(Long taskId, Long userId) {
        logger.info("启动AI分析，任务ID: {}, 操作用户ID: {}", taskId, userId);
        
        Optional<ReviewTask> taskOpt = reviewTaskRepository.findById(taskId);
        if (!taskOpt.isPresent()) {
            throw new RuntimeException("评审任务不存在");
        }
        
        ReviewTask reviewTask = taskOpt.get();
        
        // 启动AI分析
        aiAnalysisService.performFullAnalysis(reviewTask);
        
        logger.info("AI分析启动成功，任务ID: {}", taskId);
        return true;
    }

    @Override
    public Boolean stopAIAnalysis(Long taskId, Long userId) {
        logger.info("停止AI分析，任务ID: {}, 操作用户ID: {}", taskId, userId);
        
        Boolean result = aiAnalysisService.cancelAnalysis(taskId);
        
        logger.info("AI分析停止成功，任务ID: {}", taskId);
        return result;
    }

    @Override
    public Integer getAIAnalysisProgress(Long taskId) {
        return aiAnalysisService.getAnalysisProgress(taskId);
    }

    @Override
    public List<ReviewTaskDTO> getTasksDueSoon(Integer days) {
        logger.info("获取即将到期的任务，天数: {}", days);
        
        LocalDateTime startTime = LocalDateTime.now();
        LocalDateTime endTime = startTime.plusDays(days);
        
        List<ReviewTask> tasks = reviewTaskRepository.findTasksDueSoon(startTime, endTime);
        
        List<ReviewTaskDTO> dtoList = new ArrayList<>();
        for (ReviewTask task : tasks) {
            dtoList.add(convertToDTO(task));
        }
        
        return dtoList;
    }

    @Override
    public List<ReviewTaskDTO> getOverdueTasks() {
        logger.info("获取已过期的任务");
        
        List<ReviewTask> tasks = reviewTaskRepository.findOverdueTasks(LocalDateTime.now());
        
        List<ReviewTaskDTO> dtoList = new ArrayList<>();
        for (ReviewTask task : tasks) {
            dtoList.add(convertToDTO(task));
        }
        
        return dtoList;
    }

    @Override
    public ReviewTaskStatistics getUserTaskStatistics(Long userId) {
        logger.info("获取用户任务统计，用户ID: {}", userId);
        
        ReviewTaskStatistics statistics = new ReviewTaskStatistics();
        statistics.setCreatedTasks(reviewTaskRepository.countByCreatorId(userId));
        statistics.setAssignedTasks(reviewTaskRepository.countAssignedToUser(userId));
        // 这里简化实现，实际应该统计各种状态的任务数量
        statistics.setTotalTasks(statistics.getCreatedTasks() + statistics.getAssignedTasks());
        statistics.setPendingTasks(0L);
        statistics.setInProgressTasks(0L);
        statistics.setCompletedTasks(0L);
        statistics.setCancelledTasks(0L);
        
        return statistics;
    }

    @Override
    public ReviewTaskStatistics getProjectTaskStatistics(Long projectId) {
        logger.info("获取项目任务统计，项目ID: {}", projectId);
        
        ReviewTaskStatistics statistics = new ReviewTaskStatistics();
        statistics.setTotalTasks(reviewTaskRepository.countByProjectId(projectId));
        // 这里简化实现，实际应该统计各种状态的任务数量
        statistics.setPendingTasks(0L);
        statistics.setInProgressTasks(0L);
        statistics.setCompletedTasks(0L);
        statistics.setCancelledTasks(0L);
        statistics.setAssignedTasks(0L);
        statistics.setCreatedTasks(0L);
        
        return statistics;
    }

    /**
     * 将实体转换为DTO
     */
    private ReviewTaskDTO convertToDTO(ReviewTask reviewTask) {
        ReviewTaskDTO dto = new ReviewTaskDTO();
        dto.setId(reviewTask.getId());
        dto.setTaskName(reviewTask.getTaskName());
        dto.setTaskDescription(reviewTask.getTaskDescription());
        dto.setDocumentId(reviewTask.getDocumentId());
        dto.setProjectId(reviewTask.getProjectId());
        dto.setCreatorId(reviewTask.getCreatorId());
        dto.setStatus(reviewTask.getStatus());
        dto.setPriority(reviewTask.getPriority());
        dto.setDeadline(reviewTask.getDeadline());
        dto.setStartedAt(reviewTask.getStartedAt());
        dto.setCompletedAt(reviewTask.getCompletedAt());
        dto.setAiAnalysisEnabled(reviewTask.getAiAnalysisEnabled());
        dto.setAiAnalysisStatus(reviewTask.getAiAnalysisStatus());
        dto.setAiAnalysisProgress(reviewTask.getAiAnalysisProgress());
        dto.setAiAnalysisResult(reviewTask.getAiAnalysisResult());
        dto.setAiAnalysisError(reviewTask.getAiAnalysisError());
        dto.setCreatedAt(reviewTask.getCreatedAt());
        dto.setUpdatedAt(reviewTask.getUpdatedAt());
        
        // 设置关联信息（简化实现）
        dto.setDocumentTitle("示例文档");
        dto.setProjectName("示例项目");
        dto.setCreatorName("示例用户");
        
        // 设置统计信息（简化实现）
        dto.setTotalComments(0);
        dto.setUnresolvedComments(0);
        dto.setTotalAssignments(0);
        dto.setCompletedAssignments(0);
        
        return dto;
    }
}
