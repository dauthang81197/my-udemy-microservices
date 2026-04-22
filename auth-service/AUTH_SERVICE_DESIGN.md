# Auth Service — API Design & System Design

> **Stack:** Spring Boot 3.4.5 · PostgreSQL · Redis · JWT (JJWT) · Liquibase  
> **Base URL:** `/api/auth`

---

## 1. Entity Overview

```
User ──< UserRole >── Role ──< RolePermission >── Permission
User ──< RefreshToken          (multi-device, revocable)
User ──< PasswordResetToken    (one-time, expirable)
User ──< VerificationToken     (one-time, expirable)
```

| Entity | Key Fields |
|---|---|
| `User` | id, email, username, full_name, password, status (PENDING / ACTIVE / BLOCKED) |
| `Role` | id, name, code, description |
| `Permission` | id, name, code, description |
| `UserRole` | userId, roleId, assignedAt |
| `RolePermission` | roleId, permissionId, assignedAt |
| `RefreshToken` | id, userId, token, expiredAt, revoked, **deviceInfo** |
| `PasswordResetToken` | id, userId, token, expiredAt, used |
| `VerificationToken` | id, userId, token, expiredAt, used |

---

## 2. API Design

### 2.1 Register

```
POST /api/auth/register
```

**Request Body**
```json
{
  "email": "user@example.com",
  "username": "johndoe",
  "fullName": "John Doe",
  "password": "P@ssw0rd123"
}
```

**Response 201**
```json
{
  "userId": "uuid",
  "email": "user@example.com",
  "username": "johndoe",
  "message": "Registration successful. Please verify your email."
}
```

**Flow:**
1. Validate input (unique email, password strength)
2. Hash password với BCrypt
3. Create `User` với `status = PENDING`
4. Assign default role `STUDENT` vào `user_roles`
5. Generate `VerificationToken` (UUID, TTL 24h) → lưu DB
6. Publish event `USER_REGISTERED` → Mail Service gửi email xác thực
7. Return 201

**Error Cases:**
- `409 Conflict` — email đã tồn tại
- `400 Bad Request` — validation fail

---

### 2.2 Email Verify

```
GET /api/auth/verify-email?token={token}
```

**Response 200**
```json
{
  "message": "Email verified successfully. You can now log in."
}
```

**Flow:**
1. Lookup `VerificationToken` by token
2. Check `used = false` và `expiredAt > now()`
3. Update `User.status = ACTIVE`
4. Mark `VerificationToken.used = true`
5. Return 200

**Error Cases:**
- `400` — token không tồn tại / đã dùng / hết hạn

```
POST /api/auth/resend-verification
Body: { "email": "user@example.com" }
```
> Tạo lại VerificationToken mới, invalidate token cũ. Rate-limit: 1 request/5 phút.

---

### 2.3 Login

```
POST /api/auth/login
```

**Request Body**
```json
{
  "email": "user@example.com",
  "password": "P@ssw0rd123",
  "deviceInfo": "Chrome/Windows 11"
}
```

**Response 200**
```json
{
  "accessToken": "eyJhbGci...",
  "refreshToken": "uuid-refresh-token",
  "tokenType": "Bearer",
  "expiresIn": 900,
  "user": {
    "id": "uuid",
    "email": "user@example.com",
    "username": "johndoe",
    "fullName": "John Doe",
    "roles": ["STUDENT"],
    "permissions": ["course:read", "lesson:read"]
  }
}
```

**Flow:**
1. Load `User` by email → `CustomUserDetailsService`
2. Verify password via BCrypt
3. Check `User.status == ACTIVE` (PENDING → 403 "verify email first", BLOCKED → 403)
4. Resolve roles + permissions từ `user_roles` → `role_permissions`
5. Generate **Access Token** (JWT, 15 min) với claims: `{sub, userId, roles[], permissions[]}`
6. Generate **Refresh Token** (UUID) → lưu `refresh_tokens` với `deviceInfo`, `expiredAt = now + 7d`
7. Return 200

