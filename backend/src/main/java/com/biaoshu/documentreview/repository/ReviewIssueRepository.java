package com.biaoshu.documentreview.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.biaoshu.documentreview.entity.ReviewIssue;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 评审问题Repository
 *
 * @author Jules
 * @since 2025-08-26
 */
@Repository
@Mapper
public interface ReviewIssueRepository extends BaseMapper<ReviewIssue> {

    /**
     * 根据评审任务ID查找所有问题
     * @param reviewTaskId 评审任务ID
     * @return 问题列表
     */
    @Select("SELECT * FROM review_issues WHERE review_task_id = #{reviewTaskId} ORDER BY created_at DESC")
    List<ReviewIssue> findByReviewTaskId(@Param("reviewTaskId") Long reviewTaskId);

}
