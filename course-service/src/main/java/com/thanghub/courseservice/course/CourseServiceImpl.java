package com.thanghub.courseservice.course;

import com.thanghub.common.enums.CourseStatusEnum;
import com.thanghub.common.enums.LevelEnum;
import com.thanghub.courseservice.course.request.CreateCourseRequestDto;
import com.thanghub.courseservice.course.request.UpdateCourseRequestDto;
import com.thanghub.courseservice.course.response.CourseDetailResponse;
import com.thanghub.courseservice.course.response.CourseResponse;
import com.thanghub.courseservice.lesson.Lesson;
import com.thanghub.courseservice.lesson.response.LessonDetailResponse;
import com.thanghub.courseservice.section.Section;
import com.thanghub.courseservice.section.SectionRepository;
import com.thanghub.courseservice.section.response.SectionDetailResponse;
import com.thanghub.courseservice.userCourse.UserCourse;
import com.thanghub.courseservice.userCourse.UserCourseRepository;
import com.thanghub.courseservice.userLessonProgress.UserLessonProgress;
import com.thanghub.courseservice.userLessonProgress.UserLessonProgressRepository;
import com.thanghub.courseservice.userLessonProgress.response.UserLessonProgressResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service()
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {
    private final CourseRepository courseRepository;
    private final SectionRepository sectionRepository;
    private final UserCourseRepository userCourseRepository;
    private final UserLessonProgressRepository userLessonProgressRepository;

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
    public CourseDetailResponse getCourse(String id) {
        return courseRepository.findById(UUID.fromString(id))
                .map(course -> toCourseDetailResponse(course, Collections.emptyMap()))
                .orElseThrow(() -> new RuntimeException("Course not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public CourseDetailResponse getCourse(String id, UUID userId) {
        UUID courseId = UUID.fromString(id);
        Course course = courseRepository.findByIdWithDetails(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        // Second query initializes the lessons collection on each Section in the Hibernate
        // first-level cache, avoiding MultipleBagFetchException from a single JOIN FETCH query.
        sectionRepository.findByCourseIdWithLessons(courseId);

        List<UUID> lessonIds = course.getSections().stream()
                .flatMap(s -> s.getLessons().stream())
                .map(Lesson::getId)
                .toList();

        Map<UUID, UserLessonProgress> progressMap = (userId != null && !lessonIds.isEmpty())
                ? userLessonProgressRepository.findByUserIdAndLessonIdIn(userId, lessonIds)
                  .stream().collect(Collectors.toMap(p -> p.getLesson().getId(), p -> p))
                : Collections.emptyMap();

        return toCourseDetailResponse(course, progressMap);
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

    private CourseDetailResponse toCourseDetailResponse(Course course, Map<UUID, UserLessonProgress> progressMap) {
        List<SectionDetailResponse> sections = course.getSections().stream()
                .sorted(Comparator.comparing(Section::getTitle))
                .map(section -> {
                    List<LessonDetailResponse> lessons = section.getLessons().stream()
                            .map(lesson -> toLessonDetailResponse(lesson, progressMap.get(lesson.getId())))
                            .toList();
                    long completed = lessons.stream()
                            .filter(l -> l.getProgress() != null && l.getProgress().isCompleted())
                            .count();
                    return SectionDetailResponse.builder()
                            .id(section.getId())
                            .title(section.getTitle())
                            .lessons(lessons)
                            .totalLessons(lessons.size())
                            .completedLessons((int) completed)
                            .build();
                })
                .toList();

        int totalLessons = sections.stream().mapToInt(SectionDetailResponse::getTotalLessons).sum();
        int completedLessons = sections.stream().mapToInt(SectionDetailResponse::getCompletedLessons).sum();

        // Current lesson: in-progress (currentTime > 0, not completed); fallback to first incomplete
        UUID currentLessonId = sections.stream()
                .flatMap(s -> s.getLessons().stream())
                .filter(l -> l.getProgress() != null && !l.getProgress().isCompleted() && l.getProgress().getCurrentTime() > 0)
                .findFirst()
                .map(LessonDetailResponse::getId)
                .orElse(null);

        return CourseDetailResponse.builder()
                .id(course.getId())
                .title(course.getTitle())
                .description(course.getDescription())
                .level(course.getLevel())
                .status(course.getStatus())
                .sections(sections)
                .totalLessons(totalLessons)
                .completedLessons(completedLessons)
                .currentLessonId(currentLessonId)
                .build();
    }

    private LessonDetailResponse toLessonDetailResponse(Lesson lesson, UserLessonProgress progress) {
        UserLessonProgressResponse progressResponse = progress == null ? null :
                UserLessonProgressResponse.builder()
                .id(progress.getId())
                .userId(progress.getUserId())
                .lessonId(lesson.getId())
                .currentTime(progress.getCurrentTime())
                .isCompleted(progress.isCompleted())
                .build();

        return LessonDetailResponse.builder()
                .id(lesson.getId())
                .title(lesson.getTitle())
                .description(lesson.getDescription())
                .type(lesson.getType())
                .videoFileId(lesson.getVideoFile() != null ? lesson.getVideoFile().getId() : null)
                .isPreview(lesson.getIs_preview())
                .sortOrder(lesson.getSort_order())
                .status(lesson.getStatus())
                .isStarted(progress != null)
                .progress(progressResponse)
                .build();
    }
}
