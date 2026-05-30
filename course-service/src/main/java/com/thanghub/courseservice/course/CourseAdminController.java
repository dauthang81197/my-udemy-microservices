package com.thanghub.courseservice.course;

import com.thanghub.common.ApiResponseBase;
import com.thanghub.common.enums.CourseStatusEnum;
import com.thanghub.common.enums.LevelEnum;
import com.thanghub.common.mapper.PaginationMapper;
import com.thanghub.common.response.PaginationResponse;
import com.thanghub.courseservice.course.request.AutoSetupRequestDto;
import com.thanghub.courseservice.course.request.CreateCourseRequestDto;
import com.thanghub.courseservice.course.request.UpdateCourseRequestDto;
import com.thanghub.courseservice.course.response.AutoSetupResultDto;
import com.thanghub.courseservice.course.response.CourseDetailResponse;
import com.thanghub.courseservice.course.response.CourseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

    @Operation(summary = "Course", description = "Return Course List, filterable by title, status, level")
    @GetMapping()
    public PaginationResponse<CourseResponse> getCourses(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) CourseStatusEnum status,
            @RequestParam(required = false) LevelEnum level,
            @PageableDefault(page = 0, size = 10, sort = "title", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<CourseResponse> page = courseService.getCourses(true, title, status, level, pageable);
        return PaginationMapper.from(page);
    }

    @Operation(
            summary = "Course",
            description = "Return Course"
    )
    @GetMapping("/{id}")
    public ResponseEntity<?> getCourses(@PathVariable String id) {
        CourseDetailResponse course = courseService.getCourse(id);
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
    public ResponseEntity<?> createCourse(@RequestBody CreateCourseRequestDto request) {
        CourseResponse course = courseService.createCourse(request);
        return ResponseEntity.ok(ApiResponseBase.ok("Create successfully", course));
    }

    @Operation(
            summary = "Course",
            description = "Return Course "
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Update Course successfully"),
            @ApiResponse(responseCode = "401", description = "Check token and role")
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> updateCourse(@PathVariable String id, @RequestBody UpdateCourseRequestDto request) {
        CourseResponse course = courseService.updateCourse(id, request);
        return ResponseEntity.ok(ApiResponseBase.ok("Update successfully", course));
    }

    @Operation(
            summary = "Course",
            description = "Return Course "
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Public Course successfully"),
            @ApiResponse(responseCode = "401", description = "Check token and role")
    })
    @PutMapping("/{id}/published")
    public ResponseEntity<?> publicCourse(@PathVariable String id) {
        CourseResponse course = courseService.publicCourse(id);
        return ResponseEntity.ok(ApiResponseBase.ok("Published successfully", course));
    }

    @Operation(
            summary = "Course",
            description = "Return Course "
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Update Course successfully"),
            @ApiResponse(responseCode = "401", description = "Check token and role")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCourse(@PathVariable String id) {
        CourseResponse course = courseService.deleteCourse(id);
        return ResponseEntity.ok(ApiResponseBase.ok("Delete successfully", course));
    }

    @Operation(
            summary = "Auto-setup course structure from videos",
            description = "Parse originalFilename của từng video theo convention '{N}-{Section}/{M}-{Lesson}.ext' để tạo sections và lessons tự động"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Setup completed (có thể có lỗi một số videos)"),
            @ApiResponse(responseCode = "500", description = "Course not found")
    })
    @PostMapping("/{id}/auto-setup")
    public ResponseEntity<?> autoSetup(
            @PathVariable String id,
            @RequestBody AutoSetupRequestDto request) {
        AutoSetupResultDto result = courseService.autoSetupFromVideos(id, request.getVideoIds());
        return ResponseEntity.ok(ApiResponseBase.ok("Auto-setup completed", result));
    }

}
