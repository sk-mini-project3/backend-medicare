-- JPA User.verificationCode → users.verification_code
-- 로그인 시 "Unknown column 'verification_code'" 오류 시 실행

ALTER TABLE users
    ADD COLUMN verification_code VARCHAR(255) NULL
        COMMENT '의사/간호사 인증 코드 (PATIENT는 NULL)';

ALTER TABLE users
    ADD CONSTRAINT uk_users_verification_code UNIQUE (verification_code);
