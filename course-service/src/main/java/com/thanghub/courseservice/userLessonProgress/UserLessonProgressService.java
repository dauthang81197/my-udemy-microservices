package com.thanghub.courseservice.userLessonProgress;

import com.thanghub.courseservice.userLessonProgress.request.CreateUserLessonProgressRequestDto;
import com.thanghub.courseservice.userLessonProgress.request.UpdateUserLessonProgressRequestDto;
import com.thanghub.courseservice.userLessonProgress.response.UserLessonProgressResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserLessonProgressService {
    Page<UserLessonProgressResponse> getUserLessonProgresses(Pageable pageable);

    UserLessonProgressResponse getUserLessonProgress(String id);

    UserLessonProgressResponse createUserLessonProgress(CreateUserLessonProgressRequestDto request);

    UserLessonProgressResponse updateUserLessonProgress(String id, UpdateUserLessonProgressRequestDto request);

    UserLessonProgressResponse deleteUserLessonProgress(String id);
}
