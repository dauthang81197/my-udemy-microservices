# Deployment Plan — VPS + Docker Swarm + GitHub Actions

## Tại sao Docker Swarm?

**Nguyên tắc cốt lõi: Build 1 image — Deploy nhiều môi trường.**

```
1 Docker Image (ghcr.io/org/auth-service:1.2.0)
            ↓
┌───────────┬───────────┬───────────┐
│    Dev    │  Staging  │   Prod    │
│ stack.dev │ stack.stg │stack.prod │
│ .env.dev  │ .env.stg  │ .env.prod │
└───────────┴───────────┴───────────┘
```

Mỗi môi trường dùng **cùng image**, chỉ khác file stack và env. Swarm quản lý rolling update, restart tự động, và có thể mở rộng sang multi-node sau này.

---

## Kiến trúc CI/CD tổng thể

```
push develop  → deploy Staging  (auto)
push main     → deploy Prod     (auto)
push feat/**  → chỉ build & test (không deploy)

GitHub Actions:
  1. Build Maven → JAR
  2. Docker build → push image:sha + image:latest/staging
  3. SSH vào VPS → docker stack deploy
```

---

## Phase 1: Cấu trúc file trong repo

```
.
├── auth-service/
│   └── Dockerfile
├── course-service/
│   └── Dockerfile
├── gateway-service/
│   └── Dockerfile
├── deploy/
│   ├── stack.staging.yml       # Swarm stack file cho staging
│   ├── stack.prod.yml          # Swarm stack file cho prod
│   └── .env.example            # Template env (commit được)
└── .github/
    └── workflows/
        ├── ci.yml              # Build + test (tất cả branch)
        └── deploy.yml          # Deploy (develop & main)
```

---

## Phase 2: Dockerfile (dùng chung cho mọi môi trường)

`auth-service/Dockerfile`, `course-service/Dockerfile`, `gateway-service/Dockerfile` — cùng pattern:

```dockerfile
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
```

> Không có gì environment-specific trong Dockerfile. Config được inject qua env vars lúc runtime.

---

## Phase 3: Docker Swarm Stack Files

### `deploy/stack.staging.yml`

```yaml
version: "3.8"

services:
  gateway-service:
    image: ghcr.io/${GITHUB_ORG}/gateway-service:staging
    ports:
      - "8100:8000"        # staging dùng port khác
    environment:
      - LOG_LEVEL_ROOT=DEBUG
      - SWAGGER_ENABLED=true
      - SWAGGER_UI_ENABLED=true
    env_file: /opt/my-udemy/.env.staging
    deploy:
      replicas: 1
      restart_policy:
        condition: on-failure
    networks: [app-net]

  auth-service:
    image: ghcr.io/${GITHUB_ORG}/auth-service:staging
    env_file: /opt/my-udemy/.env.staging
    deploy:
      replicas: 1
      restart_policy:
        condition: on-failure
    networks: [app-net]

  course-service:
    image: ghcr.io/${GITHUB_ORG}/course-service:staging
    env_file: /opt/my-udemy/.env.staging
    deploy:
      replicas: 1
      restart_policy:
        condition: on-failure
    networks: [app-net]

networks:
  app-net:
    driver: overlay
```

### `deploy/stack.prod.yml`

```yaml
version: "3.8"

services:
  gateway-service:
    image: ghcr.io/${GITHUB_ORG}/gateway-service:latest
    ports:
      - "8000:8000"
    environment:
      - SWAGGER_ENABLED=false
      - SWAGGER_UI_ENABLED=false
    env_file: /opt/my-udemy/.env.prod
    deploy:
      replicas: 2                      # prod chạy 2 replica
      update_config:
        parallelism: 1
        delay: 10s
        order: start-first             # zero-downtime rolling update
      restart_policy:
        condition: on-failure
    networks: [app-net]

  auth-service:
    image: ghcr.io/${GITHUB_ORG}/auth-service:latest
    env_file: /opt/my-udemy/.env.prod
    deploy:
      replicas: 2
      update_config:
        parallelism: 1
        delay: 10s
        order: start-first
      restart_policy:
        condition: on-failure
    networks: [app-net]

  course-service:
    image: ghcr.io/${GITHUB_ORG}/course-service:latest
    env_file: /opt/my-udemy/.env.prod
    deploy:
      replicas: 2
      update_config:
        parallelism: 1
        delay: 10s
        order: start-first
      restart_policy:
        condition: on-failure
    networks: [app-net]

networks:
  app-net:
    driver: overlay
```

