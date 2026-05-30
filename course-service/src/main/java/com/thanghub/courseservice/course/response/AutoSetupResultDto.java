package com.thanghub.courseservice.course.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AutoSetupResultDto {
    private int sectionsCreated;
    private int lessonsCreated;
    private List<String> errors;
}
