package com.thanghub.courseservice.section.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SectionImportResultDto {
    private int totalRows;
    private int successCount;
    private int errorCount;
    private List<SectionResponse> imported;
    private List<RowError> errors;

    @Data
    @Builder
    public static class RowError {
        private int row;
        private String sectionName;
        private String reason;
    }
}
