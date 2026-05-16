package com.thanghub.courseservice.section.response;

import com.thanghub.common.enums.CourseStatusEnum;
import com.thanghub.common.enums.LevelEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SectionResponse {
    private UUID id;
    private String title;
    private int sort;
    private String description;
    private LevelEnum level;
    private CourseStatusEnum status;
}
