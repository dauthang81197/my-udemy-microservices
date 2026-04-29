package com.thanghub.courseservice.section;

import com.thanghub.courseservice.course.Course;
import com.thanghub.courseservice.course.CourseRepository;
import com.thanghub.courseservice.section.request.CreateSectionRequestDto;
import com.thanghub.courseservice.section.request.UpdateSectionRequestDto;
import com.thanghub.courseservice.section.response.SectionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service()
@RequiredArgsConstructor
public class SectionServiceImpl implements SectionService {
    private final SectionRepository sectionRepository;
    private final CourseRepository courseRepository;

    @Override
    public Page<SectionResponse> getSections(Pageable pageable) {
        return sectionRepository.findAll(pageable)
                .map(this::toResponse);
    }

    @Override
    public SectionResponse getSection(String id) {
        Section section = sectionRepository.findById(UUID.fromString(id)).orElseThrow(() -> new RuntimeException("Section not found"));
        return toResponse(section);
    }

    @Override
    public SectionResponse createSection(CreateSectionRequestDto request) {
        Course course = courseRepository.findById(request.getCourseId()).orElseThrow(() -> new RuntimeException("Course not found"));
        return toResponse(sectionRepository.save(convertSectionDtoToEntity(request, course)));
    }

    @Override
    public SectionResponse updateSection(String id, UpdateSectionRequestDto request) {
        courseRepository.findById(request.getCourseId()).orElseThrow(() -> new RuntimeException("Course not found"));
        Section section = sectionRepository.findById(UUID.fromString(id)).orElseThrow(() -> new RuntimeException("Section not found"));
        section.setTitle(request.getTitle());
        return toResponse(sectionRepository.save(section));
    }

    @Override
    public SectionResponse deleteSection(String id) {
        Section section = sectionRepository.findById(UUID.fromString(id)).orElseThrow(() -> new RuntimeException("Section not found"));
        sectionRepository.delete(section);
        return toResponse(section);
    }

    private Section convertSectionDtoToEntity(CreateSectionRequestDto request, Course course) {
        return Section
                .builder()
                .title(request.getTitle())
                .course(course)
                .build();
    }

    private SectionResponse toResponse(Section section) {
        return SectionResponse.builder()
                .id(section.getId())
                .title(section.getTitle())
                .build();
    }
}
