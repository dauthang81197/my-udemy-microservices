---
title: 'Auto-setup course structure from video naming convention'
type: 'feature'
created: '2026-05-30'
status: 'done'
baseline_commit: '23a7979'
context:
  - '{project-root}/_bmad-output/project-context.md'
---

<frozen-after-approval reason="human-owned intent — do not modify unless human renegotiates">

## Intent

**Problem:** Tạo khóa học với nhiều sections/lessons (ví dụ 8×10=80 lessons) yêu cầu hàng trăm API calls thủ công — tạo course → từng section → từng lesson → link video — rất tốn thời gian.

**Approach:** Thêm endpoint `POST /admin/courses/{courseId}/auto-setup` nhận danh sách video IDs đã upload, tự parse `originalFilename` theo naming convention để tạo sections + lessons + link video trong một transaction.

## Boundaries & Constraints

**Always:**
- Naming convention bắt buộc: `{N}-{Section Name}/{M}-{Lesson Name}.{ext}` (N, M là số nguyên dương)
- Hyphens trong tên section/lesson được chuyển thành spaces cho display
- Lesson type mặc định `VIDEO`, `isPreview` mặc định `false`, `status` mặc định `DRAFT`
- Toàn bộ thao tác trong một `@Transactional` — fail một chỗ thì rollback hết
- Video không match convention → ghi vào `errors` list, KHÔNG throw exception, xử lý tiếp các video khác
- Build entity trực tiếp qua Repository, không gọi qua SectionService/LessonService (tránh N+1 lookups)

**Ask First:**
- Nếu course đã có sections: hiện tại spec tạo mới sections, có thể tạo duplicate — nếu cần merge với structure hiện có thì HALT và hỏi

**Never:**
- Không tự upload video — endpoint này chỉ nhận videoIds đã upload sẵn
- Không xóa/sửa section hoặc lesson hiện có của course
- Không tạo Liquibase changeset mới — không có schema change

## I/O & Edge-Case Matrix

| Scenario | Input | Expected Output | Error Handling |
|---|---|---|---|
| Happy path | `courseId` valid, 80 videoIds match convention | `sectionsCreated=8`, `lessonsCreated=80`, `errors=[]` | N/A |
| 1 video sai convention | filename `no-slash-here.mp4` | video đó vào `errors`, các video khác vẫn tạo bình thường | ghi `"no-slash-here.mp4: invalid filename format"` |
| videoId không tồn tại | UUID không có trong DB | bỏ qua, ghi vào errors | `"<uuid>: video not found"` |
| courseId không tồn tại | UUID không có trong DB | throw `RuntimeException("Course not found")` → 500 | controller không wrap — dùng GlobalExceptionHandler |
| videoIds rỗng | `[]` | `sectionsCreated=0`, `lessonsCreated=0`, `errors=[]` | trả về OK với 0s |
| Tất cả videos sai convention | tất cả không match | `sectionsCreated=0`, `lessonsCreated=0`, `errors=[...]` | trả về OK với errors |

</frozen-after-approval>

## Code Map

- `course-service/src/main/java/com/thanghub/courseservice/course/CourseAdminController.java` — thêm endpoint `POST /{courseId}/auto-setup`
- `course-service/src/main/java/com/thanghub/courseservice/course/CourseService.java` — thêm method `autoSetupFromVideos`
- `course-service/src/main/java/com/thanghub/courseservice/course/CourseServiceImpl.java` — implement method, inject `VideoFileRepository` + `LessonRepository`
- `course-service/src/main/java/com/thanghub/courseservice/course/request/AutoSetupRequestDto.java` — NEW: `List<UUID> videoIds`
- `course-service/src/main/java/com/thanghub/courseservice/course/response/AutoSetupResultDto.java` — NEW: `int sectionsCreated, int lessonsCreated, List<String> errors`
- `course-service/src/main/java/com/thanghub/courseservice/media/VideoFileRepository.java` — dùng `findAllById(videoIds)` có sẵn
- `course-service/src/main/java/com/thanghub/courseservice/section/SectionRepository.java` — dùng `save()` có sẵn
- `course-service/src/main/java/com/thanghub/courseservice/lesson/LessonRepository.java` — dùng `save()` có sẵn
- `course-service/src/main/java/com/thanghub/courseservice/lesson/Lesson.java` — entity, chú ý field `is_preview`, `sort_order` (snake_case Lombok setters)

## Tasks & Acceptance