> Staging chạy 1 replica, Prod chạy 2 với rolling update zero-downtime.

---

## Phase 4: GitHub Actions

### `.github/workflows/ci.yml` — Build & Test (mọi branch)

```yaml
name: CI

on:
  push:
    branches: ["**"]

jobs:
  build:
    runs-on: ubuntu-latest
    strategy:
      matrix:
        service: [auth-service, course-service, gateway-service]
    steps:
      - uses: actions/checkout@v4

      - uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
          cache: maven

      - name: Build & Test
        run: ./mvnw clean verify -pl ${{ matrix.service }} -am
```

### `.github/workflows/deploy.yml` — Deploy (develop → staging, main → prod)

```yaml
name: Deploy

on:
  push:
    branches: [develop, main]

env:
  GITHUB_ORG: ${{ github.repository_owner }}
  REGISTRY: ghcr.io

jobs:
  build-and-push:
    runs-on: ubuntu-latest
    strategy:
      matrix:
        service: [auth-service, course-service, gateway-service]
    outputs:
      image_tag: ${{ steps.meta.outputs.version }}
    steps:
      - uses: actions/checkout@v4

      - uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
          cache: maven

      - name: Build JAR
        run: ./mvnw clean package -pl ${{ matrix.service }} -am -DskipTests

      - name: Login to GHCR
        uses: docker/login-action@v3
        with:
          registry: ghcr.io
          username: ${{ github.actor }}
          password: ${{ secrets.GITHUB_TOKEN }}

      - name: Determine image tag
        id: meta
        run: |
          if [[ "${{ github.ref }}" == "refs/heads/main" ]]; then
            echo "tag=latest" >> $GITHUB_OUTPUT
          else
            echo "tag=staging" >> $GITHUB_OUTPUT
          fi

      - name: Build & Push
        uses: docker/build-push-action@v5
        with:
          context: ./${{ matrix.service }}
          push: true
          tags: |
            ghcr.io/${{ env.GITHUB_ORG }}/${{ matrix.service }}:${{ steps.meta.outputs.tag }}
            ghcr.io/${{ env.GITHUB_ORG }}/${{ matrix.service }}:${{ github.sha }}

  deploy-staging:
    needs: build-and-push
    if: github.ref == 'refs/heads/develop'
    runs-on: ubuntu-latest
    environment: staging
    steps:
      - uses: actions/checkout@v4
      - name: Deploy to Staging
        uses: appleboy/ssh-action@v1
        with:
          host: ${{ secrets.VPS_HOST }}
          username: ${{ secrets.VPS_USER }}
          key: ${{ secrets.VPS_SSH_KEY }}
          script: |
            export GITHUB_ORG=${{ github.repository_owner }}
            docker stack deploy \
              --compose-file /opt/my-udemy/stack.staging.yml \
              --with-registry-auth \
              my-udemy-staging
            docker image prune -f

  deploy-prod:
    needs: build-and-push
    if: github.ref == 'refs/heads/main'
    runs-on: ubuntu-latest
    environment: production          # có thể bật "required reviewers" ở đây
    steps:
      - uses: actions/checkout@v4
      - name: Deploy to Production
        uses: appleboy/ssh-action@v1
        with:
          host: ${{ secrets.VPS_HOST }}
          username: ${{ secrets.VPS_USER }}
          key: ${{ secrets.VPS_SSH_KEY }}
          script: |
            export GITHUB_ORG=${{ github.repository_owner }}
            docker stack deploy \
              --compose-file /opt/my-udemy/stack.prod.yml \
              --with-registry-auth \
              my-udemy-prod
            docker image prune -f
```

