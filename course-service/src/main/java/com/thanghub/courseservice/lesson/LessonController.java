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
@RequestMapping("lessons")
@RequiredArgsConstructor
@Tag(name = "Lesson")
public class LessonController {

    private final LessonService lessonService;

    @Operation(summary = "List lessons", description = "Return paginated lesson list")
    @GetMapping
    public PaginationResponse<LessonResponse> getLessons(
            @PageableDefault(page = 0, size = 10, sort = "title", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<LessonResponse> page = lessonService.getLessons(pageable);
        return PaginationMapper.from(page);
    }

    @Operation(summary = "Get lesson", description = "Return lesson detail")
    @GetMapping("/{id}")
    public ResponseEntity<?> getLesson(@PathVariable String id) {
        LessonResponse lesson = lessonService.getLesson(id);
        return ResponseEntity.ok(ApiResponseBase.ok("Success", lesson));
    }

    @Operation(summary = "Create lesson", description = "Create a new lesson")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Create lesson successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping
    public ResponseEntity<?> createLesson(@RequestBody CreateLessonRequestDto request) {
        LessonResponse lesson = lessonService.createLesson(request);
        return ResponseEntity.ok(ApiResponseBase.ok("Create successfully", lesson));
    }

    @Operation(summary = "Update lesson", description = "Update an existing lesson")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Update lesson successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> updateLesson(@PathVariable String id, @RequestBody UpdateLessonRequestDto request) {
        LessonResponse lesson = lessonService.updateLesson(id, request);
        return ResponseEntity.ok(ApiResponseBase.ok("Update successfully", lesson));
    }

    @Operation(summary = "Delete lesson", description = "Delete a lesson")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Delete lesson successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteLesson(@PathVariable String id) {
        LessonResponse lesson = lessonService.deleteLesson(id);
        return ResponseEntity.ok(ApiResponseBase.ok("Delete successfully", lesson));
    }
}
