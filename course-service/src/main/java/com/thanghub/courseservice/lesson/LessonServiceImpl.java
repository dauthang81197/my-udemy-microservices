package com.thanghub.courseservice.lesson;

import com.thanghub.courseservice.lesson.request.CreateLessonRequestDto;
import com.thanghub.courseservice.lesson.request.UpdateLessonRequestDto;
import com.thanghub.courseservice.lesson.response.LessonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service()
@RequiredArgsConstructor
public class LessonServiceImpl implements LessonService {
    @Override
    public Page<LessonResponse> getLessons(Pageable pageable) {
        return null;
    }

    @Override
    public LessonResponse getLesson(String id) {
        return null;
    }

    @Override
    public Lesson createLesson(CreateLessonRequestDto request) {
        return null;
    }

    @Override
    public Lesson updateLesson(String id, UpdateLessonRequestDto request) {
        return null;
    }


    @Override
    public Lesson deleteLesson(String id) {
        return null;
    }
}
