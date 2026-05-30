# Deferred Work

## From: auto-setup-course-from-videos (2026-05-30)

- **@ControllerAdvice global exception handler**: RuntimeException hiện trả về Spring default error body thay vì `ApiResponseBase` envelope. Cần thêm `@RestControllerAdvice` cho course-service để tất cả errors có format nhất quán.
- **Idempotency cho auto-setup**: Gọi endpoint lần 2 tạo duplicate sections/lessons. Cân nhắc thêm check "course đã có sections?" và reject hoặc merge.
- **Giới hạn videoIds**: Không có upper-bound validation. Cân nhắc thêm `@Size(max=500)` trên request DTO.
- **Ownership check trên VideoFile**: `findAllById` không kiểm tra video thuộc course nào. Video từ course khác có thể bị link vào course hiện tại.
