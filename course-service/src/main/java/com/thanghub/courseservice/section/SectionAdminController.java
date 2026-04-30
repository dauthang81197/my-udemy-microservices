package com.thanghub.courseservice.section;

import com.thanghub.common.ApiResponseBase;
import com.thanghub.common.mapper.PaginationMapper;
import com.thanghub.common.response.PaginationResponse;
import com.thanghub.courseservice.section.request.CreateSectionRequestDto;
import com.thanghub.courseservice.section.request.UpdateSectionRequestDto;
import com.thanghub.courseservice.section.response.SectionImportResultDto;
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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("admin/sections")
@RequiredArgsConstructor
@Tag(name = "Section")
public class SectionAdminController {
    private final SectionService sectionService;
    private final SectionImportService sectionImportService;

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
    @GetMapping("/{id}")
    public ResponseEntity<?> getSections(@PathVariable String id) {
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
    public ResponseEntity<?> createSection(@RequestBody CreateSectionRequestDto request) {
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
    @PutMapping("/{id}")
    public ResponseEntity<?> updateSection(@PathVariable String id, @RequestBody UpdateSectionRequestDto request) {
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
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSection(@PathVariable String id) {
        SectionResponse createSectionRequestDto = sectionService.deleteSection(id);
        return ResponseEntity.ok(ApiResponseBase.ok("Delete successfully", createSectionRequestDto));
    }

    @Operation(
            summary = "Import sections from Excel",
            description = "Upload file .xlsx theo template đã download. Trả về kết quả import gồm số dòng thành công và danh sách lỗi."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Import completed (có thể có lỗi một số dòng)"),
            @ApiResponse(responseCode = "400", description = "File không đúng định dạng")
    })
    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> importSections(
            @RequestParam("courseId") String courseId,
            @RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponseBase.fail("File không được để trống"));
        }
        SectionImportResultDto result = sectionImportService.importFromExcel(courseId, file);
        return ResponseEntity.ok(ApiResponseBase.ok("Import completed", result));
    }
}
