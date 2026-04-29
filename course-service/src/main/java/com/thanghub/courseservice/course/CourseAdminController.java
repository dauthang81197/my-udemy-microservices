package com.thanghub.courseservice.course;

import com.thanghub.common.ApiResponseBase;
import com.thanghub.common.mapper.PaginationMapper;
import com.thanghub.common.response.PaginationResponse;
import com.thanghub.courseservice.course.request.CreateCourseRequestDto;
import com.thanghub.courseservice.course.request.UpdateCourseRequestDto;
import com.thanghub.courseservice.course.response.CourseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("admin/courses")
@RequiredArgsConstructor
@Tag(name = "Course")
public class CourseAdminController {
    private final CourseService courseService;

    @Operation(
            summary = "Course",
            description = "Return Course List"
    )
    @GetMapping()
    public PaginationResponse<CourseResponse> getCourses(@PageableDefault(page = 0, size = 10, sort = "title", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<CourseResponse> page = courseService.getCourses(pageable);
        return PaginationMapper.from(page);
    }

    @Operation(
            summary = "Course",
            description = "Return Course"
    )
    @GetMapping(":id")
    public ResponseEntity<?> getCourses(@RequestParam("id") String id) {
        CourseResponse course = courseService.getCourse(id);
        return ResponseEntity.ok(ApiResponseBase.ok("Login successfully", course));
    }

    @Operation(
            summary = "Course",
            description = "Return Course"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Create Course successfully"),
            @ApiResponse(responseCode = "401", description = "Check token and role")
    })
    @PostMapping()
    public ResponseEntity<?> createCourse(CreateCourseRequestDto request) {
        Course createCourseRequestDto = courseService.createCourse(request);
        return ResponseEntity.ok(ApiResponseBase.ok("Login successfully", createCourseRequestDto));
    }

    @Operation(
            summary = "Course",
            description = "Return Course "
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Update Course successfully"),
            @ApiResponse(responseCode = "401", description = "Check token and role")
    })
    @PutMapping(":id")
    public ResponseEntity<?> updateCourse(@RequestParam("id") String id, UpdateCourseRequestDto request) {
        Course createCourseRequestDto = courseService.updateCourse(id, request);
        return ResponseEntity.ok(ApiResponseBase.ok("Update successfully", createCourseRequestDto));
    }

    @Operation(
            summary = "Course",
            description = "Return Course "
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Update Course successfully"),
            @ApiResponse(responseCode = "401", description = "Check token and role")
    })
    @DeleteMapping(":id")
    public ResponseEntity<?> deleteCourse(@RequestParam("id") String id) {
        Course createCourseRequestDto = courseService.deleteCourse(id);
        return ResponseEntity.ok(ApiResponseBase.ok("Delete successfully", createCourseRequestDto));
    }

}
