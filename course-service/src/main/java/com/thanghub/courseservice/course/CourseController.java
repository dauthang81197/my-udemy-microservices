package com.thanghub.courseservice.course;

import com.thanghub.common.ApiResponseBase;
import com.thanghub.common.enums.LevelEnum;
import com.thanghub.common.mapper.PaginationMapper;
import com.thanghub.common.response.PaginationResponse;
import com.thanghub.courseservice.course.request.CreateCourseRequestDto;
import com.thanghub.courseservice.course.request.UpdateCourseRequestDto;
import com.thanghub.courseservice.course.response.CourseDetailResponse;
import com.thanghub.courseservice.course.response.CourseResponse;
import com.thanghub.courseservice.userCourse.service.CurrentUserService;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("courses")
@RequiredArgsConstructor
@Tag(name = "Course")
public class CourseController {

    private final CourseService courseService;
    private final CurrentUserService currentUserService;

    @Operation(summary = "List courses", description = "Return paginated course list, filterable by title, status, level")
    @GetMapping
    public PaginationResponse<CourseResponse> getCourses(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) LevelEnum level,
            @PageableDefault(page = 0, size = 10, sort = "title", direction = Sort.Direction.ASC) Pageable pageable,
            @AuthenticationPrincipal Jwt jwt) throws IllegalAccessException {
        UUID userId = currentUserService.getUserIdClaim(jwt);
        Page<CourseResponse> page = courseService.getCourses(userId, title, null, level, pageable);
        return PaginationMapper.from(page);
    }

    @Operation(summary = "List courses Enroll", description = "Return paginated course list, filterable by title, status, level")
    @GetMapping("/enroll")
    public PaginationResponse<CourseResponse> getCoursesEnroll(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) LevelEnum level,
            @PageableDefault(page = 0, size = 10, sort = "title", direction = Sort.Direction.ASC) Pageable pageable,
            @AuthenticationPrincipal Jwt jwt) throws IllegalAccessException {
        UUID userId = currentUserService.getUserIdClaim(jwt);
        Page<CourseResponse> page = courseService.getCoursesEnroll(userId, title, null, level, pageable);
        return PaginationMapper.from(page);
    }

    @Operation(summary = "Get course", description = "Return course detail with sections, lessons, and user progress")
    @GetMapping("/{id}")
    public ResponseEntity<?> getCourse(@PathVariable String id, @AuthenticationPrincipal Jwt jwt) throws IllegalAccessException {
        UUID userId = jwt != null ? currentUserService.getUserIdClaim(jwt) : null;
        CourseDetailResponse course = courseService.getCourse(id, userId);
        return ResponseEntity.ok(ApiResponseBase.ok("Success", course));
    }

    @Operation(summary = "Create course", description = "Create a new course")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Create course successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping
    public ResponseEntity<?> createCourse(@RequestBody CreateCourseRequestDto request) {
        CourseResponse course = courseService.createCourse(request);
        return ResponseEntity.ok(ApiResponseBase.ok("Create successfully", course));
    }

    @Operation(summary = "Update course", description = "Update an existing course")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Update course successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> updateCourse(@PathVariable String id, @RequestBody UpdateCourseRequestDto request) {
        CourseResponse course = courseService.updateCourse(id, request);
        return ResponseEntity.ok(ApiResponseBase.ok("Update successfully", course));
    }

    @Operation(summary = "Delete course", description = "Delete a course")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Delete course successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCourse(@PathVariable String id) {
        CourseResponse course = courseService.deleteCourse(id);
        return ResponseEntity.ok(ApiResponseBase.ok("Delete successfully", course));
    }
}
