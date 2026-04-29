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
@RequestMapping("admin/sections")
@RequiredArgsConstructor
@Tag(name = "Section")
public class SectionAdminController {
    private final SectionService sectionService;

    @Operation(
            summary = "Section",
            description = "Return Section List"
    )
    @GetMapping()
    public PaginationResponse<SectionResponse> getSections(@PageableDefault(page = 0, size = 10, sort = "title", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<SectionResponse> page = sectionService.getSections(pageable);
        return PaginationMapper.from(page);
    }

    @Operation(
            summary = "Section",
            description = "Return Section"
    )
    @GetMapping(":id")
    public ResponseEntity<?> getSections(@RequestParam("id") String id) {
        SectionResponse Section = sectionService.getSection(id);
        return ResponseEntity.ok(ApiResponseBase.ok("Login successfully", Section));
    }

    @Operation(
            summary = "Section",
            description = "Return Section"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Create Section successfully"),
            @ApiResponse(responseCode = "401", description = "Check token and role")
    })
    @PostMapping()
    public ResponseEntity<?> createSection(CreateSectionRequestDto request) {
        SectionResponse createSectionRequestDto = sectionService.createSection(request);
        return ResponseEntity.ok(ApiResponseBase.ok("Login successfully", createSectionRequestDto));
    }

    @Operation(
            summary = "Section",
            description = "Return Section "
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Update Section successfully"),
            @ApiResponse(responseCode = "401", description = "Check token and role")
    })
    @PutMapping(":id")
    public ResponseEntity<?> updateSection(@RequestParam("id") String id, UpdateSectionRequestDto request) {
        SectionResponse createSectionRequestDto = sectionService.updateSection(id, request);
        return ResponseEntity.ok(ApiResponseBase.ok("Update successfully", createSectionRequestDto));
    }

    @Operation(
            summary = "Section",
            description = "Return Section "
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Update Section successfully"),
            @ApiResponse(responseCode = "401", description = "Check token and role")
    })
    @DeleteMapping(":id")
    public ResponseEntity<?> deleteSection(@RequestParam("id") String id) {
        SectionResponse createSectionRequestDto = sectionService.deleteSection(id);
        return ResponseEntity.ok(ApiResponseBase.ok("Delete successfully", createSectionRequestDto));
    }
}