---

## Phase 5: Chuẩn bị VPS

```bash
# Cài Docker
sudo apt update && sudo apt install -y docker.io
sudo usermod -aG docker $USER

# Khởi tạo Swarm (chỉ 1 lần)
docker swarm init

# Tạo thư mục chứa config
sudo mkdir -p /opt/my-udemy
sudo chown $USER:$USER /opt/my-udemy

# Copy stack files lên VPS (từ máy local hoặc qua git)
scp deploy/stack.staging.yml user@vps:/opt/my-udemy/
scp deploy/stack.prod.yml    user@vps:/opt/my-udemy/

# Tạo file env (điền secrets thực)
cp deploy/.env.example /opt/my-udemy/.env.staging
cp deploy/.env.example /opt/my-udemy/.env.prod
nano /opt/my-udemy/.env.prod

# Login GHCR để Swarm pull được private image
echo $GITHUB_TOKEN | docker login ghcr.io -u <github-username> --password-stdin
```

---

## Phase 6: GitHub Secrets & Environments

### Secrets (Settings → Secrets → Actions)

| Secret | Giá trị |
|---|---|
| `VPS_HOST` | IP hoặc domain VPS |
| `VPS_USER` | user SSH (`ubuntu`) |
| `VPS_SSH_KEY` | nội dung private key |
| `GITHUB_TOKEN` | tự động, không cần tạo |

### Environments (Settings → Environments)

- Tạo environment **`staging`** — không cần approval
- Tạo environment **`production`** — bật **Required reviewers** để prod cần approve thủ công trước khi deploy

**Tạo SSH key:**
```bash
ssh-keygen -t ed25519 -C "github-actions-deploy"
ssh-copy-id -i ~/.ssh/id_ed25519.pub user@vps-ip
# Dán nội dung ~/.ssh/id_ed25519 vào GitHub Secret VPS_SSH_KEY
```

---

## Phase 7: Template `.env.example` (commit được)

`deploy/.env.example`:
```env
# Database — auth-service
DB_URL=jdbc:postgresql://<host>:5432/my-udemy-auth
DB_USERNAME=postgres
DB_PASSWORD=

# Database — course-service (override trong stack file nếu cần URL riêng)
# DB_URL_COURSE=jdbc:postgresql://<host>:5432/my-udemy-course

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=

# JWT
JWT_SECRET=
JWT_EXPIRATION=86400000
JWT_REFRESH_EXPIRATION=604800000

# Cloudflare R2
R2_ACCOUNT_ID=
R2_ACCESS_KEY_ID=
R2_SECRET_ACCESS_KEY=
R2_BUCKET=
R2_PUBLIC_URL=

# Gateway
GATEWAY_URL=http://<vps-ip>:8000
```

---

## Luồng làm việc hàng ngày

```
feature branch  →  push  →  CI (build + test only)
develop         →  push  →  CI + deploy staging   (tự động)
main            →  PR merge  →  CI + deploy prod  (cần approve nếu bật)
```

---

## Thứ tự thực hiện

- [ ] Tạo `Dockerfile` cho `auth-service`, `course-service`, `gateway-service`
- [ ] Tạo thư mục `deploy/`, thêm `stack.staging.yml`, `stack.prod.yml`, `.env.example`
- [ ] Tạo `.github/workflows/ci.yml` và `deploy.yml`
- [ ] Cài Docker, khởi tạo Swarm trên VPS (`docker swarm init`)
- [ ] Copy stack files lên VPS, tạo `.env.staging` và `.env.prod`
- [ ] Tạo SSH key pair, thêm public key vào VPS
- [ ] Thêm GitHub Secrets: `VPS_HOST`, `VPS_USER`, `VPS_SSH_KEY`
- [ ] Tạo GitHub Environments: `staging` và `production`
- [ ] Push lên `develop` → kiểm tra staging chạy đúng
- [ ] Merge lên `main` → kiểm tra prod