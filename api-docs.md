# API Documentation

## Tổng quan

| | URL |
|---|---|
| **Gateway (dùng cho FE)** | `http://localhost:8000` |
| Auth Service (direct) | `http://localhost:8080` |
| Course Service (direct) | `http://localhost:8081` |

**Tất cả request nên đi qua Gateway.** Prefix route:
- Auth Service: `/api/auth-service`
- Course Service: `/api/course-service`

---

## Authentication

Các endpoint của course-service đều yêu cầu JWT token trong header:

```
Authorization: Bearer <accessToken>
```

---

## Response Format chung

**Single object:**
```json
{
  "success": true,
  "message": "...",
  "data": { ... }
}
```

**Lỗi:**
```json
{
  "success": false,
  "message": "...",
  "errors": { ... }
}
```

**Danh sách (Pagination):**
```json
{
  "data": [ ... ],
  "page": 0,
  "size": 10,
  "totalItems": 100,
  "totalPages": 10,
  "hasNext": true,
  "hasPrevious": false
}
```

---

## Enum Values

| Enum | Giá trị |
|---|---|
| `level` | `BEGINNER` / `INTERMEDIATE` / `ADVANCED` |
| `courseStatus` | `DRAFT` / `PUBLISHED` |
| `lessonType` | `VIDEO` / `FILE` / `QUIZ` / `ARTICLE` |
| `userStatus` | `ACTIVE` / `BLOCKED` / `PENDING` |

---

# AUTH SERVICE

Base: `http://localhost:8000/api/auth-service`

> Không cần `Authorization` header cho 2 endpoint này.

---

### POST `/login`

Đăng nhập, nhận JWT token.

**Request Body:**
```json
{
  "email": "thangdau811@gmail.com",
  "password": "Admin@123"
}
```

**Response `200`:**
```json
{
  "success": true,
  "message": "Login successfully",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
    "userProfile": {
      "id": "uuid",
      "username": "thangdau",
      "email": "thangdau811@gmail.com",
      "full_name": "Thang Dau",
      "status": "ACTIVE",
      "createdAt": "2025-01-01T00:00:00",
      "updatedAt": "2025-01-01T00:00:00"
    }
  }
}
```

**Response `401`:** Sai email hoặc password.

---

### POST `/register`

Đăng ký tài khoản mới.

**Request Body:**
```json
{
  "email": "user@gmail.com",
  "username": "myusername",
  "password": "Admin@123",
  "fullName": "Nguyen Van A"
}
```

**Response `200`:**
```json
{
  "success": true,
  "message": "Login successfully",
  "data": {
    "username": "myusername",
    "email": "user@gmail.com"
  }
}
```

---

# COURSE SERVICE

Base: `http://localhost:8000/api/course-service`

> Tất cả endpoint bên dưới đều cần `Authorization: Bearer <token>`.

---

## Courses — `/admin/courses`

### GET `/admin/courses`

Lấy danh sách course (có phân trang).

**Query params (tất cả optional):**

| Param | Type | Default | Mô tả |
|---|---|---|---|
| `page` | int | `0` | Trang hiện tại (bắt đầu từ 0) |
| `size` | int | `10` | Số item mỗi trang |
| `sort` | string | `title,asc` | Sắp xếp, ví dụ `title,desc` |

**Response `200`:**
```json
{
  "data": [
    {
      "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
      "title": "Java Spring Boot",
      "description": "Học Spring Boot từ cơ bản đến nâng cao",
      "level": "BEGINNER",
      "status": "DRAFT"
    }
  ],
  "page": 0,
  "size": 10,
  "totalItems": 1,
  "totalPages": 1,
  "hasNext": false,
  "hasPrevious": false
}
```

---

### GET `/admin/courses:id?id={courseId}`

Lấy chi tiết một course.

> ⚠️ URL pattern đặc biệt: path là `:id` (không phải `/{id}`), `id` truyền qua query param.

