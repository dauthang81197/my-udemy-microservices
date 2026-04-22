package com.thanghub.courseservice.course.response;

import com.thanghub.courseservice.common.enums.CourseStatusEnum;
import com.thanghub.courseservice.common.enums.LevelEnum;
import lombok.Builder;

import java.util.UUID;

@Builder
public class CourseResponse {
    private UUID id;
    private String title;
    private String description;
    private LevelEnum level;
    private CourseStatusEnum status;
}
