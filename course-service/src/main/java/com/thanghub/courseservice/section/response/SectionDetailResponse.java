package com.thanghub.courseservice.section.response;

import com.thanghub.courseservice.lesson.response.LessonDetailResponse;
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
public class SectionDetailResponse {
    private UUID id;
    private String title;
    private List<LessonDetailResponse> lessons;
    private int totalLessons;
    private int completedLessons;
}