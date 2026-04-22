package com.thanghub.courseservice.common.response;


import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class PaginationResponse<T> {
    private List<T> data;

    private int page;
    private int size;

    private long totalItems;
    private int totalPages;

    private boolean hasNext;
    private boolean hasPrevious;

}
