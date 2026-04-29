package com.thanghub.courseservice.lesson.request;

import com.thanghub.common.enums.LessonTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateLessonRequestDto {
    @Schema(description = "Section Id", example = "uuid-of-section")
    private UUID sectionId;

    @Schema(description = "Title", example = "Introduction to Java")
    private String title;

    @Schema(description = "Description", example = "Overview of the lesson")
    private String description;

    @Schema(description = "Type", example = "VIDEO")
    private LessonTypeEnum type;

    @Schema(description = "Video URL", example = "https://example.com/video.mp4")
    private String videoUrl;

    @Schema(description = "Is preview", example = "false")
    private Boolean isPreview;

    @Schema(description = "Sort order", example = "1")
    private Integer sortOrder;
}