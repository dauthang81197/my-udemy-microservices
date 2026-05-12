package com.thanghub.courseservice.course.response;

import com.thanghub.common.enums.CourseStatusEnum;
import com.thanghub.common.enums.LevelEnum;
import com.thanghub.courseservice.section.response.SectionDetailResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CourseDetailResponse {
    private UUID id;
    private String title;
    private String description;
    private LevelEnum level;
    private CourseStatusEnum status;
    private List<SectionDetailResponse> sections;
    private int totalLessons;
    private int completedLessons;
    private UUID currentLessonId;
}