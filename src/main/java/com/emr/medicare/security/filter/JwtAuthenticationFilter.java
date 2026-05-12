package com.emr.medicare.security.filter;

import com.emr.medicare.security.jwt.JwtProvider;
import com.emr.medicare.user.entity.User;
import com.emr.medicare.user.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;
    private final StringRedisTemplate redisTemplate;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String token = resolveToken(request);

        if (token != null) {

            // 1. Redis 블랙리스트 검사
            Boolean isBlacklisted = redisTemplate.hasKey(
                    "blacklist:" + token
            );

            if (Boolean.TRUE.equals(isBlacklisted)) {

                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

                response.setContentType("application/json;charset=UTF-8");

                response.getWriter().write(
                        """
                        {
                          "status": 401,
                          "message": "Blacklisted Token"
                        }
                        """
                );

                return;
            }

            // 2. JWT 유효성 검사
            if (jwtProvider.validateToken(token)) {

                Long userId = jwtProvider.getUserId(token);

                User user = userRepository.findById(userId)
                        .orElse(null);

                if (user != null) {

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    user,
                                    null,
                                    List.of(
                                            new SimpleGrantedAuthority(
                                                    "ROLE_" + user.getRole().name()
                                            )
                                    )
                            );

                    SecurityContextHolder.getContext()
                            .setAuthentication(authentication);
                }

            } else {

                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

                response.setContentType("application/json;charset=UTF-8");

                response.getWriter().write(
                        """
                        {
                          "status": 401,
                          "message": "Invalid Token"
                        }
                        """
                );

                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {

        String bearerToken =
                request.getHeader("Authorization");

        if (bearerToken != null &&
                bearerToken.startsWith("Bearer ")) {

            return bearerToken.substring(7);
        }

        return null;
    }
}