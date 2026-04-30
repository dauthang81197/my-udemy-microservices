package com.thanghub.courseservice.lesson;

import com.thanghub.common.enums.CourseStatusEnum;
import com.thanghub.courseservice.lesson.request.CreateLessonRequestDto;
import com.thanghub.courseservice.lesson.request.UpdateLessonRequestDto;
import com.thanghub.courseservice.lesson.response.LessonResponse;
import com.thanghub.courseservice.media.VideoFile;
import com.thanghub.courseservice.media.VideoFileRepository;
import com.thanghub.courseservice.section.Section;
import com.thanghub.courseservice.section.SectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LessonServiceImpl implements LessonService {
    private final LessonRepository lessonRepository;
    private final SectionRepository sectionRepository;
    private final VideoFileRepository videoFileRepository;

    @Override
    public Page<LessonResponse> getLessons(Pageable pageable) {
        return lessonRepository.findAll(pageable).map(this::toResponse);
    }

    @Override
    public LessonResponse getLesson(String id) {
        Lesson lesson = lessonRepository.findById(UUID.fromString(id)).orElseThrow(() -> new RuntimeException("Lesson not found"));
        return toResponse(lesson);
    }

    @Override
    public LessonResponse createLesson(CreateLessonRequestDto request) {
        Section section = sectionRepository.findById(request.getSectionId())
                .orElseThrow(() -> new RuntimeException("Section not found"));
        VideoFile videoFile = resolveVideoFile(request.getVideoFileId());
        return toResponse(lessonRepository.save(convertToEntity(request, section, videoFile)));
    }

    @Override
    public LessonResponse updateLesson(String id, UpdateLessonRequestDto request) {
        Lesson lesson = lessonRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new RuntimeException("Lesson not found"));
        lesson.setTitle(request.getTitle());
        lesson.setDescription(request.getDescription());
        lesson.setType(request.getType());
        lesson.setVideoFile(resolveVideoFile(request.getVideoFileId()));
        lesson.setIs_preview(request.getIsPreview());
        lesson.setSort_order(request.getSortOrder());
        return toResponse(lessonRepository.save(lesson));
    }

    @Override
    public LessonResponse deleteLesson(String id) {
        Lesson lesson = lessonRepository.findById(UUID.fromString(id)).orElseThrow(() -> new RuntimeException("Lesson not found"));
        lessonRepository.delete(lesson);
        return toResponse(lesson);
    }

    private VideoFile resolveVideoFile(UUID videoFileId) {
        if (videoFileId == null) return null;
        return videoFileRepository.findById(videoFileId)
                .orElseThrow(() -> new RuntimeException("VideoFile not found: " + videoFileId));
    }

    private Lesson convertToEntity(CreateLessonRequestDto request, Section section, VideoFile videoFile) {
        return Lesson.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .type(request.getType())
                .videoFile(videoFile)
                .is_preview(request.getIsPreview())
                .sort_order(request.getSortOrder())
                .status(CourseStatusEnum.DRAFT)
                .section(section)
                .build();
    }

    private LessonResponse toResponse(Lesson lesson) {
        VideoFile videoFile = lesson.getVideoFile();
        return LessonResponse.builder()
                .id(lesson.getId())
                .title(lesson.getTitle())
                .description(lesson.getDescription())
                .type(lesson.getType())
                .videoFileId(videoFile != null ? videoFile.getId() : null)
                .videoUrl(videoFile != null ? videoFile.getPublicUrl() : null)
                .isPreview(lesson.getIs_preview())
                .sortOrder(lesson.getSort_order())
                .status(lesson.getStatus())
                .build();
    }
}