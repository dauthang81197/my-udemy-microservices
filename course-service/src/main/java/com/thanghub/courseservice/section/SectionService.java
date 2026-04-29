package com.thanghub.courseservice.section;

import com.thanghub.courseservice.section.request.CreateSectionRequestDto;
import com.thanghub.courseservice.section.request.UpdateSectionRequestDto;
import com.thanghub.courseservice.section.response.SectionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SectionService {
    Page<SectionResponse> getSections(Pageable pageable);

    SectionResponse getSection(String id);

    SectionResponse createSection(CreateSectionRequestDto request);

    SectionResponse updateSection(String id, UpdateSectionRequestDto request);

    SectionResponse deleteSection(String id);
}
