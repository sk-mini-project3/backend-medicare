package com.emr.medicare.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

// A파트 SecurityConfig(JWT)가 완성되기 전까지 로컬 테스트용으로 사용
// A파트 SecurityConfig에 @EnableMethodSecurity가 추가되면 이 클래스는 삭제
@Configuration
@EnableWebSecurity
@EnableMethodSecurity  // 컨트롤러의 @PreAuthorize 동작에 필요
@Profile("dev")
public class DevSecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .build();
    }
}
