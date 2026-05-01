package com.thanghub.courseservice.userLessonProgress;

import com.thanghub.common.ApiResponseBase;
import com.thanghub.common.mapper.PaginationMapper;
import com.thanghub.common.response.PaginationResponse;
import com.thanghub.courseservice.userLessonProgress.request.CreateUserLessonProgressRequestDto;
import com.thanghub.courseservice.userLessonProgress.request.UpdateUserLessonProgressRequestDto;
import com.thanghub.courseservice.userLessonProgress.response.UserLessonProgressResponse;
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
@RequestMapping("admin/user-lesson-progress")
@RequiredArgsConstructor
@Tag(name = "UserLessonProgress")
public class UserLessonProgressAdminController {
    private final UserLessonProgressService userLessonProgressService;

    @Operation(summary = "List user lesson progresses", description = "Return paginated user lesson progress list")
    @GetMapping
    public PaginationResponse<UserLessonProgressResponse> getUserLessonProgresses(
            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<UserLessonProgressResponse> page = userLessonProgressService.getUserLessonProgresses(pageable);
        return PaginationMapper.from(page);
    }

    @Operation(summary = "Get user lesson progress", description = "Return user lesson progress detail")
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserLessonProgress(@PathVariable String id) {
        UserLessonProgressResponse progress = userLessonProgressService.getUserLessonProgress(id);
        return ResponseEntity.ok(ApiResponseBase.ok("Success", progress));
    }

    @Operation(summary = "Create user lesson progress", description = "Track a user's progress on a lesson")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Create successfully"),
            @ApiResponse(responseCode = "401", description = "Check token and role")
    })
    @PostMapping
    public ResponseEntity<?> createUserLessonProgress(@RequestBody CreateUserLessonProgressRequestDto request) {
        UserLessonProgressResponse progress = userLessonProgressService.createUserLessonProgress(request);
        return ResponseEntity.ok(ApiResponseBase.ok("Create successfully", progress));
    }

    @Operation(summary = "Update user lesson progress", description = "Update current time and completion status")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Update successfully"),
            @ApiResponse(responseCode = "401", description = "Check token and role")
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUserLessonProgress(@PathVariable String id, @RequestBody UpdateUserLessonProgressRequestDto request) {
        UserLessonProgressResponse progress = userLessonProgressService.updateUserLessonProgress(id, request);
        return ResponseEntity.ok(ApiResponseBase.ok("Update successfully", progress));
    }

    @Operation(summary = "Delete user lesson progress", description = "Remove a user lesson progress record")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Delete successfully"),
            @ApiResponse(responseCode = "401", description = "Check token and role")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUserLessonProgress(@PathVariable String id) {
        UserLessonProgressResponse progress = userLessonProgressService.deleteUserLessonProgress(id);
        return ResponseEntity.ok(ApiResponseBase.ok("Delete successfully", progress));
    }
}