**Error Cases:**
- `401` — sai password
- `403` — tài khoản chưa verify / bị block
- `429` — quá nhiều lần thử (rate-limit)

---

### 2.4 Refresh Token

```
POST /api/auth/refresh-token
```

**Request Body**
```json
{
  "refreshToken": "uuid-refresh-token"
}
```

**Response 200**
```json
{
  "accessToken": "eyJhbGci...",
  "refreshToken": "new-uuid-refresh-token",
  "expiresIn": 900
}
```

**Flow (Token Rotation):**
1. Lookup `RefreshToken` by token
2. Check `revoked = false` và `expiredAt > now()`
3. Revoke token cũ (`revoked = true`)
4. Generate Access Token mới + Refresh Token mới (same deviceInfo)
5. Return 200

> **Refresh Token Rotation**: mỗi lần refresh, tạo cặp token mới và revoke token cũ → phát hiện token reuse attack.

**Error Cases:**
- `401` — token không hợp lệ / đã revoke / hết hạn

---

### 2.5 Logout

```
POST /api/auth/logout
Authorization: Bearer {accessToken}
```

**Request Body**
```json
{
  "refreshToken": "uuid-refresh-token"
}
```

**Response 200**
```json
{ "message": "Logged out successfully." }
```

**Flow:**
1. Revoke `RefreshToken` cho thiết bị hiện tại (`revoked = true`)
2. Blacklist Access Token trong Redis với TTL = thời gian còn lại của token
3. Return 200

---

### 2.6 Multi-Device Login

#### Danh sách thiết bị đang đăng nhập
```
GET /api/auth/devices
Authorization: Bearer {accessToken}
```

**Response 200**
```json
{
  "devices": [
    {
      "deviceId": "refresh-token-id",
      "deviceInfo": "Chrome/Windows 11",
      "lastUsed": "2025-01-15T08:30:00",
      "current": true
    },
    {
      "deviceId": "refresh-token-id-2",
      "deviceInfo": "Safari/iPhone 15",
      "lastUsed": "2025-01-14T20:00:00",
      "current": false
    }
  ]
}
```

**Flow:** Query tất cả `RefreshToken` của user với `revoked = false` và `expiredAt > now()`.

---

#### Revoke thiết bị cụ thể
```
DELETE /api/auth/devices/{deviceId}
Authorization: Bearer {accessToken}
```

**Flow:** Set `revoked = true` cho `RefreshToken` với id = deviceId và userId = current user.

---

#### Logout tất cả thiết bị
```
DELETE /api/auth/devices
Authorization: Bearer {accessToken}
```

**Flow:** Set `revoked = true` cho tất cả `RefreshToken` của user.

---

### 2.7 Forgot Password

#### Bước 1 — Gửi email reset
```
POST /api/auth/forgot-password
```

**Request Body**
```json
{ "email": "user@example.com" }
```

**Response 200** *(luôn trả 200 để tránh user enumeration)*
```json
{ "message": "If this email exists, a reset link has been sent." }
```

**Flow:**
1. Lookup `User` by email (nếu không tìm thấy → vẫn trả 200)
2. Generate `PasswordResetToken` (UUID, TTL 1h) → lưu DB
3. Publish event → Mail Service gửi link: `{frontendUrl}/reset-password?token={token}`
4. Rate-limit: 3 requests/giờ per email

---

#### Bước 2 — Reset mật khẩu
```
POST /api/auth/reset-password
```

**Request Body**
```json
{
  "token": "uuid-reset-token",
  "newPassword": "NewP@ssw0rd123",
  "confirmPassword": "NewP@ssw0rd123"
}
```

**Response 200**
```json
{ "message": "Password reset successfully. Please log in again." }
```

**Flow:**
1. Lookup `PasswordResetToken` by token
2. Check `used = false` và `expiredAt > now()`
3. Hash password mới → update `User.password`
4. Mark `PasswordResetToken.used = true`
5. Revoke **tất cả** `RefreshToken` của user (force re-login trên mọi thiết bị)
6. Return 200

