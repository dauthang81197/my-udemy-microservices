package com.thanghub.courseservice.course;

import com.thanghub.common.enums.CourseStatusEnum;
import com.thanghub.courseservice.course.request.CreateCourseRequestDto;
import com.thanghub.courseservice.course.request.UpdateCourseRequestDto;
import com.thanghub.courseservice.course.response.CourseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service()
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {
    private final CourseRepository courseRepository;

    @Override
    public Page<CourseResponse> getCourses(Pageable pageable) {
        return courseRepository.findAll(pageable)
                .map(this::toResponse);
    }

    @Override
    public CourseResponse getCourse(String id) {
       Course course = courseRepository.findById(UUID.fromString(id)).orElseThrow(() -> new RuntimeException("Course not found"));
        return toResponse(course);
    }

    @Override
    public Course createCourse(CreateCourseRequestDto request) {
        return courseRepository.save(convertCourseDtoToEntity(request));
    }

    @Override
    public Course updateCourse(String id, UpdateCourseRequestDto request) {
        // Find by ID
        Course course = courseRepository.findById(UUID.fromString(id)).orElseThrow(() -> new RuntimeException("Course not found"));
        course.setTitle(request.getTitle());
        course.setDescription(request.getDescription());
        course.setLevel(request.getLevel());
        return courseRepository.save(course);
    }

    @Override
    public Course deleteCourse(String id) {
        // Find by ID
        Course course = courseRepository.findById(UUID.fromString(id)).orElseThrow(() -> new RuntimeException("Course not found"));
        if(!course.getSections().isEmpty()) {
            throw new RuntimeException("Course has sections, can't delete");
        }
        courseRepository.delete(course);
        return course;
    }

    private Course convertCourseDtoToEntity(CreateCourseRequestDto request) {
        return Course
                .builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .level(request.getLevel())
                .status(CourseStatusEnum.DRAFT)
                .build();
    }

    private CourseResponse toResponse(Course course) {
        return CourseResponse.builder()
                .id(course.getId())
                .title(course.getTitle())
                .description(course.getDescription())
                .level(course.getLevel())
                .status(course.getStatus())
                .build();
    }
}
