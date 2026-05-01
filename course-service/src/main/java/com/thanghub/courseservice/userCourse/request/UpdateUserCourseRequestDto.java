package com.thanghub.courseservice.userCourse.request;

import com.thanghub.courseservice.userCourse.UserCourseStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserCourseRequestDto {
    @Schema(description = "Status", example = "IN_PROGRESS")
    private UserCourseStatusEnum status;

    @Schema(description = "Progress percentage", example = "50")
    private int progress;
}
