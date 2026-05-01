package com.thanghub.courseservice.userCourse.response;

import com.thanghub.courseservice.userCourse.UserCourseStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserCourseResponse {
    private UUID id;
    private UUID userId;
    private UUID courseId;
    private UserCourseStatusEnum status;
    private int progress;
}
