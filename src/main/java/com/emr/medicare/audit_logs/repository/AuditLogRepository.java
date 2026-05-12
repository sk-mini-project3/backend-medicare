package com.emr.medicare.audit_logs.repository;

import com.emr.medicare.audit_logs.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, Integer> {
}
