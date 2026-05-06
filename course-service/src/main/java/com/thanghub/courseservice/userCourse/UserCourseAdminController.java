package com.thanghub.courseservice.userCourse;

import com.thanghub.common.ApiResponseBase;
import com.thanghub.common.mapper.PaginationMapper;
import com.thanghub.common.response.PaginationResponse;
import com.thanghub.courseservice.userCourse.request.CreateAdminUserCourseRequestDto;
import com.thanghub.courseservice.userCourse.request.UpdateUserCourseRequestDto;
import com.thanghub.courseservice.userCourse.response.UserCourseResponse;
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
@RequestMapping("admin/user-courses")
@RequiredArgsConstructor
@Tag(name = "UserCourse")
public class UserCourseAdminController {
    private final UserCourseService userCourseService;

    @Operation(summary = "List user courses", description = "Return paginated user course list")
    @GetMapping
    public PaginationResponse<UserCourseResponse> getUserCourses(
            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<UserCourseResponse> page = userCourseService.getUserCourses(pageable);
        return PaginationMapper.from(page);
    }

    @Operation(summary = "Get user course", description = "Return user course detail")
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserCourse(@PathVariable String id) {
        UserCourseResponse userCourse = userCourseService.getUserCourse(id);
        return ResponseEntity.ok(ApiResponseBase.ok("Success", userCourse));
    }

    @Operation(summary = "Create user course", description = "Enroll a user to a course")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Create user course successfully"),
            @ApiResponse(responseCode = "401", description = "Check token and role")
    })
    @PostMapping
    public ResponseEntity<?> createUserCourse(@RequestBody CreateAdminUserCourseRequestDto request) {
        UserCourseResponse userCourse = userCourseService.createUserAdminCourse(request);
        return ResponseEntity.ok(ApiResponseBase.ok("Create successfully", userCourse));
    }

    @Operation(summary = "Update user course", description = "Update progress or status of a user course")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Update user course successfully"),
            @ApiResponse(responseCode = "401", description = "Check token and role")
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUserCourse(@PathVariable String id, @RequestBody UpdateUserCourseRequestDto request) {
        UserCourseResponse userCourse = userCourseService.updateUserCourse(id, request);
        return ResponseEntity.ok(ApiResponseBase.ok("Update successfully", userCourse));
    }

    @Operation(summary = "Delete user course", description = "Remove a user course enrollment")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Delete user course successfully"),
            @ApiResponse(responseCode = "401", description = "Check token and role")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUserCourse(@PathVariable String id) {
        UserCourseResponse userCourse = userCourseService.deleteUserCourse(id);
        return ResponseEntity.ok(ApiResponseBase.ok("Delete successfully", userCourse));
    }
}
