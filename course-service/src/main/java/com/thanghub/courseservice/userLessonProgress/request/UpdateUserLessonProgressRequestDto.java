package com.thanghub.courseservice.userLessonProgress.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserLessonProgressRequestDto {
    @Schema(description = "Current playback time in seconds", example = "120")
    private int currentTime;

    @Schema(description = "Whether the lesson is completed", example = "false")
    private boolean isCompleted;
}