**URL ví dụ:** `GET /admin/courses:id?id=3fa85f64-5717-4562-b3fc-2c963f66afa6`

**Response `200`:**
```json
{
  "success": true,
  "message": "Login successfully",
  "data": {
    "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "title": "Java Spring Boot",
    "description": "Học Spring Boot từ cơ bản đến nâng cao",
    "level": "BEGINNER",
    "status": "DRAFT"
  }
}
```

---

### POST `/admin/courses`

Tạo course mới.

**Request Body:**
```json
{
  "title": "Java Spring Boot",
  "description": "Học Spring Boot từ cơ bản đến nâng cao",
  "level": "BEGINNER"
}
```

**Response `200`:**
```json
{
  "success": true,
  "message": "Login successfully",
  "data": {
    "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "title": "Java Spring Boot",
    "description": "Học Spring Boot từ cơ bản đến nâng cao",
    "level": "BEGINNER",
    "status": "DRAFT"
  }
}
```

---

### PUT `/admin/courses:id?id={courseId}`

Cập nhật course.

> ⚠️ Cùng URL pattern đặc biệt như GET.

**URL ví dụ:** `PUT /admin/courses:id?id=3fa85f64-5717-4562-b3fc-2c963f66afa6`

**Request Body:**
```json
{
  "title": "Java Spring Boot Updated",
  "description": "Mô tả mới",
  "level": "INTERMEDIATE"
}
```

**Response `200`:**
```json
{
  "success": true,
  "message": "Update successfully",
  "data": {
    "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "title": "Java Spring Boot Updated",
    "description": "Mô tả mới",
    "level": "INTERMEDIATE",
    "status": "DRAFT"
  }
}
```

---

### DELETE `/admin/courses:id?id={courseId}`

Xoá course. Sẽ lỗi nếu course còn section bên trong.

**URL ví dụ:** `DELETE /admin/courses:id?id=3fa85f64-5717-4562-b3fc-2c963f66afa6`

**Response `200`:** Trả về object course vừa bị xoá (cùng format CourseResponse).

---

## Sections — `/admin/sections`

### GET `/admin/sections`

Lấy danh sách section (có phân trang). Query params giống Courses.

**Response item:**
```json
{
  "id": "uuid",
  "title": "Chương 1: Giới thiệu"
}
```

---

### GET `/admin/sections:id?id={sectionId}`

Lấy chi tiết một section.

**URL ví dụ:** `GET /admin/sections:id?id=uuid`

**Response `200`:**
```json
{
  "success": true,
  "message": "Login successfully",
  "data": {
    "id": "uuid",
    "title": "Chương 1: Giới thiệu"
  }
}
```

---

### POST `/admin/sections`

Tạo section mới — phải thuộc một course có sẵn.

**Request Body:**
```json
{
  "title": "Chương 1: Giới thiệu",
  "courseId": "3fa85f64-5717-4562-b3fc-2c963f66afa6"
}
```

**Response `200`:**
```json
{
  "success": true,
  "message": "Login successfully",
  "data": {
    "id": "uuid",
    "title": "Chương 1: Giới thiệu"
  }
}
```

---

### PUT `/admin/sections:id?id={sectionId}`

Cập nhật section.

**URL ví dụ:** `PUT /admin/sections:id?id=uuid`

**Request Body:**
```json
{
  "title": "Chương 1 Updated",
  "courseId": "3fa85f64-5717-4562-b3fc-2c963f66afa6"
}
```

**Response `200`:**
```json
{
  "success": true,
  "message": "Update successfully",
  "data": {
    "id": "uuid",
    "title": "Chương 1 Updated"
  }
}
```

---

### DELETE `/admin/sections:id?id={sectionId}`

Xoá section.

**URL ví dụ:** `DELETE /admin/sections:id?id=uuid`

**Response `200`:** Trả về object section vừa bị xoá.

---

## Lessons — `/admin/lessons`

### GET `/admin/lessons`

Lấy danh sách lesson (có phân trang). Query params giống Courses.

