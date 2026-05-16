package com.thanghub.courseservice.section.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateSectionRequestDto {
    @Schema(
            description = "Title",
            example = "Title of course"
    )
    private String title;
    @Schema(
            description = "Course Id",
            example = "CourseId of course"
    )
    private UUID courseId;
    @Schema(
            description = "Sort order",
            example = "1"
    )
    private int sort;
}
