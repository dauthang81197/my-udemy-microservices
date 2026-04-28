package com.thanghub.courseservice.course;

import com.thanghub.courseservice.course.request.CreateCourseRequestDto;
import com.thanghub.courseservice.course.request.UpdateCourseRequestDto;
import com.thanghub.courseservice.course.response.CourseResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CourseService {
    Page<CourseResponse> getCourses(Pageable pageable);

    Course createCourse(CreateCourseRequestDto request);

    Course updateCourse(String id, UpdateCourseRequestDto request);
}
