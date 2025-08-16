package com.biaoshu.documentreview.common;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 分页响应结果类
 * 
 * @author biaoshu
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PageResult<T> extends Result<List<T>> {

    /**
     * 当前页码
     */
    private Integer pageNum;

    /**
     * 每页大小
     */
    private Integer pageSize;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 总页数
     */
    private Integer pages;

    /**
     * 是否有下一页
     */
    private Boolean hasNext;

    /**
     * 是否有上一页
     */
    private Boolean hasPrevious;

    public PageResult() {
        super();
    }

    public PageResult(Integer pageNum, Integer pageSize, Long total, List<T> data) {
        super(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), data);
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.total = total;
        this.pages = (int) Math.ceil((double) total / pageSize);
        this.hasNext = pageNum < pages;
        this.hasPrevious = pageNum > 1;
    }

    /**
     * 成功分页响应
     */
    public static <T> PageResult<T> success(Integer pageNum, Integer pageSize, Long total, List<T> data) {
        return new PageResult<>(pageNum, pageSize, total, data);
    }

    /**
     * 空分页响应
     */
    public static <T> PageResult<T> empty(Integer pageNum, Integer pageSize) {
        return new PageResult<>(pageNum, pageSize, 0L, List.of());
    }
}
