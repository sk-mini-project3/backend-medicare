# Medicare EMR — B파트 API 명세서

> **Base URL:** `http://localhost:3000`  
> **Auth (dev 환경):** HTTP Basic Auth  
> **공통 응답 형식:**
> ```json
> { "success": true, "message": "success", "data": { ... } }
> { "success": false, "message": "오류 메시지", "data": null }
> ```

---

## 목차

1. [환자 상세정보 (Patient Details)](#1-환자-상세정보)
2. [예약 (Reservations)](#2-예약)
3. [진료기록 (Medical Records)](#3-진료기록)
4. [처방전 (Prescriptions)](#4-처방전)

---

## 1. 환자 상세정보

### POST `/api/patients/{userId}`
환자 상세정보 등록

- **권한:** DOCTOR, NURSE
- **Path Variable:** `userId` — Users 테이블의 user_id (환자)

**요청 Body**
```json
{
  "residentNumber": "900101-1234567",
  "gender": "남",
  "birthDate": "1990-01-01",
  "emergencyContact": "010-9999-8888",
  "bloodType": "A+",
  "address": "서울시 강남구 테헤란로 123",
  "insuranceInfo": "국민건강보험 12345678",
  "allergies": "페니실린 계열 항생제 알레르기"
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| residentNumber | String | ✅ | 주민등록번호 (DB에 AES-256 암호화 저장) |
| gender | String | ✅ | 성별 (남/여) |
| birthDate | String (yyyy-MM-dd) | ✅ | 생년월일 |
| emergencyContact | String | | 보호자 연락처 |
| bloodType | String | | 혈액형 (A+/A-/B+/B-/AB+/AB-/O+/O-) |
| address | String | | 주소 |
| insuranceInfo | String | | 건강보험 정보 |
| allergies | String | | 알레르기 정보 |

**응답 201 Created**
```json
{
  "success": true,
  "message": "success",
  "data": {
    "userId": 5,
    "gender": "남",
    "birthDate": "1990-01-01",
    "emergencyContact": "010-9999-8888",
    "bloodType": "A+",
    "address": "서울시 강남구 테헤란로 123",
    "insuranceInfo": "국민건강보험 12345678",
    "allergies": "페니실린 계열 항생제 알레르기"
  }
}
```

> `residentNumber`는 보안상 응답에서 제외됩니다.

**오류 응답**
```json
{ "success": false, "message": "residentNumber: 공백일 수 없습니다", "data": null }
```

---

### GET `/api/patients/{userId}`
환자 상세정보 단건 조회

- **권한:** DOCTOR, NURSE

**응답 200 OK**
```json
{
  "success": true,
  "message": "success",
  "data": {
    "userId": 5,
    "gender": "남",
    "birthDate": "1990-01-01",
    "emergencyContact": "010-9999-8888",
    "bloodType": "A+",
    "address": "서울시 강남구 테헤란로 123",
    "insuranceInfo": "국민건강보험 12345678",
    "allergies": "페니실린 계열 항생제 알레르기"
  }
}
```

**오류 응답 404**
```json
{ "success": false, "message": "환자 정보를 찾을 수 없습니다.", "data": null }
```

---

### GET `/api/patients`
전체 환자 목록 조회

- **권한:** DOCTOR, NURSE

**응답 200 OK**
```json
{
  "success": true,
  "message": "success",
  "data": [
    {
      "userId": 5,
      "gender": "남",
      "birthDate": "1990-01-01",
      "emergencyContact": "010-9999-8888",
      "bloodType": "A+",
      "address": "서울시 강남구 테헤란로 123",
      "insuranceInfo": "국민건강보험 12345678",
      "allergies": null
    },
    {
      "userId": 6,
      "gender": "여",
      "birthDate": "1985-07-15",
      "emergencyContact": null,
      "bloodType": "O-",
      "address": "부산시 해운대구 센텀로 45",
      "insuranceInfo": "삼성화재 의료보험",
      "allergies": "아스피린"
    }
  ]
}
```

---

### PUT `/api/patients/{userId}`
환자 상세정보 수정

- **권한:** DOCTOR, NURSE

**요청 Body**
```json
{
  "residentNumber": "900101-1234567",
  "gender": "남",
  "birthDate": "1990-01-01",
  "emergencyContact": "010-7777-6666",
  "bloodType": "A+",
  "address": "서울시 서초구 반포대로 200",
  "insuranceInfo": "국민건강보험 12345678",
  "allergies": "페니실린, 아스피린"
}
```

**응답 200 OK**
```json
{
  "success": true,
  "message": "success",
  "data": {
    "userId": 5,
    "gender": "남",
    "birthDate": "1990-01-01",
    "emergencyContact": "010-7777-6666",
    "bloodType": "A+",
    "address": "서울시 서초구 반포대로 200",
    "insuranceInfo": "국민건강보험 12345678",
    "allergies": "페니실린, 아스피린"
  }
}
```

---

## 2. 예약

### POST `/api/reservations`
예약 생성

- **권한:** PATIENT, NURSE

**요청 Body**
```json
{
  "patientId": 5,
  "doctorId": 2,
  "reservationDate": "2026-05-15T10:30:00",
  "symptoms": "3일째 기침과 발열이 지속되고 있습니다."
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| patientId | Long | ✅ | 환자 user_id |
| doctorId | Long | ✅ | 담당 의사 user_id |
| reservationDate | String (ISO 8601) | ✅ | 예약 일시 |
| symptoms | String | | 증상 설명 |

**응답 201 Created**
```json
{
  "success": true,
  "message": "success",
  "data": {
    "reservationId": 1,
    "patientId": 5,
    "doctorId": 2,
    "reservationDate": "2026-05-15T10:30:00",
    "symptoms": "3일째 기침과 발열이 지속되고 있습니다.",
    "status": "WAITING"
  }
}
```

> 신규 예약의 `status`는 항상 `WAITING`으로 시작합니다.

---

### GET `/api/reservations/{id}`
예약 단건 조회

- **권한:** DOCTOR, NURSE, PATIENT

**응답 200 OK**
```json
{
  "success": true,
  "message": "success",
  "data": {
    "reservationId": 1,
    "patientId": 5,
    "doctorId": 2,
    "reservationDate": "2026-05-15T10:30:00",
    "symptoms": "3일째 기침과 발열이 지속되고 있습니다.",
    "status": "NURSE_APPROVED"
  }
}
```

---

### GET `/api/reservations`
예약 목록 조회 (필터 지원)

- **권한:** DOCTOR, NURSE
- **Query Parameters** (모두 선택)

| 파라미터 | 타입 | 설명 |
|---------|------|------|
| status | String | `WAITING` / `NURSE_APPROVED` / `COMPLETED` |
| doctorId | Long | 담당 의사 ID |
| date | String (yyyy-MM-dd) | 예약 날짜 |

**요청 예시**
```
GET /api/reservations?status=WAITING&date=2026-05-15
GET /api/reservations?doctorId=2
GET /api/reservations
```

**응답 200 OK**
```json
{
  "success": true,
  "message": "success",
  "data": [
    {
      "reservationId": 1,
      "patientId": 5,
      "doctorId": 2,
      "reservationDate": "2026-05-15T10:30:00",
      "symptoms": "3일째 기침과 발열이 지속되고 있습니다.",
      "status": "WAITING"
    }
  ]
}
```

---

### GET `/api/reservations/my`
본인 예약 목록 조회 (환자용)

- **권한:** PATIENT
- **Query Parameter:** `patientId` (Long, 필수) — JWT 완성 후 제거 예정

**요청 예시**
```
GET /api/reservations/my?patientId=5
```

**응답 200 OK**
```json
{
  "success": true,
  "message": "success",
  "data": [
    {
      "reservationId": 1,
      "patientId": 5,
      "doctorId": 2,
      "reservationDate": "2026-05-15T10:30:00",
      "symptoms": "3일째 기침과 발열이 지속되고 있습니다.",
      "status": "COMPLETED"
    }
  ]
}
```

---

### PATCH `/api/reservations/{id}/status`
예약 상태 변경

- **권한:** NURSE, DOCTOR

**요청 Body**
```json
{ "status": "NURSE_APPROVED" }
```

| status 값 | 설명 |
|-----------|------|
| `WAITING` | 대기중 (초기 상태) |
| `NURSE_APPROVED` | 간호사 접수 완료 |
| `COMPLETED` | 진료 완료 (되돌릴 수 없음) |

**응답 200 OK**
```json
{
  "success": true,
  "message": "success",
  "data": {
    "reservationId": 1,
    "patientId": 5,
    "doctorId": 2,
    "reservationDate": "2026-05-15T10:30:00",
    "symptoms": "3일째 기침과 발열이 지속되고 있습니다.",
    "status": "NURSE_APPROVED"
  }
}
```

**오류 응답 400**
```json
{ "success": false, "message": "완료된 예약의 상태는 변경할 수 없습니다.", "data": null }
```

---

## 3. 진료기록

### POST `/api/medical-records`
진료기록 작성

- **권한:** DOCTOR

**요청 Body**
```json
{
  "patientId": 5,
  "doctorId": 2,
  "reservationId": 1,
  "diagnosis": "급성 기관지염 (J20.9)",
  "treatmentNotes": "항생제 및 기침 억제제 처방. 3일 후 재진 예약 권장."
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| patientId | Long | ✅ | 환자 user_id |
| doctorId | Long | ✅ | 진료 의사 user_id |
| reservationId | Long | | 연결된 예약 ID |
| diagnosis | String | | 진단명 |
| treatmentNotes | String | | 진료 소견 |

**응답 201 Created**
```json
{
  "success": true,
  "message": "success",
  "data": {
    "recordId": 1,
    "reservationId": 1,
    "patientId": 5,
    "doctorId": 2,
    "diagnosis": "급성 기관지염 (J20.9)",
    "treatmentNotes": "항생제 및 기침 억제제 처방. 3일 후 재진 예약 권장.",
    "createdAt": "2026-05-15T11:05:00",
    "updatedAt": "2026-05-15T11:05:00"
  }
}
```

---

### GET `/api/medical-records/{id}`
진료기록 단건 조회

- **권한:** DOCTOR, NURSE

**응답 200 OK**
```json
{
  "success": true,
  "message": "success",
  "data": {
    "recordId": 1,
    "reservationId": 1,
    "patientId": 5,
    "doctorId": 2,
    "diagnosis": "급성 기관지염 (J20.9)",
    "treatmentNotes": "항생제 및 기침 억제제 처방.",
    "createdAt": "2026-05-15T11:05:00",
    "updatedAt": "2026-05-15T11:05:00"
  }
}
```

---

### GET `/api/medical-records/patient/{patientId}`
환자별 진료기록 목록 조회

- **권한:** DOCTOR, NURSE

**응답 200 OK**
```json
{
  "success": true,
  "message": "success",
  "data": [
    {
      "recordId": 1,
      "reservationId": 1,
      "patientId": 5,
      "doctorId": 2,
      "diagnosis": "급성 기관지염 (J20.9)",
      "treatmentNotes": "항생제 및 기침 억제제 처방.",
      "createdAt": "2026-05-15T11:05:00",
      "updatedAt": "2026-05-15T11:05:00"
    }
  ]
}
```

---

### GET `/api/medical-records/reservation/{reservationId}`
예약별 진료기록 조회

- **권한:** DOCTOR, NURSE

**응답 200 OK**
```json
{
  "success": true,
  "message": "success",
  "data": {
    "recordId": 1,
    "reservationId": 1,
    "patientId": 5,
    "doctorId": 2,
    "diagnosis": "급성 기관지염 (J20.9)",
    "treatmentNotes": "항생제 및 기침 억제제 처방.",
    "createdAt": "2026-05-15T11:05:00",
    "updatedAt": "2026-05-15T11:05:00"
  }
}
```

---

### PUT `/api/medical-records/{id}`
진료기록 수정

- **권한:** DOCTOR

**요청 Body**
```json
{
  "diagnosis": "급성 기관지염 (J20.9) — 세균성",
  "treatmentNotes": "아목시실린 500mg 7일 처방. 3일 후 재진."
}
```

**응답 200 OK**
```json
{
  "success": true,
  "message": "success",
  "data": {
    "recordId": 1,
    "reservationId": 1,
    "patientId": 5,
    "doctorId": 2,
    "diagnosis": "급성 기관지염 (J20.9) — 세균성",
    "treatmentNotes": "아목시실린 500mg 7일 처방. 3일 후 재진.",
    "createdAt": "2026-05-15T11:05:00",
    "updatedAt": "2026-05-15T11:42:00"
  }
}
```

---

### GET `/api/medical-records/my`
본인 진료기록 조회 (환자용)

- **권한:** PATIENT
- **Query Parameter:** `patientId` (Long, 필수) — JWT 완성 후 제거 예정

**요청 예시**
```
GET /api/medical-records/my?patientId=5
```

**응답 200 OK**
```json
{
  "success": true,
  "message": "success",
  "data": [
    {
      "recordId": 1,
      "reservationId": 1,
      "patientId": 5,
      "doctorId": 2,
      "diagnosis": "급성 기관지염 (J20.9)",
      "treatmentNotes": "항생제 및 기침 억제제 처방.",
      "createdAt": "2026-05-15T11:05:00",
      "updatedAt": "2026-05-15T11:05:00"
    }
  ]
}
```

---

## 4. 처방전

### POST `/api/prescriptions`
처방전 작성

- **권한:** DOCTOR, NURSE

**요청 Body**
```json
{
  "patientId": 5,
  "doctorId": 2,
  "reservationId": 1,
  "nurseId": 3,
  "medication": "아목시실린 500mg, 기침억제제 15mg",
  "dosage": "하루 3회, 식후 30분, 7일분"
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| patientId | Long | ✅ | 환자 user_id |
| doctorId | Long | | 처방 의사 user_id |
| reservationId | Long | | 연결 예약 ID |
| nurseId | Long | | 임시 처방 작성 간호사 user_id |
| medication | String | | 약품명 |
| dosage | String | | 복약 지도 |

**응답 201 Created**
```json
{
  "success": true,
  "message": "success",
  "data": {
    "prescriptionId": 1,
    "reservationId": 1,
    "patientId": 5,
    "doctorId": 2,
    "medication": "아목시실린 500mg, 기침억제제 15mg",
    "dosage": "하루 3회, 식후 30분, 7일분",
    "createdAt": "2026-05-15T11:10:00",
    "hash": "a3f8d2c1e7b4...",
    "status": "PENDING",
    "nurseId": 3,
    "approvedBy": null
  }
}
```

> 신규 처방전 `status`는 항상 `PENDING`. `hash`는 서버 자동 생성.

---

### GET `/api/prescriptions/{id}`
처방전 단건 조회

- **권한:** DOCTOR, NURSE, PATIENT

**응답 200 OK**
```json
{
  "success": true,
  "message": "success",
  "data": {
    "prescriptionId": 1,
    "reservationId": 1,
    "patientId": 5,
    "doctorId": 2,
    "medication": "아목시실린 500mg, 기침억제제 15mg",
    "dosage": "하루 3회, 식후 30분, 7일분",
    "createdAt": "2026-05-15T11:10:00",
    "hash": "a3f8d2c1e7b4...",
    "status": "APPROVED",
    "nurseId": 3,
    "approvedBy": 2
  }
}
```

---

### GET `/api/prescriptions`
처방전 목록 조회 (필터 지원)

- **권한:** DOCTOR, NURSE

| 파라미터 | 타입 | 설명 |
|---------|------|------|
| status | String | `PENDING` / `APPROVED` / `REJECTED` |
| nurseId | Long | 작성 간호사 ID |
| doctorId | Long | 처방 의사 ID |

**요청 예시**
```
GET /api/prescriptions?status=PENDING
GET /api/prescriptions?nurseId=3&status=PENDING
GET /api/prescriptions?doctorId=2
```

**응답 200 OK**
```json
{
  "success": true,
  "message": "success",
  "data": [
    {
      "prescriptionId": 1,
      "patientId": 5,
      "medication": "아목시실린 500mg",
      "dosage": "하루 3회, 식후 30분, 7일분",
      "status": "PENDING",
      "nurseId": 3,
      "approvedBy": null
    }
  ]
}
```

---

### GET `/api/prescriptions/patient/{patientId}`
환자별 처방전 목록

- **권한:** DOCTOR, NURSE

---

### GET `/api/prescriptions/my`
본인 처방전 조회 (환자용)

- **권한:** PATIENT
- **Query Parameter:** `patientId` (Long, 필수) — JWT 완성 후 제거 예정

---

### PATCH `/api/prescriptions/{id}/approve`
처방전 승인 (PENDING → APPROVED)

- **권한:** DOCTOR
- **Query Parameter:** `doctorId` (Long, 필수) — JWT 완성 후 제거 예정

**요청 예시**
```
PATCH /api/prescriptions/1/approve?doctorId=2
```

**응답 200 OK**
```json
{
  "success": true,
  "message": "success",
  "data": {
    "prescriptionId": 1,
    "status": "APPROVED",
    "approvedBy": 2
  }
}
```

**오류 응답 400**
```json
{ "success": false, "message": "대기 중인 처방전만 승인할 수 있습니다.", "data": null }
```

---

### PATCH `/api/prescriptions/{id}/reject`
처방전 반려 (PENDING → REJECTED)

- **권한:** DOCTOR

**요청 예시**
```
PATCH /api/prescriptions/1/reject
```

**응답 200 OK**
```json
{
  "success": true,
  "message": "success",
  "data": {
    "prescriptionId": 1,
    "status": "REJECTED",
    "approvedBy": null
  }
}
```

**오류 응답 400**
```json
{ "success": false, "message": "대기 중인 처방전만 반려할 수 있습니다.", "data": null }
```

---

### GET `/api/prescriptions/{id}/verify`
처방전 위변조 검증

- **권한:** DOCTOR, NURSE

**오류 응답 409 — 위변조 감지**
```json
{ "success": false, "message": "처방전 hash 불일치 — 위변조가 의심됩니다.", "data": null }
```

---

## 공통 오류 코드

| HTTP Status | 상황 |
|-------------|------|
| 400 | 요청 파라미터 오류, 상태 전이 규칙 위반 |
| 401 | 인증 정보 없음 |
| 403 | 권한 없음 |
| 404 | 리소스 없음 |
| 409 | 처방전 hash 불일치 (위변조 감지) |
| 500 | 서버 내부 오류 |

---

## 상태값 참조

### ReservationStatus
| 값 | 화면 표시 |
|----|---------|
| `WAITING` | 대기중 |
| `NURSE_APPROVED` | 진료중 |
| `COMPLETED` | 완료 |

### PrescriptionStatus
| 값 | 설명 |
|----|------|
| `PENDING` | 승인 대기 |
| `APPROVED` | 승인 완료 |
| `REJECTED` | 반려 |

---

## dev 환경 테스트 계정

| 계정 | 비밀번호 | 역할 |
|------|---------|------|
| `doctor` | `doctor` | DOCTOR |
| `nurse` | `nurse` | NURSE |
| `patient` | `patient` | PATIENT |
