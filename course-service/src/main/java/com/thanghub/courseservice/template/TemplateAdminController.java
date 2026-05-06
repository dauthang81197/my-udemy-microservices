package com.thanghub.courseservice.template;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("admin/templates")
@RequiredArgsConstructor
@Tag(name = "Template")
public class TemplateAdminController {

    private final ExcelTemplateService excelTemplateService;

    @Operation(summary = "Download course import template")
    @GetMapping("/courses")
    public ResponseEntity<byte[]> downloadCourseTemplate() throws IOException {
        byte[] file = excelTemplateService.buildCourseTemplate();
        return buildResponse(file, "template_courses.xlsx");
    }

    @Operation(summary = "Download section import template")
    @GetMapping("/courses/{courseId}/sections")
    public ResponseEntity<byte[]> downloadSectionTemplate(@PathVariable String courseId) throws IOException {
        byte[] file = excelTemplateService.buildSectionTemplate(courseId);
        return buildResponse(file, "template_sections.xlsx");
    }

    @Operation(summary = "Download lesson import template")
    @GetMapping("/lessons")
    public ResponseEntity<byte[]> downloadLessonTemplate() throws IOException {
        byte[] file = excelTemplateService.buildLessonTemplate();
        return buildResponse(file, "template_lessons.xlsx");
    }

    private ResponseEntity<byte[]> buildResponse(byte[] file, String filename) {
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(file);
    }
}
