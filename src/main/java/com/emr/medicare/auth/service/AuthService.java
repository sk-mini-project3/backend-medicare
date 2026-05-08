package com.emr.medicare.auth.service;

import com.emr.medicare.auth.dto.LoginRequest;
import com.emr.medicare.auth.dto.ReissueRequest;
import com.emr.medicare.auth.dto.SignupRequest;
import com.emr.medicare.auth.dto.TokenResponse;
import com.emr.medicare.common.exception.TooManyRequestsException;
import com.emr.medicare.security.jwt.JwtProvider;
import com.emr.medicare.user.entity.User;
import com.emr.medicare.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final StringRedisTemplate redisTemplate;

    public void signup(SignupRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(
                        passwordEncoder.encode(
                                request.getPassword()
                        )
                )
                .name(request.getName())
                .phone(request.getPhone())
                .role(request.getRole())
                .build();

        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest request) {

        String failKey =
                "login:fail:" + request.getEmail();

        // 1. 로그인 잠금 여부 검사
        String failCount =
                redisTemplate.opsForValue().get(failKey);

        if (failCount != null &&
                Integer.parseInt(failCount) >= 5) {

            throw new TooManyRequestsException(
                    "로그인 5회 실패로 5분간 잠금되었습니다."
            );
        }

        User user = userRepository.findByEmail(
                request.getEmail()
        ).orElse(null);

        // 2. 이메일 없음
        if (user == null) {

            increaseFailCount(failKey);

            throw new IllegalArgumentException(
                    "이메일 또는 비밀번호가 올바르지 않습니다."
            );
        }

        // 3. 비밀번호 틀림
        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword()
        )) {

            increaseFailCount(failKey);

            throw new IllegalArgumentException(
                    "이메일 또는 비밀번호가 올바르지 않습니다."
            );
        }

        // 4. 로그인 성공 시 실패 횟수 초기화
        redisTemplate.delete(failKey);

        String accessToken =
                jwtProvider.createAccessToken(
                        user.getUserId(),
                        user.getEmail(),
                        user.getRole()
                );

        String refreshToken =
                jwtProvider.createRefreshToken(
                        user.getUserId(),
                        user.getEmail(),
                        user.getRole()
                );

        redisTemplate.opsForValue().set(
                "refresh:" + user.getUserId(),
                refreshToken,
                7,
                TimeUnit.DAYS
        );

        return new TokenResponse(
                accessToken,
                refreshToken,
                user.getRole().name()
        );
    }

    public TokenResponse reissue(
            ReissueRequest request
    ) {

        String refreshToken =
                request.getRefreshToken();

        if (!jwtProvider.validateToken(refreshToken)) {

            throw new IllegalArgumentException(
                    "유효하지 않은 Refresh Token입니다."
            );
        }

        Long userId =
                jwtProvider.getUserId(refreshToken);

        String savedRefreshToken =
                redisTemplate.opsForValue().get(
                        "refresh:" + userId
                );

        if (savedRefreshToken == null ||
                !savedRefreshToken.equals(refreshToken)) {

            throw new IllegalArgumentException(
                    "Refresh Token이 일치하지 않습니다."
            );
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "사용자를 찾을 수 없습니다."
                        )
                );

        String newAccessToken =
                jwtProvider.createAccessToken(
                        user.getUserId(),
                        user.getEmail(),
                        user.getRole()
                );

        String newRefreshToken =
                jwtProvider.createRefreshToken(
                        user.getUserId(),
                        user.getEmail(),
                        user.getRole()
                );

        redisTemplate.opsForValue().set(
                "refresh:" + userId,
                newRefreshToken,
                7,
                TimeUnit.DAYS
        );

        return new TokenResponse(
                newAccessToken,
                newRefreshToken,
                user.getRole().name()
        );
    }

    public void logout(String accessToken) {

        if (!jwtProvider.validateToken(accessToken)) {

            throw new IllegalArgumentException(
                    "유효하지 않은 토큰입니다."
            );
        }

        Long userId =
                jwtProvider.getUserId(accessToken);

        redisTemplate.delete(
                "refresh:" + userId
        );

        long expiration =
                jwtProvider.getRemainingTime(accessToken);

        redisTemplate.opsForValue().set(
                "blacklist:" + accessToken,
                "logout",
                expiration,
                TimeUnit.MILLISECONDS
        );
    }

    // 로그인 실패 횟수 증가
    private void increaseFailCount(
            String failKey
    ) {

        Long count =
                redisTemplate.opsForValue()
                        .increment(failKey);

        // 최초 실패 시 5분 TTL 설정
        if (count != null && count == 1) {

            redisTemplate.expire(
                    failKey,
                    5,
                    TimeUnit.MINUTES
            );
        }
    }
}