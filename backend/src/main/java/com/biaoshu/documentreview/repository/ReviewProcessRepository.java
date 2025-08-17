package com.biaoshu.documentreview.repository;

import com.biaoshu.documentreview.entity.ReviewProcess;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewProcessRepository extends JpaRepository<ReviewProcess, Long> {
}
