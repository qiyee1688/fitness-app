package com.fitness.dto;

import lombok.Data;
import java.util.List;

/**
 * 分页响应 DTO
 */
@Data
public class PageResponse<T> {
    private List<T> items;
    private int page;
    private int pageSize;
    private int total;
    private int totalPages;

    public PageResponse(List<T> data, int page, int pageSize, int total) {
        this.items = data;
        this.page = page;
        this.pageSize = pageSize;
        this.total = total;
        this.totalPages = (int) Math.ceil((double) total / pageSize);
    }
}
