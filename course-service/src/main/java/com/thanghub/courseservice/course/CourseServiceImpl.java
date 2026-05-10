package com.thanghub.courseservice.course;

import com.thanghub.common.enums.CourseStatusEnum;
import com.thanghub.common.enums.LevelEnum;
import com.thanghub.courseservice.course.request.CreateCourseRequestDto;
import com.thanghub.courseservice.course.request.UpdateCourseRequestDto;
import com.thanghub.courseservice.course.response.CourseResponse;
import com.thanghub.courseservice.userCourse.UserCourse;
import com.thanghub.courseservice.userCourse.UserCourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service()
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {
    private final CourseRepository courseRepository;
    private final UserCourseRepository userCourseRepository;

    @Override
    public Page<CourseResponse> getCourses(Boolean isAdmin, String title, CourseStatusEnum status, LevelEnum level, Pageable pageable) {
        Specification<Course> spec = Specification
                .where(CourseSpecification.titleContains(title))
                .and(CourseSpecification.hasStatus(isAdmin ? status : CourseStatusEnum.PUBLISHED))
                .and(CourseSpecification.hasLevel(level));
        return courseRepository.findAll(spec, pageable).map(this::toResponse);
    }

    @Override
    public Page<CourseResponse> getCourses(UUID userId, String title, CourseStatusEnum status, LevelEnum level, Pageable pageable) {
        List<UserCourse> userCourse = userCourseRepository.findAllByUserId(userId);
        Specification<Course> spec = Specification
                .where(CourseSpecification.excludeCourseIds(userCourse.stream().map(uc -> uc.getCourse().getId()).toList()));
        return courseRepository.findAll(spec, pageable).map(this::toResponse);
    }

    @Override
    public Page<CourseResponse> getCoursesEnroll(UUID userId, String title, CourseStatusEnum status, LevelEnum level, Pageable pageable) {
        List<UserCourse> userCourse = userCourseRepository.findAllByUserId(userId);
        Specification<Course> spec = Specification
                .where(CourseSpecification.hasCourseIds(userCourse.stream().map(uc -> uc.getCourse().getId()).toList()));
        return courseRepository.findAll(spec, pageable).map(this::toResponse);
    }

    @Override
    public CourseResponse getCourse(String id) {
        Course course = courseRepository.findById(UUID.fromString(id)).orElseThrow(() -> new RuntimeException("Course not found"));
        return toResponse(course);
    }


    @Override
    public CourseResponse createCourse(CreateCourseRequestDto request) {
        return toResponse(courseRepository.save(convertCourseDtoToEntity(request)));
    }

    @Override
    public CourseResponse updateCourse(String id, UpdateCourseRequestDto request) {
        Course course = courseRepository.findById(UUID.fromString(id)).orElseThrow(() -> new RuntimeException("Course not found"));
        course.setTitle(request.getTitle());
        course.setDescription(request.getDescription());
        course.setLevel(request.getLevel());
        return toResponse(courseRepository.save(course));
    }

    @Override
    public CourseResponse publicCourse(String id) {
        Course course = courseRepository.findById(UUID.fromString(id)).orElseThrow(() -> new RuntimeException("Course not found"));
        course.setStatus(CourseStatusEnum.PUBLISHED);
        return toResponse(courseRepository.save(course));
    }

    @Override
    public CourseResponse deleteCourse(String id) {
        Course course = courseRepository.findById(UUID.fromString(id)).orElseThrow(() -> new RuntimeException("Course not found"));
        if (!course.getSections().isEmpty()) {
            throw new RuntimeException("Course has sections, can't delete");
        }
        courseRepository.delete(course);
        return toResponse(course);
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
