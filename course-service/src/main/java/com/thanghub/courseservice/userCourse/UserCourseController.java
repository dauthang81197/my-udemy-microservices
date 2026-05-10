package com.thanghub.courseservice.userCourse;

import com.thanghub.common.ApiResponseBase;
import com.thanghub.common.mapper.PaginationMapper;
import com.thanghub.common.response.PaginationResponse;
import com.thanghub.courseservice.userCourse.request.CreateUserCourseRequestDto;
import com.thanghub.courseservice.userCourse.request.UpdateUserCourseRequestDto;
import com.thanghub.courseservice.userCourse.response.UserCourseResponse;
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
@RequestMapping("user-courses")
@RequiredArgsConstructor
@Tag(name = "UserCourse")
public class UserCourseController {
    private final UserCourseService userCourseService;
    private final CurrentUserService currentUserService;

    @Operation(summary = "List user courses", description = "Return paginated user course list")
    @GetMapping
    public PaginationResponse<UserCourseResponse> getUserCourses(
            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal Jwt jwt) throws IllegalAccessException {
        UUID userId = currentUserService.getUserIdClaim(jwt);
        Page<UserCourseResponse> page = userCourseService.getUserCourses(pageable, userId);
        return PaginationMapper.from(page);
    }

    @Operation(summary = "Get user course", description = "Return user course detail")
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserCourse(@PathVariable String id) {
        UserCourseResponse userCourse = userCourseService.getUserCourse(id);
        return ResponseEntity.ok(ApiResponseBase.ok("Success", userCourse));
    }

    @Operation(summary = "Enroll course", description = "Enroll current user to a course")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Enrolled successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping
    public ResponseEntity<?> createUserCourse(
            @RequestBody CreateUserCourseRequestDto request,
            @AuthenticationPrincipal Jwt jwt) throws IllegalAccessException {
        UUID userId = currentUserService.getUserIdClaim(jwt);
        UserCourseResponse userCourse = userCourseService.createUserCourse(request, userId);
        return ResponseEntity.ok(ApiResponseBase.ok("Enrolled successfully", userCourse));
    }

    @Operation(summary = "Update user course", description = "Update progress or status")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Update successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUserCourse(@PathVariable String id, @RequestBody UpdateUserCourseRequestDto request) {
        UserCourseResponse userCourse = userCourseService.updateUserCourse(id, request);
        return ResponseEntity.ok(ApiResponseBase.ok("Update successfully", userCourse));
    }

    @Operation(summary = "Unenroll course", description = "Remove current user enrollment")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Unenrolled successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUserCourse(@PathVariable String id) {
        UserCourseResponse userCourse = userCourseService.deleteUserCourse(id);
        return ResponseEntity.ok(ApiResponseBase.ok("Unenrolled successfully", userCourse));
    }
}