**Error Cases:**
- `400` — token không hợp lệ / đã dùng / hết hạn
- `400` — password không khớp / không đủ mạnh

---

### 2.8 RBAC Endpoints

#### Assign role cho user
```
POST /api/auth/users/{userId}/roles
Authorization: Bearer {accessToken}  [requires ADMIN]
Body: { "roleCode": "INSTRUCTOR" }
```

#### Revoke role
```
DELETE /api/auth/users/{userId}/roles/{roleCode}
Authorization: Bearer {accessToken}  [requires ADMIN]
```

#### Assign permission cho role
```
POST /api/auth/roles/{roleCode}/permissions
Authorization: Bearer {accessToken}  [requires ADMIN]
Body: { "permissionCode": "course:create" }
```

---

## 3. JWT Design

### Access Token Claims

```json
{
  "sub": "uuid-user-id",
  "email": "user@example.com",
  "userId": "uuid-user-id",
  "roles": ["STUDENT", "INSTRUCTOR"],
  "permissions": ["course:read", "course:create", "lesson:read"],
  "iat": 1700000000,
  "exp": 1700000900
}
```

| Claim | Value | Note |
|---|---|---|
| `sub` | userId (UUID) | Standard JWT subject |
| `roles` | `["STUDENT"]` | Để filter high-level access |
| `permissions` | `["course:read"]` | Để kiểm tra fine-grained |
| TTL | 15 phút | Ngắn để giới hạn exposure window |
| Algorithm | HS256 | Secret key từ env var `JWT_SECRET` |

### Refresh Token Strategy

| Property | Value |
|---|---|
| Format | UUID v4 (opaque, stored in DB) |
| TTL | 7 ngày |
| Storage | `refresh_tokens` table |
| Rotation | Token mới sau mỗi lần refresh |
| Reuse Detection | Token cũ bị revoke → nếu dùng lại → revoke toàn bộ user session |
| Multi-device | Mỗi device = 1 RefreshToken row, identify bởi `deviceInfo` |

---

## 4. System Architecture

```
┌─────────────┐     HTTPS      ┌──────────────────┐
│   Client    │ ─────────────► │   API Gateway    │
│(Web/Mobile) │                │  (rate limiting) │
└─────────────┘                └────────┬─────────┘
                                        │
                               ┌────────▼─────────┐
                               │   Auth Service   │
                               │  (Spring Boot)   │
                               └──┬──────┬────┬───┘
                                  │      │    │
                     ┌────────────▼──┐ ┌─▼──────────┐ ┌──────────────┐
                     │  PostgreSQL   │ │   Redis     │ │ Mail Service │
                     │  (main DB)    │ │ (blacklist) │ │ (SMTP/SES)   │
                     └───────────────┘ └─────────────┘ └──────────────┘
```

### Component Responsibilities

| Component | Trách nhiệm |
|---|---|
| **PostgreSQL** | Persistent storage: users, tokens, roles, permissions |
| **Redis** | Access token blacklist (logout), rate-limit counters, session cache |
| **Mail Service** | Gửi email verify, password reset (async event) |
| **API Gateway** | Rate limiting tổng, SSL termination, route to auth-service |

---

## 5. Security Considerations

### Rate Limiting

| Endpoint | Limit |
|---|---|
| `POST /login` | 5 requests / IP / phút |
| `POST /register` | 3 requests / IP / phút |
| `POST /forgot-password` | 3 requests / email / giờ |
| `POST /resend-verification` | 1 request / email / 5 phút |
| `POST /refresh-token` | 20 requests / user / phút |

### Password Policy
- Minimum 8 ký tự
- Ít nhất 1 chữ hoa, 1 chữ thường, 1 số, 1 ký tự đặc biệt
- Validate bằng regex: `^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@#$%^&+=!]).{8,}$`

