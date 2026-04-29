package com.thanghub.courseservice.template;

import com.thanghub.courseservice.course.Course;
import com.thanghub.courseservice.course.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ExcelTemplateService {
    private final CourseRepository courseRepository;

    public byte[] buildCourseTemplate() throws IOException {
        List<String> headers = List.of("title", "description", "level");
        List<List<String>> examples = List.of(
                List.of("Java Spring Boot", "Khóa học Spring Boot từ cơ bản đến nâng cao", "BEGINNER"),
                List.of("React Advanced", "Khóa học React nâng cao với TypeScript", "INTERMEDIATE")
        );
        String[] levelValues = {"BEGINNER", "INTERMEDIATE", "ADVANCED"};
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("courses");
            writeHeaders(wb, sheet, headers);
            writeExamples(wb, sheet, examples);
            addDropdown(sheet, 2, levelValues, examples.size() + 1, 100);
            autoSizeColumns(sheet, headers.size());
            return toBytes(wb);
        }
    }

    public byte[] buildSectionTemplate(String courseId) throws IOException {
        // Check course
        Course course = courseRepository.findById(UUID.fromString(courseId)).orElseThrow(() -> new RuntimeException("Course not found"));


        List<String> headers = List.of("Section Name", "Course Name");
        List<List<String>> examples = List.of(
                List.of("Section 1: Basics of Web Development and Internet Pre Spring Boot", course.getTitle())
        );
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("sections");
            writeHeaders(wb, sheet, headers);
            writeExamples(wb, sheet, examples);
            autoSizeColumns(sheet, headers.size());
            return toBytes(wb);
        }
    }

    public byte[] buildLessonTemplate() throws IOException {
        List<String> headers = List.of(
                "sectionId", "title", "description", "type",
                "videoUrl", "isPreview", "sortOrder"
        );
        List<List<String>> examples = List.of(
                List.of(
                        "a1b2c3d4-0000-0000-0000-000000000001",
                        "Bài 1: Hello World",
                        "Giới thiệu bài học đầu tiên",
                        "VIDEO",
                        "https://example.com/video1.mp4",
                        "false",
                        "1"
                ),
                List.of(
                        "a1b2c3d4-0000-0000-0000-000000000001",
                        "Bài 2: Biến và kiểu dữ liệu",
                        "Tìm hiểu về biến trong Java",
                        "VIDEO",
                        "https://example.com/video2.mp4",
                        "true",
                        "2"
                )
        );
        String[] typeValues = {"VIDEO", "FILE", "QUIZ", "ARTICLE"};
        String[] previewValues = {"true", "false"};
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("lessons");
            writeHeaders(wb, sheet, headers);
            writeExamples(wb, sheet, examples);
            // col 3 = type, col 5 = isPreview
            addDropdown(sheet, 3, typeValues, examples.size() + 1, 100);
            addDropdown(sheet, 5, previewValues, examples.size() + 1, 100);
            autoSizeColumns(sheet, headers.size());
            return toBytes(wb);
        }
    }

    private void writeHeaders(XSSFWorkbook wb, Sheet sheet, List<String> headers) {
        CellStyle headerStyle = wb.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.CORNFLOWER_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setBorderBottom(BorderStyle.THIN);

        Font font = wb.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        headerStyle.setFont(font);

        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.size(); i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers.get(i));
            cell.setCellStyle(headerStyle);
        }
    }

    private void writeExamples(XSSFWorkbook wb, Sheet sheet, List<List<String>> examples) {
        CellStyle exampleStyle = wb.createCellStyle();
        exampleStyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
        exampleStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        Font grayFont = wb.createFont();
        grayFont.setColor(IndexedColors.GREY_50_PERCENT.getIndex());
        exampleStyle.setFont(grayFont);

        for (int r = 0; r < examples.size(); r++) {
            Row row = sheet.createRow(r + 1);
            List<String> values = examples.get(r);
            for (int c = 0; c < values.size(); c++) {
                Cell cell = row.createCell(c);
                cell.setCellValue(values.get(c));
                cell.setCellStyle(exampleStyle);
            }
        }
    }

    private void addDropdown(Sheet sheet, int colIndex, String[] values, int firstDataRow, int lastDataRow) {
        XSSFDataValidationHelper dvHelper = new XSSFDataValidationHelper((XSSFSheet) sheet);
        XSSFDataValidationConstraint constraint =
                (XSSFDataValidationConstraint) dvHelper.createExplicitListConstraint(values);
        CellRangeAddressList range = new CellRangeAddressList(firstDataRow, lastDataRow, colIndex, colIndex);
        XSSFDataValidation validation = (XSSFDataValidation) dvHelper.createValidation(constraint, range);
//        validation.setShowDropDown(false);
        sheet.addValidationData(validation);
    }

    private void autoSizeColumns(Sheet sheet, int count) {
        for (int i = 0; i < count; i++) {
            sheet.autoSizeColumn(i);
            sheet.setColumnWidth(i, Math.max(sheet.getColumnWidth(i), 5000));
        }
    }

    private byte[] toBytes(XSSFWorkbook wb) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        wb.write(out);
        return out.toByteArray();
    }
}
