# Medicare Backend

> **Spring Boot 기반 헬스케어 전자의무기록(EMR) 백엔드 시스템**
>
> A Spring Boot backend for a healthcare Electronic Medical Records (EMR) system.

![Java](https://img.shields.io/badge/Java-17-blue) ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.6-brightgreen) ![MySQL](https://img.shields.io/badge/MySQL-8.x-orange) ![Redis](https://img.shields.io/badge/Redis-7.x-red)

---

## 프로젝트 개요 / Project Overview

환자, 의사, 간호사 역할을 가진 사용자들이 진료 예약, 진료 기록, 처방전을 관리할 수 있는 EMR 시스템의 백엔드 서버입니다.

A backend server for an EMR system where users with patient, doctor, and nurse roles can manage medical appointments, medical records, and prescriptions.

**주요 기능 / Key Features:**

- 회원가입 / 로그인 / JWT 인증 (Access Token + Refresh Token)
- 환자 정보 관리 (주민등록번호 AES-256 암호화)
- 예약 생성 및 상태 관리 (PENDING → CONFIRMED → COMPLETED)
- 진료 기록 작성 및 조회
- 처방전 발행 / 승인 / 거절 (SHA-256 무결성 검증)
- 비밀번호 재설정 (Gmail SMTP)
- 외부 AI 서비스 연동
- 전체 작업 감사 로그(Audit Log)

---

## 기술 스택 / Tech Stack

| 분류 | 기술 |
|------|------|
| Language | Java 17 |
| Framework | Spring Boot 4.0.6 |
| Build | Gradle 9.4.1 |
| Database | MySQL 8.x (Spring Data JPA / Hibernate) |
| Cache | Redis 7.x (토큰 블랙리스트) |
| Authentication | JWT (JJWT 0.12.5) + Spring Security |
| Mail | Gmail SMTP (Spring Mail) |
| Container | Docker (eclipse-temurin:17) |
| CI/CD | GitHub Actions + GitHub Container Registry (GHCR) |

---

## 아키텍처 / Architecture

### 시스템 구조

```
┌─────────┐      ┌─────────────────────┐      ┌──────────┐
│  Client │─────▶│  AWS ALB (HTTPS)    │─────▶│          │
└─────────┘      └─────────────────────┘      │  Spring  │
                                               │   Boot   │
                                               │ :3000    │
                                               │          │
                                     ┌─────────┤          ├──────────┐
                                     │         └──────────┘          │
                                     ▼                               ▼
                              ┌──────────────┐              ┌──────────────┐
                              │   MySQL 8.x  │              │   Redis 7.x  │
                              │  (JPA/ORM)   │              │(Token Store) │
                              └──────────────┘              └──────────────┘
                                                                     │
                                                            ┌────────▼────────┐
                                                            │  AI Service     │
                                                            │  :8001 (외부)   │
                                                            └─────────────────┘
```

### 도메인 모듈 구조

```
com.emr.medicare/
├── auth/           # 인증 (JWT 발급, 로그인, 로그아웃, 비밀번호 재설정)
├── patient/        # 환자 정보 관리
├── medicalrecord/  # 진료 기록
├── prescription/   # 처방전
├── reservation/    # 예약
├── ai/             # 외부 AI 서비스 연동
├── audit_logs/     # 감사 로그
├── doctor/         # 의사 정보
├── nurse/          # 간호사 정보
├── user/           # 공통 사용자
├── security/       # Spring Security 설정, JWT 필터
└── common/         # 공통 응답, 예외 처리, 유틸리티
```

각 도메인은 `controller → service → repository → entity` 계층 구조를 따릅니다.

Each domain follows the layered pattern: `controller → service → repository → entity`.

---

## 빠른 시작 / Quick Start

### 사전 요구사항

- Java 17+
- MySQL 8.x
- Redis 7.x
- (선택) Docker

### 1. 저장소 클론

```bash
git clone <repo-url>
cd backend-medicare-main
```

### 2. 환경변수 설정

아래 [환경변수 설정](#환경변수-설정--environment-variables) 섹션을 참고하여 `.env` 파일을 생성합니다.

### 3. 실행

```bash
# Windows
gradlew.bat bootRun

# Mac / Linux
./gradlew bootRun
```

서버가 `http://localhost:3000` 에서 실행됩니다.

---

## 환경변수 설정 / Environment Variables

프로젝트 루트에 `.env` 파일을 생성하세요. Spring Boot가 자동으로 읽어들입니다.

Create a `.env` file in the project root. Spring Boot will load it automatically.

### `.env` 예시 / Example

```properties
# ── 데이터베이스 (Database) ──────────────────────────────────────
# 옵션 A: 전체 JDBC URL 직접 지정 (이 값이 있으면 아래 DB_* 변수 무시됨)
# Option A: Full JDBC URL (overrides DB_HOST / DB_PORT / DB_NAME below)
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/medicalservicedb?useSSL=false&serverTimezone=Asia/Seoul&characterEncoding=UTF-8

# 옵션 B: 개별 항목 지정 (SPRING_DATASOURCE_URL 미설정 시 사용)
# Option B: Individual settings (used when SPRING_DATASOURCE_URL is not set)
# DB_HOST=localhost
# DB_PORT=3306
# DB_NAME=medicalservicedb

DB_USERNAME=root
DB_PASSWORD=your_db_password

# ── Redis ─────────────────────────────────────────────────────────
REDIS_HOST=localhost
REDIS_PORT=6379
# REDIS_PASSWORD=          # Redis 비밀번호가 있는 경우

# ── JWT ───────────────────────────────────────────────────────────
# HS256 서명에 사용하는 비밀키 (충분히 긴 랜덤 문자열 권장)
# Secret key for HS256 signing (use a long random string)
JWT_SECRET=your_very_long_jwt_secret_key_here

# ── 암호화 (Encryption) ───────────────────────────────────────────
# AES-256/CBC 암호화 키 (주민등록번호 등 민감정보 암호화)
# AES-256/CBC key for encrypting sensitive data (resident number, etc.)
ENCRYPTION_KEY=your_32_char_aes_key_here_______

# ── 이메일 (Mail) ─────────────────────────────────────────────────
# Gmail 앱 비밀번호를 사용하세요 (공백 포함 시 반드시 따옴표로 감싸기)
# Use a Gmail App Password (wrap in quotes if it contains spaces)
MAIL_USERNAME=your_gmail@gmail.com
MAIL_PASSWORD="xxxx xxxx xxxx xxxx"

# ── AI 서비스 (AI Service) ────────────────────────────────────────
# 외부 AI 서비스 URL (기본값: http://localhost:8001)
AI_BASE_URL=http://localhost:8001

# ── 기타 (Others) ─────────────────────────────────────────────────
# 비밀번호 재설정 메일의 링크 베이스 URL (기본값: http://localhost:8080)
APP_FRONTEND_URL=http://localhost:8080

# 메일 발송 모드: auto(기본) | smtp | console
# auto: MAIL_USERNAME이 비어 있으면 링크를 로그에만 출력 (로컬 개발용)
APP_MAIL_DELIVERY=auto

# 서버 포트 (기본값: 3000)
# SERVER_PORT=3000
```

### 필수 / Required vs 선택 / Optional

| 변수 | 필수 | 기본값 | 설명 |
|------|:----:|--------|------|
| `SPRING_DATASOURCE_URL` | - | 조합값 | DB URL 직접 지정 (또는 `DB_HOST` 등 사용) |
| `DB_USERNAME` | - | `root` | DB 사용자명 |
| `DB_PASSWORD` | - | (빈 값) | DB 비밀번호 |
| `REDIS_HOST` | - | `localhost` | Redis 호스트 |
| `REDIS_PORT` | - | `6379` | Redis 포트 |
| `JWT_SECRET` | **필수** | - | JWT 서명 키 |
| `ENCRYPTION_KEY` | **필수** | - | AES-256 암호화 키 |
| `MAIL_USERNAME` | - | (빈 값) | Gmail 계정 |
| `MAIL_PASSWORD` | - | (빈 값) | Gmail 앱 비밀번호 |
| `AI_BASE_URL` | - | `http://localhost:8001` | AI 서비스 URL |
| `APP_FRONTEND_URL` | - | `http://localhost:8080` | 프론트엔드 URL |
| `APP_MAIL_DELIVERY` | - | `auto` | 메일 발송 모드 |
| `SERVER_PORT` | - | `3000` | 서버 포트 |

---

## 빌드 & 실행 / Build & Run

```bash
# 개발 서버 실행 / Run dev server
./gradlew bootRun          # Mac/Linux
gradlew.bat bootRun        # Windows

# 전체 테스트 / Run all tests
./gradlew test

# 단일 테스트 클래스 / Single test class
./gradlew test --tests "com.emr.medicare.SomeTest"

# JAR 빌드 / Build JAR
./gradlew bootJar

# 클린 빌드 / Clean build
./gradlew clean build
```

---

## Docker 실행 / Docker

### 이미지 빌드

```bash
docker build -t medicare-backend .
```

### 컨테이너 실행

```bash
# .env 파일을 사용하여 실행
docker run --env-file .env -p 3000:3000 medicare-backend

# 백그라운드 실행
docker run -d --env-file .env -p 3000:3000 --name medicare medicare-backend
```

### GHCR 이미지 사용

CI/CD를 통해 자동 빌드된 이미지를 사용할 수 있습니다.

```bash
docker pull ghcr.io/<github-org>/<repo>:latest
docker run --env-file .env -p 3000:3000 ghcr.io/<github-org>/<repo>:latest
```

---

## CI/CD (GitHub Actions)

워크플로우 파일: [`.github/workflows/medical-services-ci.yml`](.github/workflows/medical-services-ci.yml)

### CI 단계 (모든 push / PR)

1. JDK 17 설정
2. Redis 서비스 컨테이너 시작
3. `./gradlew test` — 전체 테스트 실행
4. `./gradlew bootJar` — 실행 가능한 JAR 빌드
5. Docker 이미지 빌드 (스모크 테스트, 푸시 없음)

### CD 단계 (`main` 브랜치 push 시에만)

1. GHCR 로그인
2. Docker 이미지 빌드 및 푸시
   - 태그: `latest`, `{commit-sha}`

### 필요한 GitHub Secrets

저장소 Settings → Secrets and variables → Actions 에 아래 항목을 등록하세요.

| Secret | 설명 |
|--------|------|
| `SPRING_DATASOURCE_URL` | 테스트용 DB JDBC URL |
| `DB_USERNAME` | DB 사용자명 |
| `DB_PASSWORD` | DB 비밀번호 |
| `REDIS_HOST` | Redis 호스트 (CI에서는 `localhost` 사용) |
| `REDIS_PORT` | Redis 포트 (CI에서는 `6379` 사용) |
| `JWT_SECRET` | JWT 서명 키 |
| `ENCRYPTION_KEY` | AES-256 암호화 키 |
| `MAIL_USERNAME` | Gmail 계정 (선택) |
| `MAIL_PASSWORD` | Gmail 앱 비밀번호 (선택) |

> `GITHUB_TOKEN`은 GitHub가 자동으로 제공합니다. GHCR 이미지 푸시에 사용됩니다.

---

## 프로젝트 구조 / Project Structure

```
backend-medicare-main/
├── .github/
│   └── workflows/
│       └── medical-services-ci.yml   # CI/CD 워크플로우
├── src/
│   ├── main/
│   │   ├── java/com/emr/medicare/
│   │   │   ├── MedicareApplication.java
│   │   │   ├── ai/                   # AI 서비스 연동
│   │   │   ├── audit_logs/           # 감사 로그
│   │   │   ├── auth/                 # 인증 (JWT, 로그인, 회원가입)
│   │   │   ├── common/               # 공통 (예외처리, 응답, 유틸)
│   │   │   ├── doctor/               # 의사 정보
│   │   │   ├── medicalrecord/        # 진료 기록
│   │   │   ├── nurse/                # 간호사 정보
│   │   │   ├── patient/              # 환자 정보
│   │   │   ├── prescription/         # 처방전
│   │   │   ├── reservation/          # 예약
│   │   │   ├── security/             # Security 설정, JWT 필터
│   │   │   └── user/                 # 공통 사용자 엔티티
│   │   └── resources/
│   │       ├── application.yml       # 메인 설정
│   │       └── verification-codes.yml # 의료진 인증 코드
│   └── test/
├── Dockerfile
├── build.gradle
└── gradlew / gradlew.bat
```

---

## 보안 유의사항 / Security Notes

- `.env` 파일은 절대 Git에 커밋하지 마세요. (`.gitignore`에 포함됨)
- `JWT_SECRET`과 `ENCRYPTION_KEY`는 충분히 긴 랜덤 값을 사용하세요.
- Gmail을 이메일 발송에 사용할 경우 반드시 **앱 비밀번호**를 사용하세요 (일반 계정 비밀번호 사용 불가).
- Do **not** commit `.env` to Git. Use strong random values for `JWT_SECRET` and `ENCRYPTION_KEY`. For Gmail, always use an **App Password**.
