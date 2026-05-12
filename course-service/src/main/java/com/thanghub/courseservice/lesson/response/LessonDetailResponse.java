package com.thanghub.courseservice.lesson.response;

import com.thanghub.common.enums.CourseStatusEnum;
import com.thanghub.common.enums.LessonTypeEnum;
import com.thanghub.courseservice.userLessonProgress.response.UserLessonProgressResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LessonDetailResponse {
    private UUID id;
    private String title;
    private String description;
    private LessonTypeEnum type;
    private UUID videoFileId;
    private String videoUrl;
    private Boolean isPreview;
    private Integer sortOrder;
    private CourseStatusEnum status;
    private boolean isStarted;
    private UserLessonProgressResponse progress;
}