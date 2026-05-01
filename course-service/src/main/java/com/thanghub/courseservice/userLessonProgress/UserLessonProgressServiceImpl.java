package com.thanghub.courseservice.userLessonProgress;

import com.thanghub.courseservice.lesson.Lesson;
import com.thanghub.courseservice.lesson.LessonRepository;
import com.thanghub.courseservice.userLessonProgress.request.CreateUserLessonProgressRequestDto;
import com.thanghub.courseservice.userLessonProgress.request.UpdateUserLessonProgressRequestDto;
import com.thanghub.courseservice.userLessonProgress.response.UserLessonProgressResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserLessonProgressServiceImpl implements UserLessonProgressService {
    private final UserLessonProgressRepository userLessonProgressRepository;
    private final LessonRepository lessonRepository;

    @Override
    public Page<UserLessonProgressResponse> getUserLessonProgresses(Pageable pageable) {
        return userLessonProgressRepository.findAll(pageable)
                .map(this::toResponse);
    }

    @Override
    public UserLessonProgressResponse getUserLessonProgress(String id) {
        UserLessonProgress progress = userLessonProgressRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new RuntimeException("UserLessonProgress not found"));
        return toResponse(progress);
    }

    @Override
    public UserLessonProgressResponse createUserLessonProgress(CreateUserLessonProgressRequestDto request) {
        Lesson lesson = lessonRepository.findById(request.getLessonId())
                .orElseThrow(() -> new RuntimeException("Lesson not found"));
        UserLessonProgress progress = UserLessonProgress.builder()
                .userId(request.getUserId())
                .lesson(lesson)
                .currentTime(0)
                .isCompleted(false)
                .build();
        return toResponse(userLessonProgressRepository.save(progress));
    }

    @Override
    public UserLessonProgressResponse updateUserLessonProgress(String id, UpdateUserLessonProgressRequestDto request) {
        UserLessonProgress progress = userLessonProgressRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new RuntimeException("UserLessonProgress not found"));
        progress.setCurrentTime(request.getCurrentTime());
        progress.setCompleted(request.isCompleted());
        return toResponse(userLessonProgressRepository.save(progress));
    }

    @Override
    public UserLessonProgressResponse deleteUserLessonProgress(String id) {
        UserLessonProgress progress = userLessonProgressRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new RuntimeException("UserLessonProgress not found"));
        userLessonProgressRepository.delete(progress);
        return toResponse(progress);
    }

    private UserLessonProgressResponse toResponse(UserLessonProgress progress) {
        return UserLessonProgressResponse.builder()
                .id(progress.getId())
                .userId(progress.getUserId())
                .lessonId(progress.getLesson().getId())
                .currentTime(progress.getCurrentTime())
                .isCompleted(progress.isCompleted())
                .build();
    }
}
