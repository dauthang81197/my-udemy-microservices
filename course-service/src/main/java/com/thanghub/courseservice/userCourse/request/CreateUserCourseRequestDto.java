package com.thanghub.courseservice.userCourse.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateUserCourseRequestDto {
    @Schema(description = "User Id", example = "uuid-of-user")
    private UUID userId;

    @Schema(description = "Course Id", example = "uuid-of-course")
    private UUID courseId;
}
