package com.emr.medicare.audit_logs.interceptor;

import com.emr.medicare.audit_logs.service.AuditLogService;
import com.emr.medicare.common.util.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class AuditLogInterceptor implements HandlerInterceptor {

    private final AuditLogService auditLogService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute("startTime", LocalDateTime.now());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            LocalDateTime timestamp = (LocalDateTime) request.getAttribute("startTime");
            auditLogService.save(
                    userId,
                    request.getRequestURI(),
                    request.getMethod(),
                    resolveIp(request),
                    response.getStatus(),
                    timestamp
            );
        } catch (Exception ignored) {
            // 로그 저장 실패가 본 요청에 영향을 주지 않도록 예외 무시
        }
    }

    private String resolveIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        return (forwarded != null) ? forwarded.split(",")[0].trim() : request.getRemoteAddr();
    }
}
