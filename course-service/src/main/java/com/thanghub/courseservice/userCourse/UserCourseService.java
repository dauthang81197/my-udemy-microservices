package com.thanghub.courseservice.userCourse;

import com.thanghub.courseservice.userCourse.request.CreateAdminUserCourseRequestDto;
import com.thanghub.courseservice.userCourse.request.CreateUserCourseRequestDto;
import com.thanghub.courseservice.userCourse.request.UpdateUserCourseRequestDto;
import com.thanghub.courseservice.userCourse.response.UserCourseResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface UserCourseService {
    Page<UserCourseResponse> getUserCourses(Pageable pageable);

    UserCourseResponse getUserCourse(String id);

    UserCourseResponse createUserAdminCourse(CreateAdminUserCourseRequestDto request);

    UserCourseResponse createUserCourse(CreateUserCourseRequestDto request, UUID userId);

    UserCourseResponse updateUserCourse(String id, UpdateUserCourseRequestDto request);

    UserCourseResponse deleteUserCourse(String id);
}
