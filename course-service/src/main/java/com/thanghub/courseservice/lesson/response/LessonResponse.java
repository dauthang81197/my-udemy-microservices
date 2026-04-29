package com.thanghub.courseservice.lesson.response;

import com.thanghub.common.enums.CourseStatusEnum;
import com.thanghub.common.enums.LessonTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LessonResponse {
    private UUID id;
    private String title;
    private String description;
    private LessonTypeEnum type;
    private String videoUrl;
    private Boolean isPreview;
    private Integer sortOrder;
    private CourseStatusEnum status;
}