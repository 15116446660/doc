package com.biaoshu.documentreview.repository;

import com.biaoshu.documentreview.entity.ReviewProcess;
import com.biaoshu.documentreview.entity.ReviewStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewStepRepository extends JpaRepository<ReviewStep, Long> {

    /**
     * Finds the next step in a review process.
     * @param process The review process instance.
     * @param currentStepOrder The order of the current step.
     * @return An Optional containing the next ReviewStep, or empty if it's the last step.
     */
    Optional<ReviewStep> findFirstByReviewProcessAndStepOrderGreaterThanOrderByStepOrderAsc(ReviewProcess process, Integer currentStepOrder);
}
