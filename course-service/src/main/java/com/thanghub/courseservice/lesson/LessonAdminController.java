package com.thanghub.courseservice.lesson;

import com.thanghub.common.ApiResponseBase;
import com.thanghub.common.mapper.PaginationMapper;
import com.thanghub.common.response.PaginationResponse;
import com.thanghub.courseservice.lesson.request.CreateLessonRequestDto;
import com.thanghub.courseservice.lesson.request.UpdateLessonRequestDto;
import com.thanghub.courseservice.lesson.response.LessonResponse;
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
@RequestMapping("admin/lessons")
@RequiredArgsConstructor
@Tag(name = "Lesson")
public class LessonAdminController {
    private final LessonService lessonService;

    @Operation(
            summary = "Lesson",
            description = "Return Lesson List"
    )
    @GetMapping()
    public PaginationResponse<LessonResponse> getLessons(@PageableDefault(page = 0, size = 10, sort = "title", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<LessonResponse> page = lessonService.getLessons(pageable);
        return PaginationMapper.from(page);
    }

    @Operation(
            summary = "Lesson",
            description = "Return Lesson"
    )
    @GetMapping(":id")
    public ResponseEntity<?> getLesson(@RequestParam("id") String id) {
        LessonResponse Section = lessonService.getLesson(id);
        return ResponseEntity.ok(ApiResponseBase.ok("Login successfully", Section));
    }

    @Operation(
            summary = "Lesson",
            description = "Return Lesson"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Create Section successfully"),
            @ApiResponse(responseCode = "401", description = "Check token and role")
    })
    @PostMapping()
    public ResponseEntity<?> createLesson(CreateLessonRequestDto request) {
        LessonResponse createLessonRequestDto = lessonService.createLesson(request);
        return ResponseEntity.ok(ApiResponseBase.ok("Login successfully", createLessonRequestDto));
    }

    @Operation(
            summary = "Lesson",
            description = "Return Lesson "
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Update Section successfully"),
            @ApiResponse(responseCode = "401", description = "Check token and role")
    })
    @PutMapping(":id")
    public ResponseEntity<?> updateLesson(@RequestParam("id") String id, UpdateLessonRequestDto request) {
        LessonResponse updateLessonRequestDto = lessonService.updateLesson(id, request);
        return ResponseEntity.ok(ApiResponseBase.ok("Update successfully", updateLessonRequestDto));
    }

    @Operation(
            summary = "Lesson",
            description = "Return Lesson "
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Update Section successfully"),
            @ApiResponse(responseCode = "401", description = "Check token and role")
    })
    @DeleteMapping(":id")
    public ResponseEntity<?> deleteLesson(@RequestParam("id") String id) {
        LessonResponse deleteLessonRequestDto = lessonService.deleteLesson(id);
        return ResponseEntity.ok(ApiResponseBase.ok("Delete successfully", deleteLessonRequestDto));
    }
}
