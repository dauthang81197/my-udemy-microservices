package com.thanghub.courseservice.lesson.request;

import com.thanghub.common.enums.LevelEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateLessonRequestDto {
    @Schema(
            description = "Title",
            example = "Title of course"
    )
    private String title;
    @Schema(
            description = "Description",
            example = "Description of course"
    )
    private String description;

    @Schema(
            description = "Level",
            example = "BEGINNER"
    )
    private LevelEnum level;

}

