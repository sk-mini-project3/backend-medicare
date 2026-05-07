package com.emr.medicare.common.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {

    private SecurityUtils() {}

    public static Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        // A파트 JWT 완성 후 교체:
        // return ((CustomUserDetails) auth.getPrincipal()).getUserId();
        return null;
    }
}
