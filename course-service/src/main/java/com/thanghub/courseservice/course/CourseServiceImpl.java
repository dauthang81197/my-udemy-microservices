package com.thanghub.courseservice.course;

import com.thanghub.common.enums.CourseStatusEnum;
import com.thanghub.common.enums.LevelEnum;
import com.thanghub.courseservice.course.request.CreateCourseRequestDto;
import com.thanghub.courseservice.course.request.UpdateCourseRequestDto;
import com.thanghub.courseservice.course.response.CourseDetailResponse;
import com.thanghub.courseservice.course.response.CourseResponse;
import com.thanghub.common.enums.LessonTypeEnum;
import com.thanghub.courseservice.course.request.AutoSetupRequestDto;
import com.thanghub.courseservice.course.response.AutoSetupResultDto;
import com.thanghub.courseservice.lesson.Lesson;
import com.thanghub.courseservice.lesson.LessonRepository;
import com.thanghub.courseservice.lesson.response.LessonDetailResponse;
import com.thanghub.courseservice.media.VideoFile;
import com.thanghub.courseservice.media.VideoFileRepository;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service()
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {
    private final CourseRepository courseRepository;
    private final SectionRepository sectionRepository;
    private final LessonRepository lessonRepository;
    private final VideoFileRepository videoFileRepository;
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
                .sorted(Comparator.comparingInt(Section::getSort))
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
                            .sort(section.getSort())
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

    @Override
    @Transactional
    public AutoSetupResultDto autoSetupFromVideos(String courseId, List<UUID> videoIds) {
        Course course = courseRepository.findById(UUID.fromString(courseId))
                .orElseThrow(() -> new RuntimeException("Course not found"));

        List<String> errors = new ArrayList<>();

        if (videoIds == null || videoIds.isEmpty()) {
            return AutoSetupResultDto.builder()
                    .sectionsCreated(0).lessonsCreated(0).errors(errors).build();
        }

        Map<UUID, VideoFile> videoMap = videoFileRepository.findAllById(videoIds)
                .stream().collect(Collectors.toMap(VideoFile::getId, v -> v));

        record ParsedLesson(String title, VideoFile video) {}

        // [^/]+ prevents greedy match from crossing directory separators
        Pattern pattern = Pattern.compile("^(\\d+)-([^/]+)/(\\d+)-([^/]+)\\.[^.]+$");
        TreeMap<Integer, String> sectionTitles = new TreeMap<>();
        TreeMap<Integer, TreeMap<Integer, ParsedLesson>> sections = new TreeMap<>();

        for (UUID videoId : videoIds) {
            VideoFile video = videoMap.get(videoId);
            if (video == null) {
                errors.add(videoId + ": video not found");
                continue;
            }
            String filename = video.getOriginalFilename();
            if (filename == null || filename.isBlank()) {
                errors.add(videoId + ": filename is empty");
                continue;
            }
            Matcher matcher = pattern.matcher(filename);
            if (!matcher.matches()) {
                errors.add(filename + ": invalid filename format");
                continue;
            }
            int sectionOrder;
            int lessonOrder;
            try {
                sectionOrder = Integer.parseInt(matcher.group(1));
                lessonOrder = Integer.parseInt(matcher.group(3));
            } catch (NumberFormatException e) {
                errors.add(filename + ": order number out of range");
                continue;
            }
            String sectionTitle = matcher.group(2).replace("-", " ");
            String lessonTitle = matcher.group(4).replace("-", " ");

            sectionTitles.putIfAbsent(sectionOrder, sectionTitle);
            TreeMap<Integer, ParsedLesson> lessonMap =
                    sections.computeIfAbsent(sectionOrder, k -> new TreeMap<>());
            if (lessonMap.containsKey(lessonOrder)) {
                errors.add(filename + ": duplicate lesson order " + lessonOrder + " in section " + sectionOrder);
                continue;
            }
            lessonMap.put(lessonOrder, new ParsedLesson(lessonTitle, video));
        }

        int sectionsCreated = 0;
        int lessonsCreated = 0;

        for (Map.Entry<Integer, TreeMap<Integer, ParsedLesson>> sEntry : sections.entrySet()) {
            int sectionSort = sEntry.getKey();
            Section section = Section.builder()
                    .title(sectionTitles.get(sectionSort))
                    .sort(sectionSort)
                    .course(course)
                    .build();
            section = sectionRepository.save(section);
            sectionsCreated++;

            for (Map.Entry<Integer, ParsedLesson> lEntry : sEntry.getValue().entrySet()) {
                Lesson lesson = new Lesson();
                lesson.setTitle(lEntry.getValue().title());
                lesson.setDescription("");
                lesson.setType(LessonTypeEnum.VIDEO);
                lesson.setVideoFile(lEntry.getValue().video());
                lesson.setIs_preview(false);
                lesson.setSort_order(lEntry.getKey());
                lesson.setStatus(CourseStatusEnum.DRAFT);
                lesson.setSection(section);
                lessonRepository.save(lesson);
                lessonsCreated++;
            }
        }

        return AutoSetupResultDto.builder()
                .sectionsCreated(sectionsCreated)
                .lessonsCreated(lessonsCreated)
                .errors(errors)
                .build();
    }
}
