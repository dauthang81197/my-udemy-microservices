package com.thanghub.courseservice.userCourse;

import com.thanghub.courseservice.course.Course;
import com.thanghub.courseservice.course.CourseRepository;
import com.thanghub.courseservice.userCourse.request.CreateAdminUserCourseRequestDto;
import com.thanghub.courseservice.userCourse.request.CreateUserCourseRequestDto;
import com.thanghub.courseservice.userCourse.request.UpdateUserCourseRequestDto;
import com.thanghub.courseservice.userCourse.response.UserCourseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserCourseServiceImpl implements UserCourseService {
    private final UserCourseRepository userCourseRepository;
    private final CourseRepository courseRepository;

    @Override
    public Page<UserCourseResponse> getUserCourses(Pageable pageable) {
        return userCourseRepository.findAll(pageable)
                .map(this::toResponse);
    }

    @Override
    public UserCourseResponse getUserCourse(String id) {
        UserCourse userCourse = userCourseRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new RuntimeException("UserCourse not found"));
        return toResponse(userCourse);
    }

    @Override
    public UserCourseResponse createUserAdminCourse(CreateAdminUserCourseRequestDto request) {
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new RuntimeException("Course not found"));
        UserCourse userCourse = UserCourse.builder()
                .userId(request.getUserId())
                .course(course)
                .status(UserCourseStatusEnum.ENROLLED)
                .progress(0)
                .build();
        return toResponse(userCourseRepository.save(userCourse));
    }

    @Override
    public UserCourseResponse createUserCourse(CreateUserCourseRequestDto request, UUID userId) {
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new RuntimeException("Course not found"));

        // Check if user is already enrolled in the course
        boolean alreadyEnrolled = userCourseRepository.existsByUserIdAndCourseId(userId, course.getId());
        if (alreadyEnrolled) {
            throw new RuntimeException("User is already enrolled in this course");
        }
        UserCourse userCourse = UserCourse.builder()
                .userId(userId)
                .course(course)
                .status(UserCourseStatusEnum.ENROLLED)
                .progress(0)
                .build();
        return toResponse(userCourseRepository.save(userCourse));
    }

    @Override
    public UserCourseResponse updateUserCourse(String id, UpdateUserCourseRequestDto request) {
        UserCourse userCourse = userCourseRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new RuntimeException("UserCourse not found"));
        userCourse.setStatus(request.getStatus());
        userCourse.setProgress(request.getProgress());
        return toResponse(userCourseRepository.save(userCourse));
    }

    @Override
    public UserCourseResponse deleteUserCourse(String id) {
        UserCourse userCourse = userCourseRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new RuntimeException("UserCourse not found"));
        userCourseRepository.delete(userCourse);
        return toResponse(userCourse);
    }

    private UserCourseResponse toResponse(UserCourse userCourse) {
        return UserCourseResponse.builder()
                .id(userCourse.getId())
                .userId(userCourse.getUserId())
                .courseId(userCourse.getCourse().getId())
                .status(userCourse.getStatus())
                .progress(userCourse.getProgress())
                .build();
    }
}
