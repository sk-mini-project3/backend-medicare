-- =====================================================================
-- role = PATIENT 인 사용자와 그에 딸린 데이터를 모두 삭제합니다.
-- 의사·간호사 계정은 유지됩니다.
--
-- 주의: 백업 없이 실행하면 복구할 수 없습니다. RDS/운영 DB에는 사용하지 마세요.
-- 실행: mysql -h ... -u ... -p medicalservicedb < scripts/delete-all-patient-users.sql
-- =====================================================================

START TRANSACTION;

-- FK 순서: 자식 → 부모 (환자 user_id 기준)
DELETE mr FROM medical_records mr
INNER JOIN users u ON u.user_id = mr.patient_id AND u.role = 'PATIENT';

DELETE p FROM prescriptions p
INNER JOIN users u ON u.user_id = p.patient_id AND u.role = 'PATIENT';

DELETE r FROM reservations r
INNER JOIN users u ON u.user_id = r.patient_id AND u.role = 'PATIENT';

DELETE pd FROM patient_details pd
INNER JOIN users u ON u.user_id = pd.user_id AND u.role = 'PATIENT';

-- 비밀번호 재설정 토큰 (엔티티 기본 테이블명: password_reset_token)
DELETE prt FROM password_reset_token prt
INNER JOIN users u ON u.user_id = prt.user_id AND u.role = 'PATIENT';

DELETE al FROM audit_logs al
INNER JOIN users u ON u.user_id = al.user_id AND u.role = 'PATIENT';

DELETE FROM users WHERE role = 'PATIENT';

COMMIT;
