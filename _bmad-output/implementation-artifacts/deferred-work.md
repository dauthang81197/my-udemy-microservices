# Deferred Work

## From: auto-setup-course-from-videos (2026-05-30)

- **@ControllerAdvice global exception handler**: RuntimeException hiện trả về Spring default error body thay vì `ApiResponseBase` envelope. Cần thêm `@RestControllerAdvice` cho course-service để tất cả errors có format nhất quán.
- **Idempotency cho auto-setup**: Gọi endpoint lần 2 tạo duplicate sections/lessons. Cân nhắc thêm check "course đã có sections?" và reject hoặc merge.
- **Giới hạn videoIds**: Không có upper-bound validation. Cân nhắc thêm `@Size(max=500)` trên request DTO.
- **Ownership check trên VideoFile**: `findAllById` không kiểm tra video thuộc course nào. Video từ course khác có thể bị link vào course hiện tại.

## From: link-video-upload-to-course (2026-06-06)

- **R2 orphans trong uploadVideos loop**: Không có `@Transactional` — nếu DB save thất bại sau khi R2 upload thành công, object R2 bị orphan. Pre-existing pattern, cần cleanup strategy (scheduled job hoặc transactional outbox).
- **publicUrl double slash**: `r2Key = "/video" + UUID...` rồi `publicUrlBase + "/" + r2Key` → URL có double slash. Pre-existing bug, cần fix riêng.
- **nameSection vs name param inconsistency**: `VideoFileController` (user-facing) dùng `@RequestParam("name")`, admin dùng `@RequestParam("nameSection")`. Pre-existing inconsistency.
- **`@Transactional` còn thiếu trên VideoFileServiceImpl**: Read methods chưa có `@Transactional(readOnly = true)`. Pre-existing pattern chưa được áp dụng.
