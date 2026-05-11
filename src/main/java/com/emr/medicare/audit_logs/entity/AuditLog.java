package com.emr.medicare.audit_logs.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Integer logId;

    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "endpoint")
    private String endpoint;

    @Column(name = "method")
    private String method;

    @Column(name = "ip")
    private String ip;

    @Column(name = "status_code")
    private Integer statusCode;

    @Column(name = "timestamp")
    private LocalDateTime timestamp;
}
