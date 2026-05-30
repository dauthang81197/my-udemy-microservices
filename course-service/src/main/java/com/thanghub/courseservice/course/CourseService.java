package com.thanghub.courseservice.course;

import com.thanghub.common.enums.CourseStatusEnum;
import com.thanghub.common.enums.LevelEnum;
import com.thanghub.courseservice.course.request.AutoSetupRequestDto;
import com.thanghub.courseservice.course.request.CreateCourseRequestDto;
import com.thanghub.courseservice.course.request.UpdateCourseRequestDto;
import com.thanghub.courseservice.course.response.AutoSetupResultDto;
import com.thanghub.courseservice.course.response.CourseDetailResponse;
import com.thanghub.courseservice.course.response.CourseResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface CourseService {
    Page<CourseResponse> getCourses(Boolean isAdmin, String title, CourseStatusEnum status, LevelEnum level, Pageable pageable);

    Page<CourseResponse> getCourses(UUID uuid, String title, CourseStatusEnum status, LevelEnum level, Pageable pageable);

    Page<CourseResponse> getCoursesEnroll(UUID uuid, String title, CourseStatusEnum status, LevelEnum level, Pageable pageable);

    CourseDetailResponse getCourse(String id);

    CourseDetailResponse getCourse(String id, UUID userId);

    CourseResponse createCourse(CreateCourseRequestDto request);

    CourseResponse updateCourse(String id, UpdateCourseRequestDto request);

    CourseResponse publicCourse(String id);

    CourseResponse deleteCourse(String id);

    AutoSetupResultDto autoSetupFromVideos(String courseId, List<UUID> videoIds);
}
