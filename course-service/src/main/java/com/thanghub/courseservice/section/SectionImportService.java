package com.thanghub.courseservice.section;

import com.thanghub.courseservice.course.CourseRepository;
import com.thanghub.courseservice.course.Course;
import com.thanghub.courseservice.section.response.SectionImportResultDto;
import com.thanghub.courseservice.section.response.SectionImportResultDto.RowError;
import com.thanghub.courseservice.section.response.SectionResponse;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SectionImportService {

    private final CourseRepository courseRepository;
    private final SectionRepository sectionRepository;

    public SectionImportResultDto importFromExcel(String courseId, MultipartFile file) throws IOException {
        Course course = courseRepository.findById(UUID.fromString(courseId))
                .orElseThrow(() -> new RuntimeException("Course not found: " + courseId));

        List<SectionResponse> imported = new ArrayList<>();
        List<RowError> errors = new ArrayList<>();

        try (Workbook wb = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = wb.getSheet("sections");
            if (sheet == null) {
                throw new IllegalArgumentException("File không đúng định dạng: không tìm thấy sheet 'sections'");
            }

            // row 0 là header, bắt đầu từ row 1
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (isEmptyRow(row)) continue;

                String sectionName = getCellText(row, 0);

                if (sectionName.isBlank()) {
                    errors.add(RowError.builder()
                            .row(i + 1)
                            .sectionName("")
                            .reason("Section Name không được để trống")
                            .build());
                    continue;
                }

                Section section = sectionRepository.save(
                        Section.builder()
                                .title(sectionName)
                                .course(course)
                                .build()
                );

                imported.add(SectionResponse.builder()
                        .id(section.getId())
                        .title(section.getTitle())
                        .build());
            }
        }

        return SectionImportResultDto.builder()
                .totalRows(imported.size() + errors.size())
                .successCount(imported.size())
                .errorCount(errors.size())
                .imported(imported)
                .errors(errors)
                .build();
    }

    private String getCellText(Row row, int col) {
        Cell cell = row.getCell(col, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) return "";
        cell.setCellType(CellType.STRING);
        String value = cell.getStringCellValue();
        return value == null ? "" : value.trim();
    }

    private boolean isEmptyRow(Row row) {
        if (row == null) return true;
        for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c);
            if (cell != null && cell.getCellType() != CellType.BLANK) return false;
        }
        return true;
    }
}
