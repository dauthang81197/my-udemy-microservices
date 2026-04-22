package com.thanghub.courseservice.course;

import com.thanghub.courseservice.common.mapper.PaginationMapper;
import com.thanghub.courseservice.common.response.PaginationResponse;
import com.thanghub.courseservice.course.response.CourseResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/courses")
@RequiredArgsConstructor
@Tag(name = "")
public class CourseAdminController {
    private final CourseService courseService;

    @GetMapping()
    @SecurityRequirements
    public PaginationResponse<CourseResponse> getCourses(Pageable pageable) {
        Page<CourseResponse> page = courseService.getCourses(pageable);
        return PaginationMapper.from(page);
    }
}
