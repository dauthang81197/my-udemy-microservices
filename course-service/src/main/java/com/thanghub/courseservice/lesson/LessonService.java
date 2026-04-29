package com.thanghub.courseservice.lesson;

import com.thanghub.courseservice.lesson.request.CreateLessonRequestDto;
import com.thanghub.courseservice.lesson.request.UpdateLessonRequestDto;
import com.thanghub.courseservice.lesson.response.LessonResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LessonService {
    Page<LessonResponse> getLessons(Pageable pageable);

    LessonResponse getLesson(String id);

    Lesson createLesson(CreateLessonRequestDto request);

    Lesson updateLesson(String id, UpdateLessonRequestDto request);

    Lesson deleteLesson(String id);
}