**Execution:**
- [x] `course/request/AutoSetupRequestDto.java` -- CREATE -- DTO nhận `List<UUID> videoIds`
- [x] `course/response/AutoSetupResultDto.java` -- CREATE -- DTO trả về `sectionsCreated`, `lessonsCreated`, `List<String> errors`
- [x] `course/CourseService.java` -- ADD METHOD -- `AutoSetupResultDto autoSetupFromVideos(String courseId, List<UUID> videoIds)`
- [x] `course/CourseServiceImpl.java` -- IMPLEMENT -- inject `VideoFileRepository` + `LessonRepository`; parse filenames với regex `^(\d+)-(.+)/(\d+)-(.+)\.[^.]+$`; group by section order; tạo Section entities → save; tạo Lesson entities linked to section + video → save; return result
- [x] `course/CourseAdminController.java` -- ADD ENDPOINT -- `POST /admin/courses/{courseId}/auto-setup`, nhận `@RequestBody AutoSetupRequestDto`, trả về `ApiResponseBase<AutoSetupResultDto>`

**Acceptance Criteria:**
- Given course tồn tại và 80 videoIds đúng convention, when `POST /admin/courses/{id}/auto-setup`, then response có `success=true`, `data.sectionsCreated=8`, `data.lessonsCreated=80`, `data.errors=[]`
- Given 1 trong số videoIds có filename sai convention, when gọi endpoint, then video đó xuất hiện trong `data.errors`, các videos khác vẫn được tạo bình thường
- Given courseId không tồn tại, when gọi endpoint, then trả về 500 với message "Course not found"
- Given `videoIds=[]`, when gọi endpoint, then trả về `success=true` với `sectionsCreated=0`, `lessonsCreated=0`

## Design Notes

**Filename parsing logic (critical):**
```
regex: ^(\d+)-(.+)/(\d+)-(.+)\.[^.]+$

"01-Introduction/02-Setup-Environment.mp4"
  group 1 → "01"             → sectionSort = 1
  group 2 → "Introduction"   → sectionTitle = "Introduction"
  group 3 → "02"             → lessonSort = 2
  group 4 → "Setup-Environment" → lessonTitle = "Setup Environment" (hyphens → spaces)
```

Group by `sectionSort` để xác định unique sections. Trong trường hợp cùng `sectionSort` nhưng `sectionTitle` khác nhau → dùng title của video đầu tiên trong nhóm (không cần báo lỗi, đây là user error).

**Lesson entity quirk:** Field `is_preview` và `sort_order` dùng snake_case. Lombok setter là `setIs_preview(Boolean)` và `setSort_order(Integer)`.

## Verification

**Commands:**
- `./mvnw clean verify -pl course-service -am -DskipTests` -- expected: BUILD SUCCESS
- `./mvnw test -pl course-service` -- expected: BUILD SUCCESS, no test failures

## Suggested Review Order

**Entry point — core parsing & persistence logic**

- Toàn bộ method `autoSetupFromVideos`: regex parse → group by section → save entities
  [`CourseServiceImpl.java:232`](../../course-service/src/main/java/com/thanghub/courseservice/course/CourseServiceImpl.java#L232)

- Regex `[^/]+` thay vì `.+` ngăn crossing separators; duplicate lessonOrder → errors
  [`CourseServiceImpl.java:249`](../../course-service/src/main/java/com/thanghub/courseservice/course/CourseServiceImpl.java#L249)

- Section + Lesson entity creation trực tiếp qua Repository (không qua Service layer)
  [`CourseServiceImpl.java:294`](../../course-service/src/main/java/com/thanghub/courseservice/course/CourseServiceImpl.java#L294)

**API surface**

- Endpoint `POST /admin/courses/{id}/auto-setup`
  [`CourseAdminController.java:107`](../../course-service/src/main/java/com/thanghub/courseservice/course/CourseAdminController.java#L107)

- Interface method signature
  [`CourseService.java:35`](../../course-service/src/main/java/com/thanghub/courseservice/course/CourseService.java#L35)

**Types**

- Request DTO: `List<UUID> videoIds`
  [`AutoSetupRequestDto.java:1`](../../course-service/src/main/java/com/thanghub/courseservice/course/request/AutoSetupRequestDto.java#L1)

- Response DTO: `sectionsCreated`, `lessonsCreated`, `errors`
  [`AutoSetupResultDto.java:1`](../../course-service/src/main/java/com/thanghub/courseservice/course/response/AutoSetupResultDto.java#L1)

## Spec Change Log
