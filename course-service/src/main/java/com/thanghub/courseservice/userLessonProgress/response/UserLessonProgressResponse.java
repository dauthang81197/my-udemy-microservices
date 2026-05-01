package com.thanghub.courseservice.userLessonProgress.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserLessonProgressResponse {
    private UUID id;
    private UUID userId;
    private UUID lessonId;
    private int currentTime;
    private boolean isCompleted;
}
