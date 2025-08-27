package com.biaoshu.documentreview.service;

import com.biaoshu.documentreview.dto.ReviewIssueCreateDTO;
import com.biaoshu.documentreview.dto.ReviewIssueDTO;
import com.biaoshu.documentreview.dto.ReviewIssueUpdateDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 评审问题服务接口
 *
 * @author Jules
 * @since 2025-08-26
 */
public interface ReviewIssueService {

    /**
     * 创建评审问题
     * @param createDTO 创建DTO
     * @param reporterId 报告人ID
     * @return 评审问题DTO
     */
    ReviewIssueDTO createIssue(ReviewIssueCreateDTO createDTO, Long reporterId);

    /**
     * 更新评审问题
     * @param issueId 问题ID
     * @param updateDTO 更新DTO
     * @param userId 操作用户ID
     * @return 评审问题DTO
     */
    ReviewIssueDTO updateIssue(Long issueId, ReviewIssueUpdateDTO updateDTO, Long userId);

    /**
     * 删除评审问题
     * @param issueId 问题ID
     * @param userId 操作用户ID
     */
    void deleteIssue(Long issueId, Long userId);

    /**
     * 根据ID获取评审问题
     * @param issueId 问题ID
     * @return 评审问题DTO
     */
    ReviewIssueDTO getIssueById(Long issueId);

    /**
     * 根据评审任务ID获取问题列表
     * @param taskId 任务ID
     * @return 问题列表
     */
    List<ReviewIssueDTO> getIssuesByTaskId(Long taskId);

    /**
     * 分页查询指定任务的问题
     * @param taskId 任务ID
     *_@param pageable 分页参数
     * @return 分页结果
     */
    Page<ReviewIssueDTO> getIssuesByTaskId(Long taskId, Pageable pageable);

}