**Response item:**
```json
{
  "id": "uuid",
  "title": "Bài 1: Hello World",
  "description": "Giới thiệu bài học",
  "type": "VIDEO",
  "videoUrl": "https://example.com/video.mp4",
  "isPreview": false,
  "sortOrder": 1,
  "status": "DRAFT"
}
```

---

### GET `/admin/lessons:id?id={lessonId}`

Lấy chi tiết một lesson.

**URL ví dụ:** `GET /admin/lessons:id?id=uuid`

**Response `200`:**
```json
{
  "success": true,
  "message": "Login successfully",
  "data": {
    "id": "uuid",
    "title": "Bài 1: Hello World",
    "description": "Giới thiệu bài học",
    "type": "VIDEO",
    "videoUrl": "https://example.com/video.mp4",
    "isPreview": false,
    "sortOrder": 1,
    "status": "DRAFT"
  }
}
```

---

### POST `/admin/lessons`

Tạo lesson mới — phải thuộc một section có sẵn.

**Request Body:**
```json
{
  "sectionId": "uuid-của-section",
  "title": "Bài 1: Hello World",
  "description": "Giới thiệu bài học",
  "type": "VIDEO",
  "videoUrl": "https://example.com/video.mp4",
  "isPreview": false,
  "sortOrder": 1
}
```

| Field | Type | Bắt buộc | Mô tả |
|---|---|---|---|
| `sectionId` | UUID | Có | ID của section chứa lesson này |
| `title` | string | Có | Tiêu đề lesson |
| `description` | string | Không | Mô tả lesson |
| `type` | enum | Không | `VIDEO` / `FILE` / `QUIZ` / `ARTICLE` |
| `videoUrl` | string | Không | URL video (nếu type là VIDEO) |
| `isPreview` | boolean | Không | Lesson có được xem thử miễn phí không |
| `sortOrder` | int | Không | Thứ tự hiển thị trong section |

**Response `200`:**
```json
{
  "success": true,
  "message": "Login successfully",
  "data": {
    "id": "uuid",
    "title": "Bài 1: Hello World",
    "description": "Giới thiệu bài học",
    "type": "VIDEO",
    "videoUrl": "https://example.com/video.mp4",
    "isPreview": false,
    "sortOrder": 1,
    "status": "DRAFT"
  }
}
```

---

### PUT `/admin/lessons:id?id={lessonId}`

Cập nhật lesson.

**URL ví dụ:** `PUT /admin/lessons:id?id=uuid`

**Request Body:** (không có `sectionId` — không thể chuyển lesson sang section khác)
```json
{
  "title": "Bài 1 Updated",
  "description": "Mô tả mới",
  "type": "VIDEO",
  "videoUrl": "https://example.com/new-video.mp4",
  "isPreview": true,
  "sortOrder": 2
}
```

**Response `200`:**
```json
{
  "success": true,
  "message": "Update successfully",
  "data": {
    "id": "uuid",
    "title": "Bài 1 Updated",
    "description": "Mô tả mới",
    "type": "VIDEO",
    "videoUrl": "https://example.com/new-video.mp4",
    "isPreview": true,
    "sortOrder": 2,
    "status": "DRAFT"
  }
}
```

---

### DELETE `/admin/lessons:id?id={lessonId}`

Xoá lesson.

**URL ví dụ:** `DELETE /admin/lessons:id?id=uuid`

**Response `200`:** Trả về object lesson vừa bị xoá.

---

## Lưu ý quan trọng

> **URL pattern `:id`** — Tất cả endpoint GET/PUT/DELETE single item đang dùng `@GetMapping(":id")` thay vì `@GetMapping("/{id}")`. Điều này có nghĩa là URL thực tế là `/admin/courses:id?id=<uuid>` chứ **không phải** `/admin/courses/<uuid>`. Đây là một bug trong code nên cần thống nhất với BE để sửa thành path variable `/{id}` cho chuẩn REST, tránh FE phải dùng URL bất thường này.