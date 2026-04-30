package com.thanghub.courseservice.section;

import com.thanghub.common.ApiResponseBase;
import com.thanghub.common.mapper.PaginationMapper;
import com.thanghub.common.response.PaginationResponse;
import com.thanghub.courseservice.section.request.CreateSectionRequestDto;
import com.thanghub.courseservice.section.request.UpdateSectionRequestDto;
import com.thanghub.courseservice.section.response.SectionResponse;
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
@RequestMapping("sections")
@RequiredArgsConstructor
@Tag(name = "Section")
public class SectionController {

    private final SectionService sectionService;

    @Operation(summary = "List sections", description = "Return paginated section list")
    @GetMapping
    public PaginationResponse<SectionResponse> getSections(
            @PageableDefault(page = 0, size = 10, sort = "title", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<SectionResponse> page = sectionService.getSections(pageable);
        return PaginationMapper.from(page);
    }

    @Operation(summary = "Get section", description = "Return section detail")
    @GetMapping("/{id}")
    public ResponseEntity<?> getSection(@PathVariable String id) {
        SectionResponse section = sectionService.getSection(id);
        return ResponseEntity.ok(ApiResponseBase.ok("Success", section));
    }

    @Operation(summary = "Create section", description = "Create a new section")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Create section successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping
    public ResponseEntity<?> createSection(@RequestBody CreateSectionRequestDto request) {
        SectionResponse section = sectionService.createSection(request);
        return ResponseEntity.ok(ApiResponseBase.ok("Create successfully", section));
    }

    @Operation(summary = "Update section", description = "Update an existing section")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Update section successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> updateSection(@PathVariable String id, @RequestBody UpdateSectionRequestDto request) {
        SectionResponse section = sectionService.updateSection(id, request);
        return ResponseEntity.ok(ApiResponseBase.ok("Update successfully", section));
    }

    @Operation(summary = "Delete section", description = "Delete a section")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Delete section successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSection(@PathVariable String id) {
        SectionResponse section = sectionService.deleteSection(id);
        return ResponseEntity.ok(ApiResponseBase.ok("Delete successfully", section));
    }
}
