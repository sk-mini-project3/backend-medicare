package com.emr.medicare.audit_logs.service;

import com.emr.medicare.audit_logs.entity.AuditLog;
import com.emr.medicare.audit_logs.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public void save(Long userId, String endpoint, String method,
                     String ip, int statusCode, LocalDateTime timestamp) {
        auditLogRepository.save(AuditLog.builder()
                .userId(userId != null ? userId.intValue() : null)
                .endpoint(endpoint)
                .method(method)
                .ip(ip)
                .statusCode(statusCode)
                .timestamp(timestamp)
                .build());
    }
}
