package com.thanghub.courseservice.common.mapper;

import com.thanghub.courseservice.common.response.PaginationResponse;
import org.springframework.data.domain.Page;

public class PaginationMapper {

    public static <T> PaginationResponse<T> from(Page<T> pageData) {
        return PaginationResponse.<T>builder()
                .data(pageData.getContent())
                .page(pageData.getNumber() + 1)
                .size(pageData.getSize())
                .totalItems(pageData.getTotalElements())
                .totalPages(pageData.getTotalPages())
                .hasNext(pageData.hasNext())
                .hasPrevious(pageData.hasPrevious())
                .build();
    }
}