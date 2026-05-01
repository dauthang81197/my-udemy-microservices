package com.thanghub.courseservice.userLessonProgress.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateUserLessonProgressRequestDto {
    @Schema(description = "User Id", example = "uuid-of-user")
    private UUID userId;

    @Schema(description = "Lesson Id", example = "uuid-of-lesson")
    private UUID lessonId;
}