### Token Security
- JWT `secret` lưu trong env var, không hardcode
- Access Token blacklist trong Redis khi logout
- Refresh Token: revoke toàn bộ nếu phát hiện reuse attack
- Token không lưu trong `localStorage` (khuyến nghị `httpOnly cookie`)

### User Enumeration Prevention
- `forgot-password` luôn trả `200` dù email không tồn tại
- Login lỗi: trả `401 Invalid credentials` (không nói rõ email hay password sai)

---

## 6. Database Indexes (Liquibase)

```sql
-- Performance indexes đã có trong migration
CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_token ON refresh_tokens(token);
CREATE UNIQUE INDEX idx_users_email ON users(email);
CREATE UNIQUE INDEX idx_roles_code ON roles(code);
CREATE UNIQUE INDEX idx_permissions_code ON permissions(code);

-- Cần thêm
CREATE INDEX idx_refresh_tokens_revoked_expired 
  ON refresh_tokens(user_id, revoked, expired_at);
CREATE INDEX idx_password_reset_tokens_token 
  ON password_reset_tokens(token);
CREATE INDEX idx_verification_tokens_token 
  ON verification_tokens(token);
```

---

## 7. Implementation Order

```
Phase 1 — Core Auth
  [x] Entity setup (done)
  [ ] Register API + input validation
  [ ] Email Verify flow + VerificationToken service
  [ ] Login API + JWT generation
  [ ] Refresh Token API (token rotation)
  [ ] Logout + Redis blacklist

Phase 2 — Security & Multi-Device
  [ ] JwtAuthenticationFilter (check blacklist in Redis)
  [ ] Multi-device: GET/DELETE /devices endpoints
  [ ] Rate limiting (Spring Boot + Redis)
  [ ] Account lockout sau 5 lần login sai

Phase 3 — Password & RBAC
  [ ] Forgot Password + PasswordResetToken service
  [ ] Reset Password + revoke all sessions
  [ ] RBAC: assign/revoke role, permission endpoints
  [ ] @PreAuthorize annotations trên protected endpoints

Phase 4 — Hardening
  [ ] Refresh token reuse detection → revoke all
  [ ] Audit log (ai login từ đâu, lúc nào)
  [ ] Integration tests cho toàn bộ auth flows
```

---

## 8. Default RBAC Seed Data

```sql
-- Roles
INSERT INTO roles (code, name) VALUES 
  ('ADMIN', 'Administrator'),
  ('INSTRUCTOR', 'Instructor'),
  ('STUDENT', 'Student');

-- Permissions
INSERT INTO permissions (code, name) VALUES
  ('course:create', 'Create Course'),
  ('course:read',   'Read Course'),
  ('course:update', 'Update Course'),
  ('course:delete', 'Delete Course'),
  ('lesson:create', 'Create Lesson'),
  ('lesson:read',   'Read Lesson'),
  ('lesson:update', 'Update Lesson'),
  ('lesson:delete', 'Delete Lesson'),
  ('user:manage',   'Manage Users');

-- Role-Permission mapping
-- ADMIN: all permissions
-- INSTRUCTOR: course CRUD + lesson CRUD
-- STUDENT: course:read + lesson:read
```

---

## 9. API Response Format

```json
// Success
{
  "success": true,
  "data": { ... },
  "message": "...",
  "timestamp": "2025-01-15T08:30:00"
}

// Error
{
  "success": false,
  "error": {
    "code": "INVALID_CREDENTIALS",
    "message": "Invalid email or password.",
    "details": []
  },
  "timestamp": "2025-01-15T08:30:00"
}
```

---

## 10. Environment Variables

```env
# JWT
JWT_SECRET=your-256-bit-secret-key
JWT_ACCESS_TOKEN_TTL=900        # 15 phút (giây)
JWT_REFRESH_TOKEN_TTL=604800    # 7 ngày (giây)

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379

# Mail
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=noreply@example.com
MAIL_PASSWORD=app-password

# Frontend (cho email links)
FRONTEND_URL=http://localhost:3000
```
