package com.biaoshu.documentreview.service.impl;

import com.biaoshu.documentreview.dto.ReviewIssueCreateDTO;
import com.biaoshu.documentreview.dto.ReviewIssueDTO;
import com.biaoshu.documentreview.dto.ReviewIssueUpdateDTO;
import com.biaoshu.documentreview.entity.ReviewIssue;
import com.biaoshu.documentreview.repository.ReviewIssueRepository;
import com.biaoshu.documentreview.service.ReviewIssueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 评审问题服务实现
 *
 * @author Jules
 * @since 2025-08-26
 */
@Service
public class ReviewIssueServiceImpl implements ReviewIssueService {

    private final ReviewIssueRepository reviewIssueRepository;

    @Autowired
    public ReviewIssueServiceImpl(ReviewIssueRepository reviewIssueRepository) {
        this.reviewIssueRepository = reviewIssueRepository;
    }

    @Override
    public ReviewIssueDTO createIssue(ReviewIssueCreateDTO createDTO, Long reporterId) {
        // Basic implementation stub
        ReviewIssue issue = new ReviewIssue();
        // Here you would map from DTO to entity
        // issue.setIssueTitle(createDTO.getTitle());
        // ...
        issue.setReporterId(reporterId);
        reviewIssueRepository.insert(issue);
        // Here you would map from entity to DTO
        return new ReviewIssueDTO();
    }

    @Override
    public ReviewIssueDTO updateIssue(Long issueId, ReviewIssueUpdateDTO updateDTO, Long userId) {
        // Basic implementation stub
        ReviewIssue issue = reviewIssueRepository.selectById(issueId);
        if (issue == null) {
            // throw exception
            return null;
        }
        // ... map fields from updateDTO
        reviewIssueRepository.updateById(issue);
        return new ReviewIssueDTO();
    }

    @Override
    public void deleteIssue(Long issueId, Long userId) {
        // Add logic to check permissions before deleting
        reviewIssueRepository.deleteById(issueId);
    }

    @Override
    public ReviewIssueDTO getIssueById(Long issueId) {
        ReviewIssue issue = reviewIssueRepository.selectById(issueId);
        if (issue == null) {
            return null;
        }
        // map to DTO
        return new ReviewIssueDTO();
    }

    @Override
    public List<ReviewIssueDTO> getIssuesByTaskId(Long taskId) {
        List<ReviewIssue> issues = reviewIssueRepository.findByReviewTaskId(taskId);
        // map to DTO list
        return issues.stream().map(issue -> new ReviewIssueDTO()).collect(Collectors.toList());
    }

    @Override
    public Page<ReviewIssueDTO> getIssuesByTaskId(Long taskId, Pageable pageable) {
        // MyBatis-Plus pagination needs a Page object, not Spring Data's Pageable directly.
        // This is a more complex implementation detail to be handled later.
        // For now, returning an empty page.
        List<ReviewIssueDTO> dtoList = getIssuesByTaskId(taskId);
        return new PageImpl<>(dtoList, pageable, dtoList.size());
    }
}
